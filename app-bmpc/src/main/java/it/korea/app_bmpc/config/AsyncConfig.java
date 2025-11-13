package it.korea.app_bmpc.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * SMS 발송, SSE 발송 비동기 처리 하기위한 설정 파일
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 리뷰 요청 SSE를 보낼때 사용할 스레드풀 설정
     * @return
     */
    @Bean(name = "reviewRecommendExecutor")
    public Executor reviewRecommendExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);   // 동시에 3개까지 SSE 전송
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("review-sse-");
        executor.initialize();

        return executor;
    }
}
