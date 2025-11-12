package it.korea.app_bmpc.popular.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.korea.app_bmpc.popular.entity.PopularKeywordStatsEntity;

public interface PopularKeywordStatsRepository extends JpaRepository<PopularKeywordStatsEntity, Integer> {
    List<PopularKeywordStatsEntity> findByStatDate(LocalDate statDate);
}   
