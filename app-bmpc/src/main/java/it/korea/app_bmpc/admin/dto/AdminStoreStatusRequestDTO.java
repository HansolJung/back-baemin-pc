package it.korea.app_bmpc.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminStoreStatusRequestDTO {
    @NotNull(message = "가게 아이디는 필수 항목입니다.")
    private Integer storeId;

    @NotBlank(message = "휴무 여부는 필수 항목입니다.")
    @Pattern(regexp = "^[YN]$", message = "휴무 여부는 'Y' 또는 'N'이어야 합니다.")
    private String closeYn;      
}
