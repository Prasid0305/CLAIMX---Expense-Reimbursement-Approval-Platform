package com.company.claimx.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    //
    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {

        Date expiryDate = new Date(new Date().getTime() + expiration);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    /**
     *
     * @param token
     * @return
     */
    public String extractEmail(String token){
        return extractClaim(token, claims->claims.getSubject());
    }

    public String extractRole(String token){
        return extractClaim(token, claims->claims.get("role", String.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    //
    public< T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email){
        String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }




}
