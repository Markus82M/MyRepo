package com.example.kyn.dto.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Location {
    private String name;
    private String region;
    private String country;
    private String lat;
    private String lon;
    private String tz_id;
    private String localtime_epoch;
    private String localtime;

}
