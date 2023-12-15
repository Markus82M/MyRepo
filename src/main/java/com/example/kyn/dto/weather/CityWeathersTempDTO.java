package com.example.kyn.dto.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityWeathersTempDTO {
    private Double temp;
    private Double feelsLikeTemp;
    private List<Temp> cities;
    private String tempString;

    public Double tempStringAsDouble() {
        return Double.valueOf(this.getTempString());
    }

}
