package it.korea.app_bmpc.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_bmpc.review.entity.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer>, JpaSpecificationExecutor<ReviewEntity> {

    // @EntityGraph(attributePaths = {"user", "fileList", "reply", "reply.user"})   // N+1 현상 해결
    // Page<ReviewEntity> findAllByStore_storeIdAndDelYn(int storeId, String delYn, Pageable pageable);

    @Query(value = """
            select r from ReviewEntity r
            join fetch r.order o
            left join fetch o.itemList i
            where r.store.storeId = :storeId
            and r.delYn = :delYn
        """,
        countQuery = """
            select count(r) from ReviewEntity r
            where r.store.storeId = :storeId
            and r.delYn = :delYn
        """
    )
    Page<ReviewEntity> findAllByStoreId(@Param("storeId") int storeId, @Param("delYn") String delYn, Pageable pageable);

    // @EntityGraph(attributePaths = {"user", "fileList", "reply", "reply.user"})   // N+1 현상 해결
    // Page<ReviewEntity> findAllByUser_userIdAndDelYn(String userId, String delYn, Pageable pageable);

    @Query(value = """
            select r from ReviewEntity r
            join fetch r.order o
            left join fetch o.itemList i
            where r.user.userId = :userId
            and r.delYn = :delYn
        """,
        countQuery = """
            select count(r) from ReviewEntity r
            where r.user.userId = :userId
            and r.delYn = :delYn
        """
    )
    Page<ReviewEntity> findAllByUserId(@Param("userId") String userId, @Param("delYn") String delYn, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "fileList", "reply", "reply.user"})  // N+1 현상 해결
    Page<ReviewEntity> findAll(Specification<ReviewEntity> searchSpecification, Pageable pageable);

    boolean existsByOrder_orderId(int orderId);

    @Query("select avg(r.rating) from ReviewEntity r where r.store.storeId = :storeId and r.delYn = 'N'")
    Double findRatingAvgByStoreId(@Param("storeId") int storeId);

    Long countByStore_storeIdAndDelYn(int storeId, String delYn);

    List<ReviewEntity> findAllByOrder_orderIdIn(List<Integer> orderIdList);
}
