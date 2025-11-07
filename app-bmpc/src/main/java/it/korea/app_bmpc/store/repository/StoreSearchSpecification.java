package it.korea.app_bmpc.store.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import it.korea.app_bmpc.menu.entity.MenuCategoryEntity;
import it.korea.app_bmpc.menu.entity.MenuEntity;
import it.korea.app_bmpc.store.dto.StoreSearchDTO;
import it.korea.app_bmpc.store.entity.CategoryEntity;
import it.korea.app_bmpc.store.entity.StoreCategoryEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class StoreSearchSpecification implements Specification<StoreEntity> {

    private StoreSearchDTO searchDTO;

    public StoreSearchSpecification(StoreSearchDTO searchDTO) {
        this.searchDTO = searchDTO;
    }

    // root 비교대상 -> entity (JPA 가 만들어서 넣어줌)
    // query : sql 조작 (잘 사용하지 않음)
    // cb : where 조건
    @Override
    public Predicate toPredicate(Root<StoreEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        Subquery<Integer> subQuery = query.subquery(Integer.class);
        Root<MenuEntity> menuRoot = subQuery.from(MenuEntity.class);

        // 메뉴가 적어도 1개 이상인 가게들만 검색되도록 서브쿼리 작성
        subQuery.select(cb.literal(1))
                .where(cb.equal(menuRoot.get("menuCategory").get("store"), root), // 메뉴 -> 카테고리 -> 스토어 매칭
                    cb.equal(menuRoot.get("delYn"), "N"),  // 기본적으로 삭제 여부가 N 인 메뉴들만 검색
                    cb.equal(menuRoot.get("menuCategory").get("delYn"), "N")); // 상위 메뉴 카테고리의 삭제 여부도 N 이어야 함

        predicates.add(cb.exists(subQuery));  // 위 서브쿼리의 결과가 존재해야함

        if (StringUtils.isNotBlank(searchDTO.getSearchText())) {  // 검색어가 있을 경우
            String likeText = "%" + searchDTO.getSearchText() + "%";

            // Store -> MenuCategory -> Menu 순으로 LEFT 조인하기
            Join<StoreEntity, MenuCategoryEntity> menuCategoryJoin = root.join("menuCategoryList", JoinType.LEFT);
            Join<MenuCategoryEntity, MenuEntity> menuJoin = menuCategoryJoin.join("menuList", JoinType.LEFT);

            Predicate validMenuPredicate = cb.and(
                cb.equal(menuJoin.get("delYn"), "N"), 
                cb.equal(menuCategoryJoin.get("delYn"), "N"));
            Predicate storeNamePredicate = cb.like(root.get("storeName"), likeText);
            Predicate menuNamePredicate = cb.and(
                validMenuPredicate, 
                cb.like(menuJoin.get("menuName"), likeText));

            // 가게명 또는 메뉴명으로 검색
            Predicate orPredicate = orTogether(List.of(storeNamePredicate, menuNamePredicate), cb);
            predicates.add(orPredicate);

            // 가게 중복 제거
            query.distinct(true);
        }

        if (searchDTO.getCaId() != 0) { // 검색하려는 카테고리가 있을 경우... 즉, 전체보기가 아닐 경우
            
            // Store -> StoreCategory -> Category 순으로 LEFT 조인하기
            Join<StoreEntity, StoreCategoryEntity> storeCategoryJoin = root.join("categoryList", JoinType.LEFT);
            Join<StoreCategoryEntity, CategoryEntity> categoryJoin = storeCategoryJoin.join("category", JoinType.LEFT);

            // 카테고리 아이디가 일치하는 가게만 검색
            predicates.add(cb.equal(categoryJoin.get("caId"), searchDTO.getCaId()));

            query.distinct(true);
        }

        predicates.add(cb.equal(root.get("delYn"), "N"));   // 기본적으로 삭제 여부가 N 인 가게들만 검색

        // 사용자가 입력한 주소의 위도/경도 값으로 반경 4km 내의 가게들만 필터링 (Haversine 공식 사용)
        // 만약 사용자 주소가 위도/경도 값으로 변환이 안됐다면 해당 필터링 과정은 스킵
        if (searchDTO.getUserLatitude() != null && searchDTO.getUserLongitude() != null) {

            // 사용자 좌표를 라디안으로 변환
            Expression<Double> lat1 = cb.function("radians", Double.class, cb.literal(searchDTO.getUserLatitude()));
            Expression<Double> lon1 = cb.function("radians", Double.class, cb.literal(searchDTO.getUserLongitude()));

            // 가게 좌표를 라디안으로 변환
            Expression<Double> lat2 = cb.function("radians", Double.class, root.get("latitude"));
            Expression<Double> lon2 = cb.function("radians", Double.class, root.get("longitude"));

            // Δlat / 2, Δlon / 2
            // 나누기 2 대신, 곱하기 0.5 로 나누기 효과를 구현해야 함
            Expression<Double> halfLatDiff = cb.prod(cb.diff(lat2, lat1), cb.literal(0.5));
            Expression<Double> halfLonDiff = cb.prod(cb.diff(lon2, lon1), cb.literal(0.5));

            // sin²(Δlat/2), sin²(Δlon/2)
            Expression<Double> sinLatSq = 
                cb.prod(cb.function("sin", Double.class, halfLatDiff),
                        cb.function("sin", Double.class, halfLatDiff));
            Expression<Double> sinLonSq = 
                cb.prod(cb.function("sin", Double.class, halfLonDiff),
                        cb.function("sin", Double.class, halfLonDiff));

            // a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
            Expression<Double> a = 
                cb.sum(
                    sinLatSq,
                    cb.prod(
                        cb.prod(
                            cb.function("cos", Double.class, lat1),
                            cb.function("cos", Double.class, lat2)),
                        sinLonSq));

            // c = 2 * atan2(sqrt(a), sqrt(1 - a))
            Expression<Double> c = cb.prod(
                cb.literal(2.0),
                cb.function("atan2", Double.class,
                    cb.function("sqrt", Double.class, a),
                    cb.function("sqrt", Double.class, cb.diff(cb.literal(1.0), a))
                )
            );

            // distance = R * c
            // 6371.0 은 지구의 반지름 값
            Expression<Double> distance = cb.prod(cb.literal(6371.0), c);

            // 반경 4km 이하 필터링 조건 추가
            predicates.add(cb.lessThanOrEqualTo(distance, 4.0));
        }

        return andTogether(predicates, cb);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));  // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }

    private Predicate orTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.or(predicates.toArray(new Predicate[0]));   // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }
}
