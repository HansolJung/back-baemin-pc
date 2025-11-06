package it.korea.app_bmpc.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.user.dto.UserDepositRequestDTO;
import it.korea.app_bmpc.user.dto.UserRequestDTO;
import it.korea.app_bmpc.user.dto.UserResponseDTO;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import it.korea.app_bmpc.user.dto.UserUpdateDTO;
import it.korea.app_bmpc.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 API", description = "내 정보 수정, 보유금 충전 등 사용자 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserApiController {

    private final UserService userService;

    /**
     * 회원 가입하기 
     * @param userRequestDTO 사용자 객체
     * @return
     * @throws Exception
     */
    @PostMapping("/register")
    @Operation(summary = "회원 가입하기")
    public ResponseEntity<?> register(@Valid @RequestBody // JSON 타입으로 받기 때문에 @RequestBody 사용
            UserRequestDTO userRequestDTO) throws Exception {

        userService.register(userRequestDTO);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 나의 보유금 충전하기
     * @param request 보유금 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // USER 권한이 있어야 접근 가능
    @PostMapping("/deposit/increase")
    @Operation(summary = "나의 보유금 충전하기")
    public ResponseEntity<?> increaseDeposit(@Valid @RequestBody UserDepositRequestDTO request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        if (!user.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("다른 사용자의 보유금은 충전할 수 없습니다.");
        }

        userService.increaseDeposit(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 나의 정보 가져오기
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @GetMapping("/user")
    @Operation(summary = "나의 정보 가져오기")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        UserResponseDTO dto = userService.getUser(user.getUserId());
        
        resultMap.put("vo", dto);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 나의 정보 수정하기
     * @param request 사용자 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PutMapping("/user")
    @Operation(summary = "나의 정보 수정하기")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDTO request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        if (!user.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("다른 사용자의 정보는 수정할 수 없습니다.");
        }

        userService.updateUser(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 나의 계정 탈퇴하기
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @DeleteMapping("/user")
    @Operation(summary = "나의 계정 탈퇴하기")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserSecureDTO user) throws Exception {

        userService.deleteUser(user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
