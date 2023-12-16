package com.example.kyn.services;

import com.example.kyn.dto.weather.CityWeathersByTempResponseDTO;
import com.example.kyn.dto.weather.CityWeathersTempDTO;
import com.example.kyn.dto.weather.ErrorMessage;
import com.example.kyn.dto.weather.Temp;
import com.example.kyn.dto.weather.WeatherInputRequest;
import com.example.kyn.dto.weather.WeatherResponseDTO;
import com.example.kyn.errorHandling.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherService {

    private WebClient webClient;

    public WeatherService(@Qualifier("weatherWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<WeatherResponseDTO> getWeatherByCity(String city) {

        WeatherResponseDTO errorResponse = WeatherResponseDTO.builder().build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("q", city).build())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new ClientException("Client error. Original status code:" + response.statusCode(),
                                response.rawStatusCode())))
                .bodyToMono(WeatherResponseDTO.class)
                .timeout(Duration.ofMillis(1000))
                .doOnError(t -> {
                    log.info("General Exception for city: " + city + " is:" + t);
                    errorResponse.setErrorMessage(ErrorMessage.builder().city(city).errorMessage(t.getMessage()).build());
                })
                .doOnError(WebClientResponseException.class, (error) -> {
                    log.info("Response Exception for city: " + city + " have StatusCode:" + error.getStatusCode()
                            + System.lineSeparator() + "ResponseBody:" + error.getResponseBodyAsString());
                    errorResponse.setErrorMessage(ErrorMessage.builder().city(city).errorMessage(error.getMessage()).build());

                })
                .doOnNext(response -> log.info("Call GetWeather for city " + city + " successfully. Temperature:" + response.getCurrent().getTemp_c()))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(3000))
                        .filter(exp -> !(exp instanceof ClientException)))
                .onErrorReturn(errorResponse)
                .subscribeOn(Schedulers.boundedElastic())
                .cache(Duration.ofMinutes(10));
    }

    public byte[] getCityPicture(WeatherResponseDTO cityWeather) {
        String pictureUrl = cityWeather.getCurrent().getCondition().getIcon();
        pictureUrl = pictureUrl.substring(pictureUrl.indexOf("//") + 2);

        return WebClient.builder().baseUrl(pictureUrl)
                .build()
                .get()
                .retrieve()
                .bodyToMono(byte[].class)
                .subscribeOn(Schedulers.boundedElastic())
                .block();

    }

    public CityWeathersByTempResponseDTO groupAllCitiesByRealTemp(WeatherInputRequest weatherInputRequest, String ordering) {

        List<WeatherResponseDTO> weatherByCities = weatherInputRequest.getCities().parallelStream()
                .map(city -> getWeatherByCity(city).block())
                .collect(Collectors.toList());

        List<CityWeathersTempDTO> tempDTOList =
                weatherByCities.stream()
                        .filter(cityWeather -> cityWeather.getErrorMessage() == null)
                        .collect(Collectors.groupingBy(this::groupCitiesByTemp))
                        .entrySet()
                        .stream()
                        .map(this::buildTempByCities)
                        .sorted("asc".equals(ordering) ? Comparator.comparingDouble(CityWeathersTempDTO::getTemp) :
                                "desc".equals(ordering) ? Comparator.comparingDouble(CityWeathersTempDTO::getTemp).reversed() :
                                        Comparator.comparingDouble(CityWeathersTempDTO::getTemp))
                        .collect(Collectors.toList());

        List<ErrorMessage> errorMessages = weatherByCities.stream()
                .filter(cityWeather -> cityWeather.getErrorMessage() != null)
                .map(cityWeather -> cityWeather.getErrorMessage())
                .collect(Collectors.toList());

        return CityWeathersByTempResponseDTO.builder().temperatures(tempDTOList).errorMessages(errorMessages).build();
    }

    private CityWeathersTempDTO groupCitiesByTemp(WeatherResponseDTO cityWeather) {
        CityWeathersTempDTO cityWeathersTempDTO = new CityWeathersTempDTO();
        cityWeathersTempDTO.setTemp(cityWeather.getCurrent().getTemp_c());

        return cityWeathersTempDTO;
    }

    private CityWeathersTempDTO buildTempByCities(Map.Entry<CityWeathersTempDTO, List<WeatherResponseDTO>> entry) {

        CityWeathersTempDTO cityWeathersTempDTO = entry.getKey();

        List<Temp> listTempCities = new ArrayList<>();

        listTempCities = entry.getValue().stream()
                .map(cityWeather -> Temp.builder()
                        .city(cityWeather.getLocation().getName())
                        .feelsLike(cityWeather.getCurrent().getFeelslike_c())
                        .build())
                .sorted(Comparator.comparing(Temp::getCity))
                .collect(Collectors.toList());

        cityWeathersTempDTO.setCities(listTempCities);

        return cityWeathersTempDTO;
    }

    public CityWeathersByTempResponseDTO groupAllCitiesByFeelsLikeTemp(WeatherInputRequest weatherInputRequest, String ordering) {
        List<WeatherResponseDTO> weatherByCities = weatherInputRequest.getCities().parallelStream()
                .map(city -> getWeatherByCity(city).block())
                .collect(Collectors.toList());

        List<CityWeathersTempDTO> tempDTOList =
                weatherByCities.stream()
                        .filter(cityWeather -> cityWeather.getErrorMessage() == null)
                        .collect(Collectors.groupingBy(this::groupCitiesByFeelsLikeTemp))
                        .entrySet()
                        .stream()
                        .map(this::buildFeelsLikeTempByCities)
                        .sorted("asc".equals(ordering) ? Comparator.comparingDouble(CityWeathersTempDTO::getFeelsLikeTemp) :
                                "desc".equals(ordering) ? Comparator.comparingDouble(CityWeathersTempDTO::getFeelsLikeTemp).reversed() :
                                        Comparator.comparingDouble(CityWeathersTempDTO::getFeelsLikeTemp))
                        .collect(Collectors.toList());

        List<ErrorMessage> errorMessages = weatherByCities.stream()
                .filter(cityWeather -> cityWeather.getErrorMessage() != null)
                .map(cityWeather -> cityWeather.getErrorMessage())
                .collect(Collectors.toList());

        return CityWeathersByTempResponseDTO.builder().temperatures(tempDTOList).errorMessages(errorMessages).build();

    }

    private CityWeathersTempDTO groupCitiesByFeelsLikeTemp(WeatherResponseDTO cityWeather) {
        CityWeathersTempDTO cityWeathersTempDTO = new CityWeathersTempDTO();
        cityWeathersTempDTO.setFeelsLikeTemp(cityWeather.getCurrent().getFeelslike_c());

        return cityWeathersTempDTO;
    }

    private CityWeathersTempDTO buildFeelsLikeTempByCities(Map.Entry<CityWeathersTempDTO, List<WeatherResponseDTO>> entry) {

        CityWeathersTempDTO cityWeathersTempDTO = entry.getKey();

        List<Temp> listTempCities = new ArrayList<>();

        listTempCities = entry.getValue().stream()
                .map(cityWeather -> Temp.builder()
                        .city(cityWeather.getLocation().getName())
                        .temp(cityWeather.getCurrent().getTemp_c())
                        .build())
                .sorted(Comparator.comparing(Temp::getCity))
                .collect(Collectors.toList());

        cityWeathersTempDTO.setCities(listTempCities);

        return cityWeathersTempDTO;
    }
}
