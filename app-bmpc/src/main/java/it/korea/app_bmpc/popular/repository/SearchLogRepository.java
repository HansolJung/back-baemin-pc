package it.korea.app_bmpc.popular.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_bmpc.popular.entity.SearchLogEntity;

public interface SearchLogRepository extends JpaRepository<SearchLogEntity, Integer> {


    @Query("""
            select s.searchText, count(s)
            from SearchLogEntity s
            where s.createDate >= :startDate AND s.createDate < :endDate
            GROUP BY s.searchText
        """)
    List<Object[]> getSearchTextWithCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
