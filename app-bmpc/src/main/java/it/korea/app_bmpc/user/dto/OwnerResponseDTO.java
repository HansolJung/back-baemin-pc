package it.korea.app_bmpc.user.dto;

import it.korea.app_bmpc.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OwnerResponseDTO {

    private String userName;
    private String phone;
    private String email;
    private String businessNo;  // 점주일 때만 값이 돌아옴

    public static OwnerResponseDTO of(UserEntity entity) {

        return OwnerResponseDTO.builder()
            .userName(entity.getUserName())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .businessNo(entity.getBusinessNo())
            .build();
    }
}
