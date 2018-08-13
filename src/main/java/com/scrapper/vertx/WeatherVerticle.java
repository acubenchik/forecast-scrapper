package com.scrapper.vertx;

import com.scrapper.services.FetcherService;
import com.scrapper.services.IWeatherService;
import com.scrapper.services.WeatherService;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class WeatherVerticle extends AbstractVerticle {

    public static final String WEATHER_SERVICE_ADDRESS = "weather-service";
    private IWeatherService weatherService;
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.weatherService = new WeatherService(this.vertx, new FetcherService());
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        MessageConsumer<JsonObject> binder = new ServiceBinder(vertx.getDelegate())
                .setAddress(WEATHER_SERVICE_ADDRESS)
                .register(IWeatherService.class, weatherService);

        binder.completionHandler(startFuture);
    }
}
