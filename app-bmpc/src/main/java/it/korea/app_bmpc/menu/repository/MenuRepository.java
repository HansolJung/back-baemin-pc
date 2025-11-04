package it.korea.app_bmpc.menu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_bmpc.menu.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

    @Query("""
        select distinct m
        from MenuEntity m
        left join fetch m.file
        left join fetch m.menuOptionGroupList mog
        left join fetch mog.menuOptionList mol
        where m.menuId = :menuId
        and m.delYn = 'N'
    """)
    Optional<MenuEntity> getMenu(@Param("menuId") int menuId);

    // 반드시 연결되어 있어야 하기 때문에 left join 대신 (inner) join 사용
    @Query("""
        select m from MenuEntity m
        join fetch m.menuCategory c
        join fetch c.store s
        where m.menuId in :menuIdList
    """)
    List<MenuEntity> findAllByMenuIdList(@Param("menuIdList") List<Integer> menuIdList);
}
