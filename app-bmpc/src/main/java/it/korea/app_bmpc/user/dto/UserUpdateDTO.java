package it.korea.app_bmpc.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String userId;
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String userName;
    @NotBlank(message = "생년월일은 필수 항목입니다.")
    private String birth;
    @NotBlank(message = "전화번호는 필수 항목입니다.")
    private String phone;
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    private String businessNo;
}
