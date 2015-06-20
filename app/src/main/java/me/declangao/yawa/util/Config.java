package me.declangao.yawa.util;

/**
 * Configurations
 */
public class Config {
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    public static final String FORECAST_URL = BASE_URL + "forecast/daily?mode=json&units=metric&cnt=7&q=";
    public static final String CURRENT_URL = BASE_URL + "weather?units=metric&q=";
    public static final String WEATHER_ICON_PREFIX = "http://openweathermap.org/img/w/";
}
