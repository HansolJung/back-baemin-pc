package it.korea.app_bmpc.order.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import it.korea.app_bmpc.order.service.OrderSseAuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderSseController {

    private final OrderSseAuthService orderSseAuthService;

    // /**
    //  * SSE 구독하기
    //  * @param user 로그인한 사용자
    //  * @return
    //  */
    // @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public SseEmitter subscribe() {

    //     System.out.println("들어옴!!!!!!!!!");

    //     // 인증 정보 확인
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
    //         throw new AccessDeniedException("인증되지 않은 사용자입니다.");
    //     }
    //     UserSecureDTO user = (UserSecureDTO) authentication.getPrincipal();

    //     // 권한 확인
    //     if (user.getAuthorities() == null ||
    //         user.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
    //         throw new AccessDeniedException("접근 권한이 없습니다.");
    //     }

    //     return orderSseService.subscribe(user.getUserId());
    // }

    /**
     * SSE 구독하기
     * 로그인 직후에도 바로 user 정보가 null이 되지 않도록 SecurityContext에서 직접 추출
     */
    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("token") String token) {
        // 서비스에서 모든 인증/권한/구독 처리
        System.out.println("구독하러 들어옴");
        return orderSseAuthService.subscribeWithToken(token);
    }
}
