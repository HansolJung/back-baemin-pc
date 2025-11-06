package it.korea.app_bmpc.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    // http 요청을 기억하는 객체
    private RequestCache requestCache = new HttpSessionRequestCache();

    // 응답 전략
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    // setter 를 통한 의존성 주입
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    // 권한 처리 후 실행
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        
        // 이동 경로가 있으면 우선 사용
        setAlwaysUseDefaultTargetUrl(false);

        request.getSession().setMaxInactiveInterval(1800);
        
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // 세션 가져오기
        HttpSession session = request.getSession();

        // 사용자 정보 저장
        session.setAttribute("user", (UserSecureDTO) authentication.getPrincipal());

        // 이전에 저장된 요청 경로가 있을 경우 처리
        if (savedRequest != null) {
            String targetURI = savedRequest.getRedirectUrl();  // 이전 페이지 주소가 들어있다
            
            if (targetURI.contains("error") || targetURI.contains(".well-known") 
                || targetURI.contains("login")) {   // 주소에 error, well-known(데브툴), login 이 있으면 기본 URL 로 대체
                targetURI = getDefaultTargetUrl();
            }

            redirectStrategy.sendRedirect(request, response, targetURI);
        } else {
            redirectStrategy.sendRedirect(request, response, getDefaultTargetUrl());
        }
    }
}