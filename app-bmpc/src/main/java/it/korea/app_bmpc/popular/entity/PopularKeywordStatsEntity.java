package it.korea.app_bmpc.popular.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_popular_keyword_stats")
public class PopularKeywordStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int statId;
    private String keyword;
    private LocalDate statDate;
    private int dailyCount;
    private int totalCount;
    private Integer rank;
    private Integer prevRank;
    private Integer rankDiff;
}
