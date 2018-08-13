package com.scrapper.vertx;

import com.scrapper.services.IWeatherService;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class HttpVerticle extends AbstractVerticle {

    private com.scrapper.services.reactivex.IWeatherService weatherService;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        weatherService =
                new com.scrapper.services.reactivex.IWeatherService(new ServiceProxyBuilder(vertx)
                        .setAddress(WeatherVerticle.WEATHER_SERVICE_ADDRESS)
                        .build(IWeatherService.class));
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/statistics").handler(this::statisticsHandler);
        httpServer.requestStream().toFlowable().map(HttpServerRequest::pause)
                .onBackpressureDrop(httpServerRequest -> {
                    System.out.println("Dropped");
                })
//                .observeOn(RxHelper.blockingScheduler(vertx.getDelegate()))
               .subscribe(router::accept);
        httpServer.rxListen(8080)
                .subscribe(s -> {
                    startFuture.complete();
                }, startFuture::fail);
    }

    private void statisticsHandler(RoutingContext routingContext) {
        System.out.println("Outside " + Thread.currentThread().getName());
        weatherService.rxAverageRainPerWeek().subscribe(integer -> {
            System.out.println("Inside " + Thread.currentThread().getName());
            routingContext.response().end("<html><body>" + String.valueOf(integer) + "</body></html>");
        }, routingContext::fail);
    }
}
