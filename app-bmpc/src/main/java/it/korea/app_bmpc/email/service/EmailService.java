package it.korea.app_bmpc.email.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendMimeMessage() {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 메일을 받을 수신자 설정
            mimeMessageHelper.setTo("solsol3638@naver.com");
            // 메일의 제목 설정
            mimeMessageHelper.setSubject("html 적용 테스트 메일 제목");

            // html 문법 적용한 메일의 내용
            String content = """
                    <!DOCTYPE html>
                    <html xmlns:th="http://www.thymeleaf.org">
                                        
                    <body>
                    <div style="margin:100px;">
                        <h1> 테스트 메일 </h1>
                        <br>
                                        
                                        
                        <div align="center" style="border:1px solid black;">
                            <h3> 테스트 메일 내용 </h3>
                        </div>
                        <br/>
                    </div>
                                        
                    </body>
                    </html>
                    """;
            
            // 메일의 내용 설정
            mimeMessageHelper.setText(content, true);

            javaMailSender.send(mimeMessage);

            log.info("메일 발송 성공!");
        } catch (Exception e) {
            log.info("메일 발송 실패!");
            throw new RuntimeException(e);
        }
    }
}
