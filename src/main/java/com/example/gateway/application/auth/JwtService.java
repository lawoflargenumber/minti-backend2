package com.example.gateway.application.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {

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
    }

    public String issueToken(String subjectUserId, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMinutes * 60);

        var builder = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(subjectUserId)        
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp));

        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
}
