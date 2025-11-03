package it.korea.app_bmpc.order.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.korea.app_bmpc.common.utils.JWTUtils;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSseAuthService {

    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;
    private final OrderSseService orderSseService;

    /**
     * SSE 구독 처리
     * @param token URL 파라미터로 전달된 JWT
     * @return SseEmitter
     */
    public SseEmitter subscribeWithToken(String token) {

        System.out.println("토큰!!! " + token);

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        if (token == null || !jwtUtils.validateToken(token)) {
            System.out.println("SSE: 인증 실패, 연결 종료");
            emitter.complete();
            return emitter;
        }

        // 토큰에서 사용자 정보 추출
        String userId = jwtUtils.getUserId(token);
        UserEntity user = userRepository.findById(userId)
            .orElse(null);

        if (user == null || "Y".equals(user.getDelYn())) {
            System.out.println("SSE: 유효하지 않은 사용자, 연결 종료");
            emitter.complete();
            return emitter;
        }

        // ROLE_USER 권한 확인
        if (user.getRole() == null || !"USER".equals(user.getRole().getRoleId())) {
            System.out.println("SSE: 권한 없음, 연결 종료");
            emitter.complete();

            return emitter;
        }

        // SSE 구독 등록
        return orderSseService.subscribe(userId);
    }
}
