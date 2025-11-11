package it.korea.app_bmpc.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_bmpc.admin.dto.AdminUserProjection;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(attributePaths = {"role"})   // 전체 유저 리스트 가져올 때 N+1 현상 해결
    Page<UserEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"role"})   // 전체 유저 리스트 가져올 때 N+1 현상 해결
    Page<UserEntity> findAll(Specification<UserEntity> searchSpecification, Pageable pageable);

    // Page<UserEntity> findByUserIdContainingOrUserNameContaining(String searchText1, 
    //     String searchText2, Pageable pageable);    // 통합 검색할 때 사용

    @Query(value = """
            select u.user_id,
                u.user_name,
                u.birth,
                u.gender,
                u.phone,
                u.email,
                u.use_yn,
                u.del_yn,
                u.create_date,
                u.update_date,
                u.deposit,
                u.balance,
                r.role_id,
                r.role_name
            from bmpc_users
            join bmpc_user_role r on u.user_role = r.role_id
            where user_id = :userId
        """, nativeQuery = true)
    Optional<AdminUserProjection> getUserById(@Param("userId") String userId);   // 네이티브 쿼리 사용

    Optional<UserEntity> findByStore(StoreEntity store);

    Optional<UserEntity> findByStore_storeId(int storeId);

    Optional<UserEntity> findByEmail(String email);
}
