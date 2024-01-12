package com.example.kyn.controllers;

import com.example.kyn.dto.weather.CityWeathersByTempResponseDTO;
import com.example.kyn.dto.weather.CurrentInfo;
import com.example.kyn.dto.weather.ErrorMessage;
import com.example.kyn.dto.weather.InputCities;
import com.example.kyn.dto.weather.Location;
import com.example.kyn.dto.weather.WeatherInputRequest;
import com.example.kyn.dto.weather.WeatherResponseDTO;
import com.example.kyn.services.TokenService;
import com.example.kyn.services.WeatherService;
import com.google.common.util.concurrent.RateLimiter;
import com.example.kyn.dto.token.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class WeatherController {

    private WeatherService weatherService;
    private TokenService tokenService;
    private CacheManager cacheManager;

    public WeatherController(final @Value("${token.api.secret}") String tokenSecret, WeatherService weatherService,
                             TokenService tokenService, CacheManager cacheManager) {
        this.weatherService = weatherService;
        this.tokenService = tokenService;
        this.cacheManager = cacheManager;
    }

    private List<String> capitalCities = Arrays.asList("Vienna","Brussels","Sofia","Prague","Berlin","Copenhagen","Madrid","Tallinn","Helsinki","Paris","London","Athens","Zagreb","Budapest","Dublin","Reykjavik",
            "Rome","Vilnius","Luxembourg","Riga","Amsterdam","Oslo","Warsaw","Lisbon","Bucharest","Moscow","Belgrade","Bratislava","Ljubljana","Stockholm","Kyiv","Istanbul");

    @GetMapping(value = "/weathers", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] provideWeatherForMoreCities(@RequestBody InputCities citiesList) {

        if (citiesList.getCities().isEmpty()) {
            throw new IllegalArgumentException();
        }

        List<WeatherResponseDTO> weatherByCities = citiesList.getCities().stream()
                .map(city -> weatherService.getWeatherByCity(city).block())
                .collect(Collectors.toList());

        List<byte[]> cities = new ArrayList<>();
        String newLine = "\n";
        String space = "  ";
        weatherByCities.stream()
                .forEach(city -> {
                    cities.add(city.getLocation().getName().getBytes());
                    cities.add(space.getBytes());
                    cities.add(String.valueOf(city.getCurrent().getTemp_c()).getBytes());
                    cities.add(space.getBytes());
                    cities.add(newLine.getBytes());
                });
        // Remove last new line
        cities.remove(cities.size() - 1);

        int size = 0, index = 0;
        for (byte[] element : cities) {
            size = size + element.length;
        }
        byte[] result = new byte[size];
        for (int i = 0; i < cities.size(); i++) {
            for (int j = 0; j < cities.get(i).length; j++) {
                result[index] = cities.get(i)[j];
                index++;
            }
        }
        return result;
    }


    @GetMapping(value = "/weathers/{city}")
    public String provideWeatherByCity(@PathVariable("city") String city) {

        return weatherService.getWeatherByCity(city)
                .doOnNext(result -> {
                    String[] cityInfo = new String[2];
                    cityInfo[0] = String.valueOf(result.getCurrent().getTemp_c());
                    cityInfo[1] = String.valueOf(System.currentTimeMillis());
                    cacheManager.getCache("temperatures").evictIfPresent(city);
                    cacheManager.getCache("temperatures").put(city, cityInfo);
                })
                .doOnNext(result -> {
                    log.info("City " + result.getLocation().getName() + " has temp " + result.getCurrent().getTemp_c());
                })
                .map(result -> String.valueOf(result.getCurrent().getTemp_c()))
                .block();
    }


    @GetMapping(value = "/weathers_image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] provideWeatherImageForMoreCities() throws IOException {

        // Implement a throttling using Guava RateLimiter
        // RateLimiter rateLimiter = RateLimiter.create(100);

        List<String> citiesToSelect;

        Map<String, String> cityExistingCache = new HashMap<>();

        Cache tempCache = cacheManager.getCache("temperatures");

        String localDir = System.getProperty("user.dir");
        String filePathDelimiter = System.getProperty("file.separator");

        capitalCities.parallelStream()
                .forEach(city -> {
                    if (tempCache != null && tempCache.get(city) != null) {
                        String[] tempTime = (String[]) tempCache.get(city).get();
                        assert tempTime != null;
                        Date date = new Date(Long.parseLong(tempTime[1]));
                        log.info("City|Temp|Time from cache:" + city + " | " + tempTime[0] + " | " + date);
                        if (System.currentTimeMillis() < Long.parseLong(tempTime[1]) + 300000)  // temp found in cache in newer than 5 minutes
                            cityExistingCache.put(city, tempTime[0]);
                    }
                });

        if (!cityExistingCache.isEmpty()) {
            log.info("Valid cities found in cache:" + cityExistingCache);
        }

        citiesToSelect = capitalCities.stream().filter(city -> !cityExistingCache.containsKey(city))
                .collect(Collectors.toList());

        long startTime = System.currentTimeMillis();
        log.debug("StartTime all cities getWeatherByCity:{}", startTime);

        List<WeatherResponseDTO> weatherByCities = citiesToSelect.parallelStream()
                .map(city -> {
                    // Activate or deactivate throttling
                    // double timeToSleep = rateLimiter.acquire();
                    // log.info("Get City " + city + " call will sleep seconds:" + timeToSleep);
                    WeatherResponseDTO cityWeather = weatherService.getWeatherByCity(city).block();
                    String[] cityInfo = new String[2];
                    cityInfo[0] = String.valueOf(cityWeather.getCurrent().getTemp_c());
                    cityInfo[1] = String.valueOf(System.currentTimeMillis());
                    cacheManager.getCache("temperatures").evictIfPresent(city);
                    cacheManager.getCache("temperatures").put(city, cityInfo);
                    return cityWeather;
                })
                .collect(Collectors.toList());

        var endTime = System.currentTimeMillis();
        log.debug("EndTime all cities getWeatherByCity:{}", endTime);
        log.info("Duration all cities for /weathers_image:{} millis", endTime - startTime);

        // Add in the list cities that are already present in the cache with valid temperatures
        cityExistingCache.entrySet().stream()
                .forEach(value -> weatherByCities.add(WeatherResponseDTO.builder()
                        .current(CurrentInfo.builder().temp_c(Double.parseDouble(value.getValue())).build())
                        .location(Location.builder().name(value.getKey()).build())
                        .build()));

        int width = 250;
        int height = 750;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // create a string with blue
        g2d.setColor(Color.blue);
        AtomicInteger pos = new AtomicInteger(60);
        AtomicInteger y = new AtomicInteger(90);

        weatherByCities.stream()
                .filter(cityWeather -> cityWeather.getErrorMessage() == null)
                .sorted(Comparator.comparing(cityWeather -> Double.valueOf(cityWeather.getCurrent().getTemp_c())))
                .forEach(cityWeather -> {
                    // save the city picture temperature - just for fun
                    if (cityWeather.getCurrent().getCondition() != null) {
                        byte[] picture = weatherService.getCityPicture(cityWeather);
                        try {
                            Path picturesDir = Files.createDirectories(Paths.get(localDir + filePathDelimiter + "cityWeatherPictures"));
                            Files.write(Paths.get(picturesDir + filePathDelimiter + "picture_" + cityWeather.getLocation().getName() + ".jpg"), picture);
                        } catch (IOException e) {
                            log.info("Exception when writing file:{}", e.getMessage());
                        }
                    }
                    g2d.drawString(cityWeather.getLocation().getName(), pos.get(), y.get());
                    pos.set(pos.get() + 85);
                    g2d.drawString(String.valueOf(cityWeather.getCurrent().getTemp_c()), pos.get(), y.get());
                    pos.set(60);
                    y.set(y.get() + 20);

                });

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();
        // Save as JPG
        File file = new File(localDir + filePathDelimiter + "allCitiesWeather.jpg");
        ImageIO.write(bufferedImage, "jpg", file);
        return Files.readAllBytes(Paths.get(localDir + filePathDelimiter + "allCitiesWeather.jpg"));

    }

    @GetMapping(value = "/groupCitiesByTemp")
    public ResponseEntity<CityWeathersByTempResponseDTO> groupCitiesByTemp(@RequestParam String ordering, @RequestBody WeatherInputRequest weatherInputRequest,
                                                                           @RequestHeader("Authorization") String authorization) {

        CityWeathersByTempResponseDTO errorResponse = CityWeathersByTempResponseDTO.builder().build();

        TokenResponse tokenResponse = tokenService.validateToken(authorization);
        if (!tokenResponse.isValidToken()) {
            List<ErrorMessage> errorMessages = new ArrayList<>();
            errorMessages.add(ErrorMessage.builder().errorMessage(tokenResponse.getErrorMessage()).build());
            errorResponse.setErrorMessages(errorMessages);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        long startTime = System.currentTimeMillis();
        log.debug("StartTime all cities getWeatherByCity:{}", startTime);

        CityWeathersByTempResponseDTO allCitiesTemp = weatherService.groupAllCitiesByRealTemp(weatherInputRequest, ordering);

        long endTime = System.currentTimeMillis();
        log.debug("EndTime all cities getWeatherByCity:{}", endTime);
        log.info("Duration all cities for /groupCitiesByTemp:{} millis", endTime - startTime);

        if (!allCitiesTemp.getTemperatures().isEmpty()) {
            return ResponseEntity.ok().body(allCitiesTemp);
        } else {
            return ResponseEntity.internalServerError().body(allCitiesTemp);
        }
    }


    @GetMapping(value = "/groupCitiesByFeelsLikeTemp")
    public ResponseEntity<CityWeathersByTempResponseDTO> groupCitiesByFeelsLikeTemp(@RequestParam String ordering, @RequestBody WeatherInputRequest weatherInputRequest,
                                                                                    @RequestHeader("Authorization") String authorization) {

        CityWeathersByTempResponseDTO errorResponse = CityWeathersByTempResponseDTO.builder().build();

        TokenResponse tokenResponse = tokenService.validateToken(authorization);
        if (!tokenResponse.isValidToken()) {
            List<ErrorMessage> errorMessages = new ArrayList<>();
            errorMessages.add(ErrorMessage.builder().errorMessage(tokenResponse.getErrorMessage()).build());
            errorResponse.setErrorMessages(errorMessages);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (weatherInputRequest.getCities().isEmpty()) {
            throw new IllegalArgumentException("Missing list of cities");
        }

        long startTime = System.currentTimeMillis();
        log.debug("StartTime all cities getWeatherByCity:{}", startTime);

        CityWeathersByTempResponseDTO allCitiesFeelsLikeTemp = weatherService.groupAllCitiesByFeelsLikeTemp(weatherInputRequest, ordering);

        long endTime = System.currentTimeMillis();
        log.debug("EndTime all cities getWeatherByCity:{}", endTime);
        log.info("Duration all cities for /groupCitiesByTemp:{} millis", endTime - startTime);


        if (!allCitiesFeelsLikeTemp.getTemperatures().isEmpty()) {
            return ResponseEntity.ok().body(allCitiesFeelsLikeTemp);
        } else {
            return ResponseEntity.internalServerError().body(allCitiesFeelsLikeTemp);
        }

    }


    @GetMapping(value = "/clearCache")
    public String clearCache() {
        String message = Objects.requireNonNull(cacheManager.getCache("temperatures")).invalidate() ? "Cache has been cleared" : "Not cleared. No mappings were present before in the cache";
        return message;
    }

}