package it.korea.app_bmpc.order.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.order.event.ReviewRecommendEvent;
import it.korea.app_bmpc.order.repository.OrderRepository;
import it.korea.app_bmpc.order.service.OrderSseService;
import it.korea.app_bmpc.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 리뷰 요청 스케줄러
 * 주문이 배달완료로 변경된 후 1시간이 지난 주문들 가져오기
 * 해당 주문을 한 주문자에게 리뷰를 작성해달라고 SSE 알림 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRecommendScheduler {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderSseService orderSseService;

    // 스케줄러 동시 실행 방지용 lock
    private final ReentrantLock lock = new ReentrantLock();

    // 이미 리뷰 요청을 보낸 주문의 아이디를 저장하는 Set
    private final Set<Integer> orderIdList = ConcurrentHashMap.newKeySet();

    /**
     * 배달완료 후 1시간 지난 주문자에게 리뷰 요청 SSE 전송하기
     */
    //@Scheduled(fixedRate = 600000) // 10분마다 실행
    @Scheduled(initialDelay = 60000, fixedRate = 60000)  // 1분마다 실행. 추후 10분으로 교체해야함.
    @Transactional(readOnly = true)
    public void sendReviewRequest() {
        boolean locked = false;
        int count = 0;

        try {
            locked = lock.tryLock();
            if (!locked) {
                return;
            }

            log.info("리뷰 요청 스케줄러 시작");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.minusHours(1).minusMinutes(5); 
            LocalDateTime startTime = now.minusHours(2).minusMinutes(5); 

            List<OrderEntity> orderEntityList = orderRepository.findByStatusAndOrderDateBetween("배달완료", startTime, endTime);

            for (OrderEntity orderEntity : orderEntityList) {
                int orderId = orderEntity.getOrderId();

                // 이미 리뷰 요청을 보낸 주문은 건너뛰기
                if (orderIdList.contains(orderId)) {
                    continue;
                }

                try {
                    UserEntity userEntity = orderEntity.getUser();
                    if (userEntity != null && "N".equals(userEntity.getDelYn())) {
                        String message = "배달 음식은 잘 드셨나요?\n리뷰를 남겨주세요.";

                        if (orderSseService.isConnected(userEntity.getUserId())) {
                            eventPublisher.publishEvent(new ReviewRecommendEvent(userEntity.getUserId(), message));
                            count++;

                            // SSE 발송을 완료한 주문의 아이디는 맵에 저장
                            orderIdList.add(orderId);
                        } else {
                            log.info("SSE 미연결 상태 - 사용자 아이디: {}", userEntity.getUserId());
                        }
                    }
                } catch (Exception e) {
                    log.error("리뷰 요청 전송 실패 - 주문 아이디: {}", orderId);
                }
            }

            log.info("리뷰 요청 스케줄러 완료 - 전송건수: {}", count);

            // 2시간 이상 지난 주문은 Set에서 제거하기
            try {
                List<OrderEntity> oldOrderEntityList = orderRepository.findByStatusAndOrderDateBefore("배달완료", startTime);
                Set<Integer> oldOrderIdList = oldOrderEntityList.stream().map(OrderEntity::getOrderId).collect(Collectors.toSet());

                orderIdList.removeAll(oldOrderIdList);
            } catch (Exception e) {
                log.warn("2시간 이상 지난 주문의 아이디 정보를 Set에서 정리 중 오류 발생", e);
            }

        } catch (Exception e) {
            log.error("리뷰 요청 스케줄러 실행 중 예외 발생", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
