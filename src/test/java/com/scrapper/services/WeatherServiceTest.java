package com.scrapper.services;

import org.junit.BeforeClass;
import org.junit.Test;

public class WeatherServiceTest {

    private IWeatherService weatherService;

    @BeforeClass
    public void init(){
        weatherService = new WeatherService(null);
    }

    @Test
    public void test(){

    }

}
