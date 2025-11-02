package it.korea.app_bmpc.review.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import it.korea.app_bmpc.admin.dto.AdminReviewSearchDTO;
import it.korea.app_bmpc.review.entity.ReviewEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ReviewSearchSpecification implements Specification<ReviewEntity> {

    private AdminReviewSearchDTO searchDTO;

    public ReviewSearchSpecification(AdminReviewSearchDTO searchDTO) {
        this.searchDTO = searchDTO;
    }

    // root 비교대상 -> entity (JPA 가 만들어서 넣어줌)
    // query : sql 조작 (잘 사용하지 않음)
    // cb : where 조건
    @Override
    public Predicate toPredicate(Root<ReviewEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchDTO.getSearchText())) {  // 검색어가 있을 경우
            String likeText = "%" + searchDTO.getSearchText() + "%";

            // 리뷰 -> 가게 순으로 LEFT 조인하기
            Join<ReviewEntity, StoreEntity> storeJoin = root.join("store", JoinType.INNER);

            // 가게명 또는 리뷰 내용으로 검색
            Predicate storeNamePredicate = cb.like(storeJoin.get("storeName"), likeText);
            Predicate contentPredicate = cb.like(root.get("content"), likeText);

            // 가게명 또는 리뷰 내용으로 검색
            Predicate orPredicate = orTogether(List.of(storeNamePredicate, contentPredicate), cb);
            predicates.add(orPredicate);

            query.distinct(true);
        }

        // 기본적으로 삭제가 안된 리뷰만 검색
        predicates.add(cb.equal(root.get("delYn"), "N"));

        return andTogether(predicates, cb);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));  // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }

    private Predicate orTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.or(predicates.toArray(new Predicate[0]));   // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }
}
