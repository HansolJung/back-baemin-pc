package it.korea.app_bmpc.order.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderSseService {

    // 사용자별 SSE 연결을 보관하는 Map
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 구독하기
     * @param userId 사용자 아이디
     * @return
     */
    public SseEmitter subscribe(String userId) {
        log.info(userId + "가 구독하러 들어옴");
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);  // 제한시간 30분

        emitters.put(userId, emitter);
        emitter.onCompletion(()-> emitters.remove(userId));
        emitter.onTimeout(()-> emitters.remove(userId));
        emitter.onError((e)-> emitters.remove(userId));

        return emitter;
    }

    /**
     * 주문 상태 변경 시 사용자에게 알림 전송
     * @param userId 사용자 아이디
     * @param message 메세지
     */
    public void sendEvent(String userId, String message) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("order-status").data(message));
            } catch (IOException e) {
                emitters.remove(userId);
                try {
                    emitter.complete();
                } catch (Exception ignore) {}  // 로그 출력 방지
            }
        }
    }

    /**
     * 사용자와 SSE 연결이 되는지 확인
     * @param userId 사용자 아이디
     * @return
     */
    public boolean isConnected(String userId) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                // ping 이벤트를 보낼 수 있으면 연결된 것
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));

                return true;
            } catch (IOException e) {

                emitters.remove(userId);
                try {
                    emitter.complete();
                } catch (Exception ignore) {}  // 로그 출력 방지
            }
        }

        return false;
    }
}
