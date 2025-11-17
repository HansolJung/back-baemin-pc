package it.korea.app_bmpc.order.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.order.event.OrderStatusChangedEvent;
import it.korea.app_bmpc.order.repository.OrderRepository;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 자동 취소 스케줄러
 * 주문 후 5분이 지나도 점주가 수락하지 않으면 자동으로 '주문취소' 상태로 변경
 * 주문자에게 주문이 취소됐다고 SSE 알림 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelScheduler {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 스케줄러 동시 실행 방지용 lock
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 주문한지 5분이 넘었지만 수락이 안된 주문들 자동 취소하기
     */
    @Scheduled(initialDelay = 60000, fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void cancelOrderBySchedule() {

        int count = 0;
        boolean locked = false;

        try {
            locked = lock.tryLock();
            if (!locked) {
                return;
            }

            log.info("주문 자동취소 스케줄러 시작");

            LocalDateTime time = LocalDateTime.now().minusMinutes(5);
            List<OrderEntity> orderEntityList = orderRepository.findByStatusAndOrderDateBefore("주문완료", time);

            for (OrderEntity orderEntity : orderEntityList) {
                try {

                    int updated = 
                        orderRepository.updateStatusWithScheduler(orderEntity.getOrderId(), "주문완료", "주문취소");

                    // 만약 updated 의 값이 0이라면 점주가 이미 해당 주문의 상태를 변경했다는 뜻.
                    if (updated == 0) {
                        log.info("주문 자동취소 실패 - 이미 처리된 주문 아이디: {}", orderEntity.getOrderId());

                        continue;
                    }


                    UserEntity userEntity = orderEntity.getUser();

                    // 사용자가 존재하는 경우에만 보유금 원복
                    if (userEntity != null && "N".equals(userEntity.getDelYn())) {
                        int originalDeposit = userEntity.getDeposit();
                        int totalPrice = orderEntity.getTotalPrice();

                        userEntity.setDeposit(originalDeposit + totalPrice);  // 주문이 취소되었기 때문에 보유금 원복

                        userRepository.save(userEntity);

                        String message = "주문이 취소됐습니다.";
                        eventPublisher.publishEvent(new OrderStatusChangedEvent(userEntity.getUserId(), message));
                    }

                    count++;
                } catch (Exception e) {
                    log.error("주문 자동취소 실패 - 주문 아이디: {}", orderEntity.getOrderId());
                }
            }

            log.info("주문 자동취소 스케줄러 완료 - 처리건수: {}", count);

        } catch (Exception e) {
            log.error("주문 자동취소 스케줄러 실행 중 예외 발생", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
