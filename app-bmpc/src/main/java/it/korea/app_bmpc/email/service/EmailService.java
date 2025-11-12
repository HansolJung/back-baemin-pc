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
     * 사용자 아이디 이메일로 발송하기
     * @param email 이메일 주소
     * @param userId 사용자 아이디
     */
    @Async
    public void sendUserIdEmail(String email, String userId) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[배달의민족] 아이디 안내");

            String content = """
                <!DOCTYPE html>
                <html lang="ko">
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:'Apple SD Gothic Neo', Arial, sans-serif;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width:600px; margin: 20px auto; background-color:#ffffff; border-radius:12px; box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                    <tr>
                        <td style="padding:40px 30px 20px 30px; text-align:center;">
                        <h1 style="color:#2ac1bc; font-size:28px; margin-bottom:10px;">배달의민족</h1>
                        <p style="font-size:18px; color:#333; margin:0;">아이디 안내</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:10px 30px 30px 30px; color:#333;">
                        <p style="font-size:16px; line-height:1.6;">안녕하세요 👋 고객님,<br>
                        찾으시려는 아이디는 아래와 같습니다.</p>

                        <div style="text-align:center; margin:25px 0;">
                            <div style="display:inline-block; background-color:#2ac1bc; color:#fff; padding:15px 40px; border-radius:8px; font-size:18px; font-weight:bold;">
                                %s
                            </div>
                        </div>

                        <p style="font-size:14px; color:#666; line-height:1.5;">로그인 페이지에서 위 아이디로 로그인해주세요.</p>

                        <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">

                        <p style="font-size:13px; color:#999; line-height:1.4;">이 이메일은 발신 전용입니다.<br>
                        궁금한 점이 있으시면 <a href="https://ceo.baemin.com/cscenter" style="color:#2ac1bc; text-decoration:none;">고객센터</a>로 문의해주세요.</p>

                        <p style="font-size:12px; color:#bbb; text-align:center; margin-top:40px;">© 2025 배달의민족. All rights reserved.</p>
                        </td>
                    </tr>
                    </table>
                </body>
                </html>
                """.formatted(userId);

            helper.setText(content, true);
            javaMailSender.send(mimeMessage);

            log.info("이메일 주소 {}로 아이디 안내 메일 전송 성공", email);

        } catch (Exception e) {
            log.error("이메일 주소 {}로 아이디 안내 메일 전송 실패: {}", email, e.getMessage());
            throw new RuntimeException("아이디 안내 메일 발송 실패");
        }
    }

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
            helper.setSubject("[배달의민족] 비밀번호 재설정 안내");

            String resetUrl = "http://localhost:4000/reset-password?token=" + token;
            String content = """
                <!DOCTYPE html>
                <html lang="ko">
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:'Apple SD Gothic Neo', Arial, sans-serif;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width:600px; margin: 20px auto; background-color:#ffffff; border-radius:12px; box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                    <tr>
                        <td style="padding:40px 30px 20px 30px; text-align:center;">
                        <h1 style="color:#2ac1bc; font-size:28px; margin-bottom:10px;">배달의민족</h1>
                        <p style="font-size:18px; color:#333; margin:0;">비밀번호 재설정 안내</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:10px 30px 30px 30px; color:#333;">
                        <p style="font-size:16px; line-height:1.6;">안녕하세요 👋 고객님,<br>
                        비밀번호를 잊으셨나요?<br>
                        아래 버튼을 눌러 새 비밀번호를 설정해주세요.</p>

                        <div style="text-align:center; margin:30px 0;">
                            <a href="%s" style="background-color:#2ac1bc; color:#ffffff; text-decoration:none; padding:15px 30px; border-radius:8px; font-weight:bold; display:inline-block;">🔒 비밀번호 재설정하기</a>
                        </div>

                        <p style="font-size:14px; color:#666; line-height:1.5;">해당 링크는 <strong>30분 동안만 유효</strong>하며, 이후에는 만료됩니다.</p>

                        <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">

                        <p style="font-size:13px; color:#999; line-height:1.4;">이 이메일은 발신 전용입니다.<br>
                        궁금한 점이 있으시면 <a href="https://ceo.baemin.com/cscenter" style="color:#2ac1bc; text-decoration:none;">고객센터</a>로 문의해주세요.</p>

                        <p style="font-size:12px; color:#bbb; text-align:center; margin-top:40px;">© 2025 배달의민족. All rights reserved.</p>
                        </td>
                    </tr>
                    </table>
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
