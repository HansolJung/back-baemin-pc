package it.korea.app_bmpc.store.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import it.korea.app_bmpc.admin.dto.AdminStoreSearchDTO;
import it.korea.app_bmpc.menu.entity.MenuCategoryEntity;
import it.korea.app_bmpc.menu.entity.MenuEntity;
import it.korea.app_bmpc.store.entity.CategoryEntity;
import it.korea.app_bmpc.store.entity.StoreCategoryEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class AdminStoreSearchSpecification implements Specification<StoreEntity> {

    private AdminStoreSearchDTO searchDTO;

    public AdminStoreSearchSpecification(AdminStoreSearchDTO searchDTO) {
        this.searchDTO = searchDTO;
    }

    // root 비교대상 -> entity (JPA 가 만들어서 넣어줌)
    // query : sql 조작 (잘 사용하지 않음)
    // cb : where 조건
    @Override
    public Predicate toPredicate(Root<StoreEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchDTO.getSearchText())) {  // 검색어가 있을 경우
            String likeText = "%" + searchDTO.getSearchText() + "%";

            // Store -> MenuCategory -> Menu 순으로 LEFT 조인하기
            Join<StoreEntity, MenuCategoryEntity> menuCategoryJoin = root.join("menuCategoryList", JoinType.LEFT);
            Join<MenuCategoryEntity, MenuEntity> menuJoin = menuCategoryJoin.join("menuList", JoinType.LEFT);

            Predicate storeNamePredicate = cb.like(root.get("storeName"), likeText);
            Predicate menuNamePredicate = cb.like(menuJoin.get("menuName"), likeText);

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

        return andTogether(predicates, cb);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));  // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }

    private Predicate orTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.or(predicates.toArray(new Predicate[0]));   // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }
}
