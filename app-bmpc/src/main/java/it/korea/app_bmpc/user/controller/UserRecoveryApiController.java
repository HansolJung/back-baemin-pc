package it.korea.app_bmpc.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.user.dto.UserRecoveryDTO;
import it.korea.app_bmpc.user.service.UserRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 계정 복구 API", description = "아이디 찾기 및 비밀번호 재설정 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserRecoveryApiController {

    private final UserRecoveryService recoveryService;

    /**
     * 사용자 아이디 정보 요청하기
     * @param request 사용자 정보 객체
     * @return
     */
    @PostMapping("/recovery/id/forgot")
    @Operation(summary = "사용자 아이디 정보 요청하기")
    public ResponseEntity<?> forgotUserId(@Valid @RequestBody UserRecoveryDTO.ForgotId request) {

        recoveryService.sendUserId(request.getEmail());

        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    /**
     * 비밀번호 재설정 링크 요청하기
     * @param request 사용자 정보 객체
     * @return
     */
    @PostMapping("/recovery/passwd/forgot")
    @Operation(summary = "비밀번호 재설정 링크 요청하기")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody UserRecoveryDTO.ForgotPassword request) {

        recoveryService.sendPasswordResetLink(request.getUserId(), request.getEmail());

        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    /**
     * 비밀번호 재설정하기
     * @param request 비밀번호 재설정 객체
     * @return
     */
    @PostMapping("/recovery/passwd/reset")
    @Operation(summary = "비밀번호 재설정하기")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserRecoveryDTO.ResetPassword request) {

        recoveryService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }
}
