package it.korea.app_bmpc.kakao.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class KakaoAddressResponseDTO {

    private List<Document> documents;

    @Getter
    @Setter
    public static class Document {
        private String y;  // 위도 (latitude)
        private String x;  // 경도 (longitude)
    }
}
