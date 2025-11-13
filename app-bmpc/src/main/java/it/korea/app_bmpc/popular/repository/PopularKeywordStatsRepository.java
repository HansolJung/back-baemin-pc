package it.korea.app_bmpc.popular.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import it.korea.app_bmpc.popular.entity.PopularKeywordStatsEntity;
import jakarta.transaction.Transactional;

public interface PopularKeywordStatsRepository extends JpaRepository<PopularKeywordStatsEntity, Integer> {

    List<PopularKeywordStatsEntity> findByStatDate(LocalDate statDate);

    Optional<PopularKeywordStatsEntity> findByKeywordAndStatDate(String keyword, LocalDate statDate);

    @Modifying
    @Transactional
    void deleteByStatDate(LocalDate statDate);


    List<PopularKeywordStatsEntity> findByStatDateOrderByRankAsc(LocalDate statDate);
}   
