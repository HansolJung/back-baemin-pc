package it.korea.app_bmpc.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LogoutHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // 세션 가져오기
        HttpSession session = request.getSession();

        // 유저 정보가 세션에 남아있으면 지우기
        if (session.getAttribute("user") != null) {
            session.removeAttribute("user");
        }

        Cookie[] cookies = request.getCookies();

        // 쿠기 정보가 남아있으면 지우기
        if (cookies != null) {
            // 시간을 0으로 해서 삭제
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
