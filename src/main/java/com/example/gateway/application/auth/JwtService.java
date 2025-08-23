package com.example.gateway.application.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    private final String issuer;
    private final long expiresMinutes;
    private final Key key;

    public JwtService(
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expires-minutes}") long expiresMinutes
    ) {
        this.issuer = issuer;
        this.expiresMinutes = expiresMinutes;
        this.key = buildKey(secret);
        logger.info("🔧 JWT 서비스 초기화 완료 - issuer: {}, expiresMinutes: {}", issuer, expiresMinutes);
    }

    public String issueToken(String subjectUserId, Map<String, Object> extraClaims) {
        logger.info("🔑 JWT 토큰 발급 시작 - userId: {}, extraClaims 개수: {}", subjectUserId, extraClaims != null ? extraClaims.size() : 0);
        
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMinutes * 60);

        var builder = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(subjectUserId)        
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp));

        if (extraClaims != null && !extraClaims.isEmpty()) {
            logger.info("📝 JWT 토큰에 추가 클레임 설정 - claims: {}", extraClaims.keySet());
            builder.addClaims(extraClaims);
        }

        String token = builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        logger.info("✅ JWT 토큰 발급 완료 - userId: {}, 만료시간: {}", subjectUserId, exp);
        return token;
    }

    private static Key buildKey(String secret) {
        byte[] keyBytes = tryDecodeSecret(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] tryDecodeSecret(String secret) {
        String s = secret.trim();

        if (s.length() % 2 == 0 && s.matches("^[0-9a-fA-F]+$")) {
            return hexToBytes(s);
        }

        try {
            return Decoders.BASE64.decode(s);
        } catch (IllegalArgumentException ignore) {
        }

        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    public Claims parse(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)     
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractCompany(String token) {
        try {
            Claims claims = parse(token);
            return claims.get("company", String.class);
        } catch (JwtException e) {
            logger.warn("JWT 토큰에서 company 정보 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}
