package it.korea.app_bmpc.popular.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.popular.dto.PopularKeywordStatsDTO;
import it.korea.app_bmpc.popular.service.PopularService;
import lombok.RequiredArgsConstructor;

@Tag(name = "인기 검색어 API", description = "어제 기준 인기 검색어 TOP 10 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PopularController {

    private final PopularService popularService;

    @GetMapping("/popular")
    @Operation(summary = "어제 기준 인기 검색어 TOP 10 가져오기 (with 캐시)")
    public ResponseEntity<?> getPopularKeywordList() throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        List<PopularKeywordStatsDTO> keywordList = popularService.getPopularKeywordList();

        resultMap.put("content", keywordList);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }
}
