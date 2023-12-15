package com.example.kyn.dto.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponseDTO {
    private Location location;
    private CurrentInfo current;
    private ErrorMessage errorMessage;

    /*
    {
    "location": {
        "name": "London",
        "region": "City of London, Greater London",
        "country": "United Kingdom",
        "lat": 51.52,
        "lon": -0.11,
        "tz_id": "Europe/London",
        "localtime_epoch": 1651583670,
        "localtime": "2022-05-03 14:14"
    },
    "current": {
        "last_updated_epoch": 1651579200,
        "last_updated": "2022-05-03 13:00",
        "temp_c": 13.0,
        "temp_f": 55.4,
        "is_day": 1,
        "condition": {
            "text": "Overcast",
            "icon": "//cdn.weatherapi.com/weather/64x64/day/122.png",
            "code": 1009
        },
        "wind_mph": 4.3,
        "wind_kph": 6.8,
        "wind_degree": 40,
        "wind_dir": "NE",
        "pressure_mb": 1022.0,
        "pressure_in": 30.18,
        "precip_mm": 0.1,
        "precip_in": 0.0,
        "humidity": 77,
        "cloud": 100,
        "feelslike_c": 12.5,
        "feelslike_f": 54.6,
        "vis_km": 10.0,
        "vis_miles": 6.0,
        "uv": 4.0,
        "gust_mph": 5.6,
        "gust_kph": 9.0
        }
    }
     */
}
