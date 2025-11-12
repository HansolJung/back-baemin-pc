package it.korea.app_bmpc.config;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import it.korea.app_bmpc.common.utils.JWTUtils;
import it.korea.app_bmpc.filter.CustomLogoutFilter;
import it.korea.app_bmpc.filter.JWTFilter;
import it.korea.app_bmpc.filter.LoginFilter;
import it.korea.app_bmpc.user.repository.UserRepository;
import it.korea.app_bmpc.user.service.UserServiceDetails;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserServiceDetails userServiceDetails;
    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;

    // 시큐리티 무시하기
    // WebSecurityCustomizer 는 추상화 메서드를 하나만 가지는 함수형 인터페이스기 때문에,
    // 추상화 메서드 customize() 를 람다식으로 구현해서 리턴할 수 있다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web
            .ignoring()
            .requestMatchers("/static/imgs/**", "/static/img/**")    // 우리가 직접 만든 외부 파일 연동 링크
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
            // 마지막 명령어는 스프링 리소스 관련 처리. 아래와 같은 모든 경로들을 처리한다.
            /*
             * 1. classpath:/META-INF/resources/    <- 라이브러리 리소스 폴더
             * 2. classpath:/resources/
             * 3. classpath:/static/
             * 4. classpath:/public/   
             */
    }

    // 보안 처리
    // Security 6 의 특징: 메서드 파라미터를 전부 함수형 인터페이스로 처리한다
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationConfiguration configuration = 
            http.getSharedObject(AuthenticationConfiguration.class);
        
        // LoginFilter 에서 인증처리하기 위한 매니저 생성
        AuthenticationManager manager = this.authenticationManager(configuration);

        LoginFilter loginFilter = new LoginFilter(manager, jwtUtils);
        loginFilter.setFilterProcessesUrl("/api/v1/login");

        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors
                .configurationSource(this.configurationSource()))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)  // 토큰 발행으로 로그인할것이기 때문에 기존 form 로그인 방식은 끈다
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                .requestMatchers("/api/v1/login/**").permitAll()
                .requestMatchers("/api/v1/logout/**").permitAll()
                .requestMatchers("/api/v1/refresh/**").permitAll()
                .requestMatchers("/api/v1/register/**").permitAll()
                .requestMatchers("/api/v1/recovery/**").permitAll() 
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/sse/subscribe").authenticated()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/.well-known/**").permitAll()   // chrome dev-tool 에러 처리
                .requestMatchers("/favicon.ico").permitAll()  // favicon 에러 처리
                .requestMatchers("/img/**").permitAll()  // 대체 이미지 경로는 인증 처리하지 않음
                .requestMatchers(HttpMethod.GET, "/api/v1/store/my").authenticated()   // 내 가게 보기는 인증 처리
                .requestMatchers(HttpMethod.GET, "/api/v1/store/**").permitAll()   // GET 방식인 /api/v1/store 는 모두 허용
                .requestMatchers(HttpMethod.GET, "/api/v1/menu/**").permitAll()   // GET 방식인 /api/v1/menu 는 모두 허용
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN")   // ADMIN 권한을 가지고 있어야만 허용
                .anyRequest().authenticated())
            .addFilterBefore(new JWTFilter(jwtUtils, userRepository), LoginFilter.class)    // 로그인 필터를 사용하기 전에 JWTFilter 를 먼저 사용하겠다는 뜻
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)   //  UsernamePasswordAuthenticationFilter 대신 LoginFilter 를 사용하라는 뜻
            .addFilterBefore(new CustomLogoutFilter(jwtUtils), LogoutFilter.class)   // 로그아웃 필터를 사용하기 전에 CustomLogoutFilter 를 먼저 사용하겠다는 뜻
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // 세션 유지 하지 않는다는 뜻 (세션 로그인이 아니기 때문)
            .logout(Customizer.withDefaults())   // Spring Security에서 로그아웃 기능을 기본 설정으로 활성화하는 설정
            .exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(   // 인증받지 않은 API 요청에 대해서는 401 응답 (리다이렉트 하지 않음)
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                .defaultAccessDeniedHandlerFor(   // 인증은 받았으나 권한이 없는 API 요청에 대해서는 403 응답 (리다이렉트 하지 않음)
                    new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN), 
                    PathPatternRequestMatcher.withDefaults().matcher("/api/**")));

        return http.build();
    }

    // auth provider 생성해서 전달 > 사용자가 만든 것을 전달한다.
    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userServiceDetails);
        provider.setPasswordEncoder(bcyPasswordEncoder());

        return provider;
    }

    // 패스워드 암호화 객체 설정
    @Bean
    public PasswordEncoder bcyPasswordEncoder() {
        // 단방향 암호화 방식. 복호화 없음. 값 비교는 가능.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 헤더 설정
        config.setAllowedHeaders(List.of("*"));

        // 메서드 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 경로 설정
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4000"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);  // 1시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}