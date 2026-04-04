package com.falcon.booking.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret-key}")
    String secretKey;

    @Value("${app.jwt.issuer}")
    String issuerName;

    @Value("${app.jwt.expiration-ms}")
    Long expirationMillis;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm)
                .withIssuer(issuerName)
                .build();
    }

    public String generateToken(JwtPayload payload) {
        return JWT.create()
                .withSubject(payload.userId().toString())
                .withClaim("roles", payload.roles())
                .withClaim("email", payload.email())
                .withIssuer(this.issuerName)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + this.expirationMillis))
                .sign(algorithm);
    }

    public JwtPayload extractPayload(String token) {
        DecodedJWT decodedJWT = verifier.verify(token);

        Long userId = Long.parseLong(decodedJWT.getSubject());
        String email = decodedJWT.getClaim("email").asString();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

        return new JwtPayload(userId, email, roles);
    }

}
