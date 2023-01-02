package com.hau.huylong.graduation_proejct.common.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {
    //5p
    public final long JWT_TOKEN_VALIDITY = 10 * 30 * 30;
    public String TOKEN_SECRET = "Siten@AllRights";
    public final String TOKEN_ISSUER = "http://siten.vn/";

    @PostConstruct
    protected void init() {
        TOKEN_SECRET = Base64.getEncoder().encodeToString(TOKEN_SECRET.getBytes());
    }

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(TOKEN_SECRET).parseClaimsJws(token).getBody();
    }

    // check if the token has expired
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // generate token for user
    public String generateToken(String username, Set<String> authorities,
                                Integer uid, String fullname, String avatar, String type) {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        claims.put("authorities", authorities);
        claims.put("uid", uid);
        claims.put("fullname", fullname);
        claims.put("avatar", avatar);
        claims.put("type", type);
        return doGenerateToken(claims, headers, username);
    }

    private String doGenerateToken(Map<String, Object> claims, Map<String, Object> headers, String subject) {
        return Jwts.builder().setClaims(claims)
                .setSubject(subject)
                .setHeader(headers)
                .setIssuer(TOKEN_ISSUER)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(JWT_TOKEN_VALIDITY * 1000)))
                .signWith(SignatureAlgorithm.HS512, TOKEN_SECRET).compact();
    }

    public Boolean validateToken(String token, User user) {
        final String username = getUsernameFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(TOKEN_SECRET).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
