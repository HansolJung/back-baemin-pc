package it.korea.app_bmpc.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.common.utils.CookieUtils;
import it.korea.app_bmpc.common.utils.JWTUtils;
import it.korea.app_bmpc.filter.LoginFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "refresh 토큰 API", description = "refresh 토큰을 이용한 재로그인 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RefreshTokenController {

    private final JWTUtils jwtUtils;
    private final CookieUtils cookieUtils;

    /**
     * refresh 토큰으로 재로그인 하기
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/refresh")
    @Operation(summary = "refresh 토큰으로 재로그인 하기")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body("Invalid refresh token");
        }

        String userId = jwtUtils.getUserId(refreshToken);
        String userName = jwtUtils.getUserName(refreshToken);
        String userRole = jwtUtils.getUserRole(refreshToken);

        // 토큰 생성
        String accessToken = jwtUtils.createJwt("access", userId, userName, userRole, LoginFilter.ACCESS_TOKEN_EXPIRE_TIME);
        String newRefresh = jwtUtils.createJwt("refresh", userId, userName, userRole, LoginFilter.REFRESH_TOKEN_EXPIRE_TIME);

        // 응답을 설정
        cookieUtils.addCookie(cookieUtils.createCookie("refresh", newRefresh, 
            (int) LoginFilter.REFRESH_TOKEN_EXPIRE_TIME), response);
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> mapContent = new HashMap<>();
        mapContent.put("resultMsg", "OK");
        mapContent.put("status", HttpServletResponse.SC_OK);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("userName", userName);
        data.put("userRole", userRole);
        data.put("token", accessToken);

        mapContent.put("content", data);

        return ResponseEntity.status(HttpServletResponse.SC_OK).body(ApiResponse.ok(mapContent));
    }
}
