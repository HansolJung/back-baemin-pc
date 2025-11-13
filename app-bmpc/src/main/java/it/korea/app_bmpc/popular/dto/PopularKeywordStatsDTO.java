package it.korea.app_bmpc.popular.dto;

import java.time.LocalDate;

import it.korea.app_bmpc.popular.entity.PopularKeywordStatsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PopularKeywordStatsDTO {
    private int statId;
    private String keyword;
    private LocalDate statDate;
    private int dailyCount;
    private int totalCount;
    private Integer rank;
    private Integer prevRank;
    private Integer rankDiff;

    public static PopularKeywordStatsDTO of(PopularKeywordStatsEntity entity) {
        return PopularKeywordStatsDTO.builder()
            .statId(entity.getStatId())
            .keyword(entity.getKeyword())
            .statDate(entity.getStatDate())
            .dailyCount(entity.getDailyCount())
            .totalCount(entity.getTotalCount())
            .rank(entity.getRank())
            .prevRank(entity.getPrevRank())
            .rankDiff(entity.getRankDiff())
            .build();
    }
}
