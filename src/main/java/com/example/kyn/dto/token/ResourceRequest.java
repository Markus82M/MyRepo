package com.example.kyn.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceRequest {

    private String user; // email address of the functional user
    private String operation;

}
