package it.korea.app_bmpc.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class AdminUserRequestDTO {

    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String userId;
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String passwd;
    @NotBlank(message = "권한은 필수 항목입니다.")
    private String userRole;
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String userName;
    @NotBlank(message = "생년월일은 필수 항목입니다.")
    private String birth;
    @NotBlank(message = "성별은 필수 항목입니다.")
    private String gender;
    @NotBlank(message = "전화번호는 필수 항목입니다.")
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    private String phone;
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "사용여부는 필수 항목입니다.")
    private String useYn;
    private int deposit;
    private int balance;
    private String businessNo;

    public String getDelYn() {
        return "N";
    }
}
