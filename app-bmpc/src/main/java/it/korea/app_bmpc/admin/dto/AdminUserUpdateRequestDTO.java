package it.korea.app_bmpc.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserUpdateRequestDTO {
    
    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String userId;
    private String passwd;
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String userName;
    @NotBlank(message = "생년월일은 필수 항목입니다.")
    private String birth;
    @NotBlank(message = "성별은 필수 항목입니다.")
    private String gender;
    @NotBlank(message = "전화번호는 필수 항목입니다.")
    private String phone;
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;

    private String businessNo;

}
