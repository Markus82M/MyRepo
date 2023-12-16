package com.example.kyn.dto.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityWeathersTempDTO {

    private Double temp;
    private Double feelsLikeTemp;
    private List<Temp> cities;

}
