package it.korea.app_bmpc.common.utils;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 발급을 위한 유틸 클래스 
 */
@Component
@Slf4j
public class JWTUtils {

    private SecretKey secretKey;

    public JWTUtils(@Value("${spring.jwt.secretKey}") String secret) {
        // JWT 토큰을 만들기 위한 비공개 키를 HS256 알고리즘을 통해 생성
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), 
            Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 사용자 아이디, 이름, 권한, 지속시간(분) 을 파라미터로 받는다
    public String createJwt(String category, String userId, 
        String userName, String userRole, Long mins) {

        if (userRole.startsWith("ROLE_")) {  // 이미 권한 앞에 ROLE_이 붙어있다면...
            userRole = userRole.substring(5); // ROLE_ 제거
        }

        return Jwts.builder()
            .claim("category", category)
            .claim("userId", userId)
            .claim("userName", userName)
            .claim("userRole", userRole)
            .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
            .expiration(Timestamp.valueOf(LocalDateTime.now().plusMinutes(mins)))
            .signWith(secretKey)
            .compact();
    }

    // JWT 토큰 유효성 체크
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

            return true;  // exception 이 발생하지 않으면 그냥 true 리턴
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰입니다.");
        }

        return false;
    }

    // 비밀번호 재설정 토큰 발급
    public String createPasswordResetToken(String email) {
        return Jwts.builder()
            .subject("password-reset")
            .claim("email", email)
            .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
            .expiration(Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)))
            .signWith(secretKey)
            .compact();
    }

    // 비밀번해 재설정 토큰 유효성 체크
    public String validatePasswordResetToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // password-reset 이라는 이름으로 발급된 토큰인지 체크
            if (!"password-reset".equals(claims.getSubject())) {
                throw new RuntimeException("비밀번호 재설정 토큰이 아닙니다.");
            }

            return claims.get("email", String.class);

        } catch (SecurityException | MalformedJwtException e) {
            throw new RuntimeException("유효하지 않은 비밀번호 재설정 토큰 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("만료된 비밀번호 재설정 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("지원되지 않는 비밀번호 재설정 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 비밀번호 재설정 토큰입니다.");
        }
    }

    // 토큰 카테고리 추출
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token)
            .getPayload()
            .get("category", String.class);
    }

    // 토큰에서 아이디 추출
    public String getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userId", String.class);
    }

    // 토큰에서 이름 추출
    public String getUserName(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userName", String.class);
    }

    // 토큰에서 권한 추출
    public String getUserRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userRole", String.class);
    }

    // 토큰 유효시간 체크
    public boolean getExpired(String token) {

        // 현재 시간이 유효시간보다 이전인지 체크
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration()
            .before(Timestamp.valueOf(LocalDateTime.now()));
    }
}

