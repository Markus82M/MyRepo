package com.example.kyn.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.kyn.dto.token.TokenRequest;
import com.example.kyn.dto.token.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class TokenService {
    private final String tokenSecret;
    private String getTokenKey;
    private String tokenExpiryTime;
    private CacheManager cacheManager;

    public TokenService(final @Value("${token.api.secret}") String tokenSecret,
                        final @Value("${token.api.key}") String getTokenKey,
                        final @Value("${token.api.expiry}") String tokenExpiryTime,
                        CacheManager cacheManager) {
        this.tokenSecret = tokenSecret;
        this.getTokenKey = getTokenKey;
        this.tokenExpiryTime = tokenExpiryTime;
        this.cacheManager = cacheManager;
    }

    public TokenResponse getToken(TokenRequest request, String keyHeader) {

        TokenResponse tokenResponse = TokenResponse.builder().build();
        if (!keyHeader.equalsIgnoreCase(getTokenKey)) {
            log.info("Incorrect key header.");
            tokenResponse.setErrorMessage("Invalid key header");
            tokenResponse.setValidToken(false);
            return tokenResponse;
        }

        String userOperationKey = request.getUser() + request.getOperation();
        Cache tokensCache = cacheManager.getCache("tokens");

        String id = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + (1000 * Integer.parseInt(tokenExpiryTime))); // life-time in millis

        try {
            Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(tokenSecret));
            String token = JWT.create()
                    .withIssuer("WeatherTokenGenerator")
                    .withClaim("jti", id)
                    .withClaim("iat", now)
                    .withClaim("operation", request.getOperation())
                    .withClaim("user", request.getUser())
                    .withClaim("aud", "WeatherApp")
                    .withExpiresAt(exp)
                    .sign(algorithm);

            tokensCache.evictIfPresent(userOperationKey);
            tokensCache.put(userOperationKey, token);

            tokenResponse.setToken(token);
            tokenResponse.setValidToken(true);
            return tokenResponse;

        } catch (JWTCreationException exception) {
            log.info("Exception occured:{}", exception.getMessage());
            tokenResponse.setErrorMessage("An error occured when creating JWT.");
            tokenResponse.setValidToken(false);
            return tokenResponse;
        }

    }

    public TokenResponse validateToken(String authorization) {

        TokenResponse tokenResponse = TokenResponse.builder().build();
        if (authorization.isEmpty()) {
            tokenResponse.setErrorMessage("Missing Authorization header");
            tokenResponse.setValidToken(false);
            return tokenResponse;
        }
        String token = "", userOperationKey = "";
        try {
            String[] tokenParts = authorization.split(" ");
            token = tokenParts[1]; // the token after Bearer
            Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(tokenSecret)); //use more secure key
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("WeatherTokenGenerator")
                    .withAudience("WeatherApp")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            userOperationKey = jwt.getClaim("user").asString() + jwt.getClaim("operation").asString();
        } catch (JWTVerificationException exception) {
            log.info("Check Token exception occurred:{}", exception.getMessage());
            tokenResponse.setErrorMessage("Invalid token.Might be expired. Please call again /getToken");
            tokenResponse.setValidToken(false);
            return tokenResponse;
        }

        Cache tokensCache = cacheManager.getCache("tokens");
        if (tokensCache != null && tokensCache.get(userOperationKey) != null) {
            String tokenFromCache = (String) tokensCache.get(userOperationKey).get();
            if (token.equals(tokenFromCache)) {
                // success path
                log.info("Valid authorization token");
                tokenResponse.setValidToken(true);
                return tokenResponse;
            } else {
                log.info("Token used is not the equal with token initially generated");
                tokenResponse.setErrorMessage("Token used is not originally generated for the user from request");
                tokenResponse.setValidToken(false);
                return tokenResponse;
            }
        } else {
            log.info("Tokens cache missing. Probably the application was restarted");
            tokenResponse.setValidToken(true);
            return tokenResponse;
            // to be decided if we should continue or return an error. Token already passed validation for signature
            // tokenResponse.setErrorMessage("Issues when checking token. Please call again /getToken");
            // tokenResponse.setValidToken(false);
        }

    }
}
