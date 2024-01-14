# Cities Weather

This project is using a public API that provides city weather and it is offering different presentation layers for such data.

## About The Project

The project aims to provide useful APIs that are grouping and showing weather for cities to support different use cases:
1. By providing a list with more cities create a text file with every city and corresponding temperature on every line
2. By providing a list with more cities (e.g. all european capital cities) create a image/png with all these cities ordered based on temperature
This image can be used for multiple purposes like displaying ordered temperature for major cities on different websites like tourist websites.
Also image/png with actual weather conditions (clouds, sun, rain) for every city is downloaded and can be used.
3. Project is exposing APIs to group and order cities based on real temperature or based on feels-like temperature. 
This can be used for multiple purposes - e.g. to find the coldest city during the summer when we want to have one-day city-break and we don't know what city to choose to escape the heat a little bit

## Inventory
Without any IDE the application can be started by using : "java -jar cities_weather-0.0.1-SNAPSHOT.jar" after running it in folder where jar built "cities_weather-0.0.1-SNAPSHOT.jar" is copied 
GET http://localhost:8082/api/weathers_image -- provide real temperature for all capital cities from Europe displayed in a PNG picture; temperatures are cached for 5 minutes (configurable) and returned very fast

City icon are downloaded for each city with weather sky condition (sun, clouds, rain, snow etc.) in a separate folder "cityWeatherPictures" 

POST http://localhost:8082/api/weathers -- provide real temperature for a list of cities provided in payload like a list of strings : {"cities" : ["London","Berlin"]}

GET http://localhost:8082/api/weathers/{city} -- provide real temperature for a city provided like a path variable

GET http://localhost:8082/token/getToken -- to get a JWT token used to authenticate for below 2 services. Expects a payload like : {"user":"abc@yahoo.com","operation":"getWeather"}

POST http://localhost:8082/api/groupCitiesByTemp?ordering={asc/desc} -- provide real and feels-like temperatures for a list of cities ordered based on real temperatures

POST http://localhost:8082/api/groupCitiesByFeelsLikeTemp?ordering={asc/desc} -- provide real and feels-like temperatures for a list of cities ordered based on feels-like temperatures
