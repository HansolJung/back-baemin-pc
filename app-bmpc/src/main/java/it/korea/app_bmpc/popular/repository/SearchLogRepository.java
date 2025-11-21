package it.korea.app_bmpc.popular.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_bmpc.popular.dto.SearchLogProjection;
import it.korea.app_bmpc.popular.entity.SearchLogEntity;

public interface SearchLogRepository extends JpaRepository<SearchLogEntity, Integer> {

    @Query("""
            select s.searchText as searchText, count(s) as count
            from SearchLogEntity s
            where s.createDate >= :startDate and s.createDate < :endDate
            group by s.searchText
        """)
    List<SearchLogProjection> getSearchTextWithCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
