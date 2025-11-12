package it.korea.app_bmpc.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.korea.app_bmpc.common.utils.JWTUtils;
import it.korea.app_bmpc.email.service.EmailService;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRecoveryService {

    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일로 사용자 아이디 발송하기
     * @param email 이메일
     */
    public void sendUserId(String email) {

        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 사용자가 존재하지 않습니다."));

        emailService.sendUserIdEmail(email, user.getUserId());
    }
    
    /**
     * 이메일로 비밀번호 재설정 링크 발송하기
     * @param email 이메일
     */
    public void sendPasswordResetLink(String userId, String email) {

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(()-> new RuntimeException("잘못된 아이디 혹은 이메일입니다."));

        // 가입했었던 이메일과 요청 이메일이 다를 경우...
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("잘못된 아이디 혹은 이메일입니다.");
        }

        // 비밀번호 재설정용 JWT 생성
        String token = jwtUtils.createPasswordResetToken(email);
        emailService.sendPasswordResetEmail(email, token);
    }

    /**
     * 비밀번호 재설정하기
     * @param token 비밀번호 재설정 토큰
     * @param newPassword 새로운 비밀번호
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {

        // 비밀번호 재설정 토큰 검증
        String email = jwtUtils.validatePasswordResetToken(token);

        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 사용자가 존재하지 않습니다."));

        user.setPasswd(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
