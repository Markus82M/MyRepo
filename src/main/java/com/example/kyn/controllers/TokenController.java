package com.example.kyn.controllers;

import com.example.kyn.services.TokenService;
import com.example.kyn.dto.token.TokenRequest;
import com.example.kyn.dto.token.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/token")
public class TokenController {

    private TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/getToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request, @RequestHeader("key") String keyHeader) {

        TokenResponse tokenResponse = tokenService.getToken(request, keyHeader);

        if (!tokenResponse.isValidToken()) {
            return ResponseEntity.badRequest().body(tokenResponse);
        } else {
            return ResponseEntity.ok().body(tokenResponse);
        }
    }

}
