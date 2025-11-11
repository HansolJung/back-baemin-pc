package it.korea.app_bmpc.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFailureHanlder implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
                
        String encoded = URLEncoder.encode("아이디 또는 비밀번호를 확인해주십시오.", Charset.forName("UTF-8"));
        response.sendRedirect("/user/login/error?msg=" + encoded);
    }
}
