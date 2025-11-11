package it.korea.app_bmpc.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "아이디는 필수 항목입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 4~20자의 영문 소문자와 숫자만 사용할 수 있습니다.")
    private String userId;
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "비밀번호는 8~20자이며, 영문과 숫자를 각각 1개 이상 포함해야 합니다. (특수문자 불가)")
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
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "권한은 필수 항목입니다.")
    private String userRole;
    private int deposit;
    private int balace;
    private String businessNo;

    public String getUseYn() {
        return "Y";
    }

    public String getDelYn() {
        return "N";
    }
}
