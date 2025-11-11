package it.korea.app_bmpc.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.user.dto.PasswordRequestDTO;
import it.korea.app_bmpc.user.service.PasswordResetTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "비밀번호 재설정 토큰 API", description = "비밀번호 재설정 토큰을 이용한 비밀번호 재설정 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PasswordResetTokenController {

    private final PasswordResetTokenService resetService;

    @PostMapping("/passwd/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordRequestDTO.Forgot request) {

        resetService.sendPasswordResetLink(request.getUserId(), request.getEmail());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    @PostMapping("/passwd/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordRequestDTO.Reset request) {

        resetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
