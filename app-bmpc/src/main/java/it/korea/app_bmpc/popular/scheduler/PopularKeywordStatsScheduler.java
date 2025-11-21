package it.korea.app_bmpc.popular.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.korea.app_bmpc.popular.dto.SearchLogProjection;
import it.korea.app_bmpc.popular.entity.PopularKeywordEntity;
import it.korea.app_bmpc.popular.entity.PopularKeywordStatsEntity;
import it.korea.app_bmpc.popular.repository.PopularKeywordRepository;
import it.korea.app_bmpc.popular.repository.PopularKeywordStatsRepository;
import it.korea.app_bmpc.popular.repository.SearchLogRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인기 검색어 통계 스케줄러
 * 매일 새벽 1시에 인기 검색어 통계 데이터를 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopularKeywordStatsScheduler {

    private final PopularKeywordRepository popularKeywordRepository;
    private final PopularKeywordStatsRepository popularKeywordStatsRepository;
    private final SearchLogRepository searchLogRepository;

    //@Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    @Scheduled(cron = "0 */1 * * * *") // 1분마다 실행 (테스트용). 추후 새벽 1시로 바꿔야함
    @Transactional
    public void createDailyPopularKeywordStats() {

        log.info("인기 검색어 통계 스케줄러 시작");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // 제일 먼저 기존 통계 데이터 삭제 
            popularKeywordStatsRepository.deleteByStatDate(yesterday);

            LocalDateTime startOfYesterday = yesterday.atStartOfDay();
            LocalDateTime startOfToday = yesterday.plusDays(1).atStartOfDay();

            // 어제 하루 검색 횟수 집계하기
            List<SearchLogProjection> dailyCounts = searchLogRepository.getSearchTextWithCount(startOfYesterday, startOfToday);

            // <검색어, 검색 횟수>로 Map 만들기
            Map<String, Integer> dailyCountMap = 
                dailyCounts.stream().collect(Collectors.toMap(SearchLogProjection::getSearchText, c -> c.getCount().intValue()));

            // 모든 popular_keyword 가져오기
            List<PopularKeywordEntity> popularKeywordList = popularKeywordRepository.findAll();

            // 점수 계산하기
            List<KeywordScore> keywordScoreList = popularKeywordList.stream()
                .map(keyword -> {
                    int dailyCount = dailyCountMap.getOrDefault(keyword.getKeyword(), 0);
                    double score = keyword.getSearchCount() * 0.7 + dailyCount * 0.3; // '기존 검색 횟수 + 어제 하루 검색 횟수'로 적절하게 가중치 부여해서 점수 계산

                    return new KeywordScore(keyword, score, dailyCount);
                })
                .sorted(Comparator.comparingDouble(KeywordScore::getScore).reversed())
                .limit(10)
                .toList();

            // 이전날 순위 조회
            Map<String, Integer> prevRankMap = popularKeywordStatsRepository
                .findByStatDate(yesterday.minusDays(1))
                .stream()
                .collect(Collectors.toMap(
                        PopularKeywordStatsEntity::getKeyword,
                        PopularKeywordStatsEntity::getRank
                ));

            // 인기검색어 TOP 10 통계 생성
            int rank = 1;
            for (KeywordScore keywordScore : keywordScoreList) {
                try {
                    PopularKeywordEntity keywordEntity = keywordScore.getKeywordEntity();
                    String keyword = keywordEntity.getKeyword();

                    // 이미 존재하는 통계 조회
                    PopularKeywordStatsEntity statsEntity = popularKeywordStatsRepository.findByKeywordAndStatDate(keyword, yesterday)
                        .orElseGet(PopularKeywordStatsEntity::new);

                    Integer prevRank = prevRankMap.get(keyword);
                    Integer rankDiff = (prevRank != null) ? prevRank - rank : null; // 새로 등장한 검색어의 순위는 null로 들어가게 됨

                    statsEntity.setKeyword(keyword);
                    statsEntity.setStatDate(yesterday);
                    statsEntity.setDailyCount(keywordScore.getDailyCount()); // 어제 하루치 검색량
                    statsEntity.setTotalCount(keywordEntity.getSearchCount());
                    statsEntity.setRank(rank);
                    statsEntity.setPrevRank(prevRank);
                    statsEntity.setRankDiff(rankDiff);

                    popularKeywordStatsRepository.save(statsEntity);
                    rank++;
                } catch (Exception e) {
                    // 개별 키워드 처리 실패시 로그만 남기고 다음 키워드 진행
                    log.error("인기 검색어 통계 저장 중 오류 발생. keyword : {}, ", keywordScore.getKeywordEntity().getKeyword(), e.getMessage());
                }
            }

            log.info("인기 검색어 통계 스케줄러 완료");
        } catch (Exception e) {
            log.error("인기 검색어 통계 스케줄러 실행 중 예외 발생", e);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class KeywordScore {
        private final PopularKeywordEntity keywordEntity;
        private final double score;
        private final int dailyCount;
    }
}
