package com.scrapper.services;

import io.reactivex.Flowable;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(VertxUnitRunner.class)
public class WeatherServiceTest {

    private static IWeatherService weatherService;
    private static Vertx vertx;
    private static IFetcherService fetcherMock;

    @BeforeClass
    public static void setUp(TestContext testContext) {
        vertx = Vertx.vertx();
        fetcherMock = mock(IFetcherService.class);
        weatherService = new WeatherService(vertx, fetcherMock);
    }

    @Test
    public void testAverageRain() throws IOException {
        DataElement de1 = new DataElement("cat1", 10, 0);
        DataElement de2 = new DataElement("cat2", 20, 0);
        when(fetcherMock.fetchData(any(Vertx.class))).thenReturn(Flowable.fromArray(de1, de2));
        weatherService.totalRainPerWeek(res -> {
            assertEquals(30, res.result().intValue());
        });
    }

}
