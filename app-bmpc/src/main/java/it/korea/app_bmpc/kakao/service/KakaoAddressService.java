package it.korea.app_bmpc.kakao.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import it.korea.app_bmpc.kakao.dto.KakaoAddressResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAddressService {

    private final WebClient webClient;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;   // API 키

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;   // API URL

    public Optional<KakaoAddressResponseDTO> getLocation(String address) {
        log.info("카카오 맵 API 호출");

        try {
            KakaoAddressResponseDTO response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(kakaoApiUrl)
                    .queryParam("query", address)
                    .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)   // API KEY 앞에 KakaoAK 를 꼭 붙여야함
                .retrieve()
                .bodyToMono(KakaoAddressResponseDTO.class)
                .block();   // 동기 처리

            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                log.warn("카카오 맵 API 응답 없음");
                return Optional.empty();
            }

            return Optional.of(response);
        } catch (Exception e) {
            log.error("카카오 맵 API 호출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
