package it.korea.app_bmpc.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class PasswordRequestDTO {

    @Data
    public static class Forgot {
        @NotBlank(message = "아이디는 필수 항목입니다.")
        private String userId;
        @NotBlank(message = "이메일은 필수 항목입니다.")
        private String email;
    }

    @Data
    public static class Reset {
        @NotBlank(message = "비밀번호 재설정 토큰은 필수 항목입니다.")
        private String token;
        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "비밀번호는 8~20자이며, 영문과 숫자를 각각 1개 이상 포함해야 합니다. (특수문자 불가)")
        private String newPassword;
    }
}
