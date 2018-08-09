package com.scrapper.vertx;

import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.rxDeployVerticle("com.scrapper.vertx.WeatherVerticle")
                .flatMap(s -> {
                    Single<String> httpVerticleDeployment = vertx.rxDeployVerticle(
                            "com.scrapper.vertx.HttpVerticle",
                            new DeploymentOptions().setInstances(1));
                    return httpVerticleDeployment;
                }).subscribe(id -> startFuture.complete(), startFuture::fail);
    }
}
