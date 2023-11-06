package com.example.security;

import com.example.exception.RuntimeExceptionWithHTTPCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.requestHeader}")
    private String requestHeader;

    @Value("${security.jwt.secretKey}")
    private String secretKey;

    @Value("${security.jwt.validity}")
    private Long validationInSeconds;

    private final UserDetailsService userDetailsService;

    @PostConstruct
    public void init() {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
    }

    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("authorities", authorities);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validationInSeconds * 1_000);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | AuthenticationException | IllegalArgumentException e) {
            throw new RuntimeExceptionWithHTTPCode(e.getMessage(), HttpStatusCode.valueOf(403));
        }
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(requestHeader);
    }

    public Authentication getAuthentication(String token) {
        String username = getClaims(token).getSubject();
        UserDetails user = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
    }

    private Claims getClaims(String token) {

        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();

        return parser.parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] secretKeyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

}
