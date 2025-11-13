package it.korea.app_bmpc.popular.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.popular.dto.PopularKeywordStatsDTO;
import it.korea.app_bmpc.popular.entity.PopularKeywordStatsEntity;
import it.korea.app_bmpc.popular.repository.PopularKeywordStatsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopularService {

    private final PopularKeywordStatsRepository statsRepository;

    /**
     * 어제 기준 인기 검색어 TOP 10 조회 (with 캐시)
     * @return
     */
    @Cacheable("popularKeywordList")   // 캐시 저장
    @Transactional(readOnly = true)
    public List<PopularKeywordStatsDTO> getPopularKeywordList() {

        // 어제 날짜
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<PopularKeywordStatsEntity> entityList = statsRepository.findByStatDateOrderByRankAsc(yesterday);

        // 혹시나 개수가 10을 넘어갈 수 있기 때문에 limit 10 추가
        return entityList.stream().limit(10).map(PopularKeywordStatsDTO::of).toList();
    }
}
