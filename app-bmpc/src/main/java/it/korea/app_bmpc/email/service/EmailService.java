package it.korea.app_bmpc.email.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    /**
     * 비밀번호 재설정 링크 이메일로 발송하기
     * @param email 이메일 주소
     * @param token 비밀번호 재설정 토큰
     */
    @Async
    public void sendPasswordResetEmail(String email, String token) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("비밀번호 재설정 안내");

            String resetUrl = "http://localhost:4000/reset-password?token=" + token;
            String content = """
                    <!DOCTYPE html>
                    <html>
                    <body>
                    <div style="margin:50px;">
                        <h2>비밀번호 재설정 안내</h2>
                        <p>아래 링크를 클릭하여 새 비밀번호를 설정해주세요.</p>
                        <a href="%s">비밀번호 재설정</a>
                        <p>해당 링크는 30분 동안 유효합니다.</p>
                    </div>
                    </body>
                    </html>
                """.formatted(resetUrl);

            helper.setText(content, true);
            javaMailSender.send(mimeMessage);

            log.info("이메일 주소 {}로 비밀번호 재설정 링크 전송 성공", email);

        } catch (Exception e) {
            log.info("이메일 주소 {}로 비밀번호 재설정 링크 전송 실패. {}", email, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
