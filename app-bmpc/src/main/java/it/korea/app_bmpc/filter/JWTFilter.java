package it.korea.app_bmpc.filter;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.korea.app_bmpc.common.dto.ApiErrorResponse;
import it.korea.app_bmpc.common.utils.JWTUtils;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 이전에 JWT 토큰을 검증하기 위한 필터
 * 검증에 문제가 없다면 인증정보를 SecurityContextHolder 에 저장
 */
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    
    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();

        // 로그인, 회원가입, refresh 토큰, 로그아웃 경로는 화이트리스트로 등록
        if (requestURI.startsWith("/api/v1/login")
                || requestURI.startsWith("/api/v1/register")
                || requestURI.startsWith("/api/v1/refresh")
                || requestURI.startsWith("/api/v1/logout")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // request header 에서 token 찾기
        // header 에 원래 있는 속성인 Authorization 으로 찾는다
        String accessToken = request.getHeader("Authorization");

        if (accessToken == null) {  // accessToken 이 null 이라면 로그인을 안했다는 뜻
            log.error("accessToken is null");
            filterChain.doFilter(request, response);   // 다음 필터로 이동하도록 함
            
            return;   // 자격이 없으니 로그인으로 넘어가도록 함
        }

        try {
            
            if (accessToken.startsWith("Bearer ")) {  // accessToken 이 "Bearer " 로 시작하면 토큰이 들어있다는 뜻
                accessToken = accessToken.substring(7);

                if (!jwtUtils.validateToken(accessToken)) {  // 유효성 체크에 실패했으면 예외 발생
                    throw new IllegalAccessException("유효하지 않은 토큰입니다.");
                }
            }

            // 토큰의 카테고리 검색
            String category = jwtUtils.getCategory(accessToken);

            if (!category.equals("access")) {   // 카테고리 추출 결과가 "access" 가 아니면 예외 발생
                throw new IllegalAccessException("유효하지 않은 토큰입니다.");
            }
        } catch (Exception e) {
            
            // 클라이언트에게 텍스트 응답을 보낸다
            response.setContentType("application/json");   // 던지는 데이터 타입이 json 임을 명시해줘야 함
            JSONObject message = this.getErrorMessage(e.getMessage(), HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getWriter().write(message.toString());
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

            return;   // 함수 종료
        }

        // 인증 성공
        String userId = jwtUtils.getUserId(accessToken);
        String userName = jwtUtils.getUserName(accessToken);
        String userRole = jwtUtils.getUserRole(accessToken);

        // 사용자 조회
        UserEntity user = userRepository.findById(userId).orElse(null);

        // 사용자 삭제 여부 확인
        if (user == null || "Y".equals(user.getDelYn())) {
            sendErrorResponse(response, "해당 계정은 삭제되었습니다. 다른 계정으로 로그인해주세요.", HttpServletResponse.SC_UNAUTHORIZED);
            
            return;
        }

        UserSecureDTO dto = new UserSecureDTO(userId, userName, "", userRole);

        // 시큐리티 세션에 저장
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(dto, null, dto.getAuthorities());    // null 은 비번을 안 넣은것


         /**
         * Spring Security는 기본적으로 SecurityContext를 먼저 확인한다.
         *
         * 이제 LoginFilter(즉, UsernamePasswordAuthenticationFilter를 대체한 필터)가 실행될 때,
         * Security는 이미 SecurityContext에 인증 정보가 있는지부터 확인한다.
         *
         * 만약 SecurityContext에 Authentication이 존재한다면, Security는 이미 로그인된 사용자로 간주한다.
         *
         * 따라서 filterChain.doFilter로 LoginFilter로 넘어가지만 LoginFilter는 로그인 시도를 수행하지 않고 바로 통과(pass) 한다.
         */
        //SecurityContext 생성 및 저장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        SecurityContextRepository repository = new RequestAttributeSecurityContextRepository();
        repository.saveContext(context, request, response);

        // 다음으로 이동
        filterChain.doFilter(request, response);
    }

    private JSONObject getErrorMessage(String message, int status) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resultMsg", message == null ? "Invalid Token" : message);
        jsonObject.put("status", status);

        return jsonObject;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error("E401", message);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(apiErrorResponse);
        
        response.getWriter().write(json);
    }
}

