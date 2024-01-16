package com.example.kyn.dto.cache;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CacheResponse {

    private String message;
    private boolean cacheCleared;
}
