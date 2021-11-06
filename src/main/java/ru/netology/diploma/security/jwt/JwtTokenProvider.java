package ru.netology.diploma.security.jwt;

import io.jsonwebtoken.*;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.netology.diploma.security.jwt.service.JwtBlackListService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expired}")
    private Long validInMilliseconds;

    @Value("${jwt.token.header}")
    private String tokenHeader;

    private final JwtBlackListService jwtBlackListService;

    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtTokenProvider(@Qualifier("jwtUserDetailService") UserDetailsService userDetailsService, @Qualifier("jwtBlackListServiceImpl") JwtBlackListService jwtBlackListService) {
        this.userDetailsService = userDetailsService;
        this.jwtBlackListService = jwtBlackListService;
    }

    @PostConstruct
    public void init() {
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret) //
                .compact();

        return token;
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(tokenHeader);
    }

    public boolean isTokenValid(String token) {
        try {
            if (jwtBlackListService.findByTokenEquals(token) != null)
                return false;

            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            //return false;
            throw new JwtAuthenticationException("JWT token is not valid");
        }
    }

    public void addTokenToBlackList(String token) {
        if (jwtBlackListService.findByTokenEquals(token) == null)
            jwtBlackListService.save(token);
    }
}
