package it.korea.app_bmpc.order.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.order.service.OrderSseAuthService;
import lombok.RequiredArgsConstructor;

@Tag(name = "SSE API", description = "점주가 주문 수락시 주문자에게 알림을 보내는 SSE API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderSseController {

    private final OrderSseAuthService orderSseAuthService;

    /**
     * SSE 구독하기
     * @param token JWT 값
     * @return
     */
    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 구독하기")
    public SseEmitter subscribe(@RequestParam("token") String token) {
        // 서비스에서 모든 인증/권한/구독 처리
        return orderSseAuthService.subscribeWithToken(token);
    }
}
