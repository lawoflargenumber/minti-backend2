package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    // ⬅️ JwtAuthFilter를 생성자 파라미터로 주입받아 사용
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  JwtAuthFilter jwtAuthFilter) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .exceptionHandling(e -> e.authenticationEntryPoint((ex, err) -> {
                ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return ex.getResponse().setComplete();
            }))

            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .authorizeExchange(ex -> ex
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/auth/**").permitAll()
                .pathMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**",
                    "/actuator/**"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .build();
    }
}
