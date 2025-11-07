package it.korea.app_bmpc.sms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsService {

    private static DefaultMessageService messageService;
    
    @Value("${coolsms.api.number}")
    private  String fromNumber;

    @Value("${coolsms.api.key}")
    String apiKey;

    @Value("${coolsms.api.secret}")
    String apiSecret;

    /**
     * 점주에게 주문 내용 SMS 발송하기
     * @param toNumber 점주 전화번호
     * @param orderSummary 주문 요약
     */
    @Async
    public void sendToOwner(String toNumber, String orderSummary) {
        try {

            if (messageService == null) {  // 생성자로 주입 받으면 빌드시 오류가 발생. 이렇게 선언한다.
                messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
            }

            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(toNumber);
            message.setText("[배달의민족]\n새 주문이 들어왔습니다.\n\n주문내역)\n" + orderSummary);

            messageService.send(message);

            log.info("점주 번호 {}로 문자 전송 성공", toNumber);
        } catch (Exception e) {
            log.error("점주 번호 {}로 문자 전송 실패. {}", toNumber, e.getMessage());
        }
    }
}
