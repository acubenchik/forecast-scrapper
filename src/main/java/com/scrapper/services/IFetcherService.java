package com.scrapper.services;


import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;

public interface IFetcherService {

    Flowable<DataElement> fetchData(Vertx vertx);
}
