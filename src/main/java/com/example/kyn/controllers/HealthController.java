package com.example.kyn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.example.kyn.dto.GeneralResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/health")
//@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class HealthController {

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponseDTO> callTest() {

        GeneralResponseDTO generalResponseDTO = GeneralResponseDTO.builder().currentTime(LocalDateTime.now()).status("OK").build();
        log.info(objectToString(generalResponseDTO));

        return ResponseEntity.ok().body(generalResponseDTO);
    }

    public String objectToString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            return null;
        }
    }

}
