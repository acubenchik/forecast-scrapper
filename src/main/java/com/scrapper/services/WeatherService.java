package com.scrapper.services;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Comparator;
import java.util.List;


public class WeatherService implements IWeatherService {

    private Vertx vertx;
    private IFetcherService fetcherService;

    public WeatherService(Vertx vertx, IFetcherService fetcherService) {
        this.vertx = vertx;
        this.fetcherService = fetcherService;
    }

    @Override
    public IWeatherService totalRainPerWeek(Handler<AsyncResult<Integer>> resultHandler) {
        fetcherService.fetchData(vertx).scan(0, (i, k) -> {
            i = i + k.getRainLevel();
            return i;
        }).takeLast(1).firstOrError()
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    private Single<DataElement> mostRainyDay() {
        return  fetcherService.fetchData(vertx)
                .groupBy(DataElement::getCategory)
                .flatMap(dataElementDataElementGroupedFlowable ->
                        dataElementDataElementGroupedFlowable.scan(0, (i, k) -> {
                            i = i + k.getRainLevel();
                            return i;
                        }).takeLast(1).flatMap(integer ->
                                Flowable.just(new DataElement(dataElementDataElementGroupedFlowable.getKey(), integer, 0))))
                .sorted(Comparator.comparingInt(DataElement::getRainLevel)).takeLast(1).firstOrError();
    }

    private Flowable<DataElement> daysWithRainLessThen(int level) {
        return  fetcherService.fetchData(vertx)
                .groupBy(DataElement::getCategory)
                .flatMap(dataElementDataElementGroupedFlowable ->
                        dataElementDataElementGroupedFlowable.scan(0, (i, k) -> {
                            i = i + k.getRainLevel();
                            return i;
                        })
                                .takeLast(1)
                                .flatMap(integer ->
                                        Flowable.just(new DataElement(dataElementDataElementGroupedFlowable.getKey(), integer, 0))))

                .filter(dataElement -> dataElement.getRainLevel() <= level);
    }


}