package it.korea.app_bmpc.user.dto;

import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.entity.UserRoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserResponseDTO {

    private String userId;
    private String userName;
    private String birth;
    private String gender;
    private String phone;
    private String email;
    private int deposit;
    private int balance;  // 점주일 때만 값이 돌아옴
    private String roleName;
    private String businessNo;  // 점주일 때만 값이 돌아옴

    public static UserResponseDTO of(UserEntity entity) {

        UserRoleEntity role = entity.getRole();

        return UserResponseDTO.builder()
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .birth(entity.getBirth())
            .gender(entity.getGender())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .deposit(entity.getDeposit())
            .balance(entity.getBalance())
            .roleName(role.getRoleName())
            .businessNo(entity.getBusinessNo())
            .build();
    }
}
