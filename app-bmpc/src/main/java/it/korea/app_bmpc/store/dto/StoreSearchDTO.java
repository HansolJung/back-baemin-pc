package it.korea.app_bmpc.store.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreSearchDTO {
    private String searchText;
    private int caId;
    @NotBlank(message = "가게 검색시 사용자 주소는 필수값입니다.")
    private String addr;     // 사용자 주소

    private BigDecimal userLatitude;   // 사용자 위도(서비스에서 주입함)
    private BigDecimal userLongitude;  // 사용자 경도(서비스에서 주입함)
}
