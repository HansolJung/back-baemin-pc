package it.korea.app_bmpc.admin.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUserDTO {

    private String userId;
    private String passwd;
    private String userName;
    private String birth;
    private String gender;
    private String phone;
    private String email;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
    private String useYn;
    private String delYn;
    private String userRole;
    private String roleName;
    private int deposit;
    private int balance;
    private String businessNo;
    private Integer storeId;
    private String storeName;

    public static AdminUserDTO of(UserEntity entity) {

        StoreEntity storeEntity = entity.getStore();
        Integer storeId = storeEntity != null ? storeEntity.getStoreId() : null;
        String storeName = storeEntity != null ? storeEntity.getStoreName() : null;

        return AdminUserDTO.builder()
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .birth(entity.getBirth())
            .gender(entity.getGender())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .createDate(entity.getCreateDate())
            .updateDate(entity.getUpdateDate())
            .useYn(entity.getUseYn())
            .delYn(entity.getDelYn())
            .userRole(entity.getRole().getRoleId())
            .roleName(entity.getRole().getRoleName())
            .deposit(entity.getDeposit())
            .balance(entity.getBalance())
            .businessNo(entity.getBusinessNo())
            .storeId(storeId)
            .storeName(storeName)
            .build();
    }

    public static AdminUserDTO of(AdminUserProjection entity) {
        return AdminUserDTO.builder()
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .birth(entity.getBirth())
            .gender(entity.getGender())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .createDate(entity.getCreateDate())
            .updateDate(entity.getUpdateDate())
            .useYn(entity.getUseYn())
            .delYn(entity.getDelYn())
            .userRole(entity.getRoleId())
            .roleName(entity.getRoleName())
            .deposit(entity.getDeposit())
            .balance(entity.getBalance())
            .businessNo(entity.getBusinessNo())
            .build();
    }

    public static UserEntity to(AdminUserDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUserId(dto.getUserId());
        entity.setPasswd(dto.getPasswd());
        entity.setUserName(dto.getUserName());
        entity.setBirth(dto.getBirth());
        entity.setGender(dto.getGender());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setUseYn(dto.getUseYn());
        entity.setDelYn(dto.getDelYn());
        entity.setDeposit(dto.getDeposit());
        entity.setBalance(dto.getBalance());
        entity.setBusinessNo(dto.getBusinessNo());

        return entity;
    }
}
