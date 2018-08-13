package com.scrapper.services;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Comparator;
import java.util.List;


public class WeatherService implements IWeatherService {

    private Vertx vertx;

    public WeatherService(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public IWeatherService averageRainPerWeek(Handler<AsyncResult<Integer>> resultHandler) {
        this.fetchData().scan(0, (i, k) -> {
            i = i + k.getRainLevel();
            return i;
        }).takeLast(1).map(integer -> integer / 4).firstOrError()
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    public Flowable<DataElement> fetchData() {
        System.out.println("Hmm outside " + Thread.currentThread().getName());
        return Flowable.fromArray("https://www.mountain-forecast.com/peaks/Rysy/forecasts/2499",
                "https://www.mountain-forecast.com/peaks/Koscielec/forecasts/2155")
                .parallel(2).runOn(RxHelper.blockingScheduler(vertx, false))
                .flatMap(url -> {
                    System.out.println("Hmm " + Thread.currentThread().getName());
                    Document doc = Jsoup.connect(url).get();
                    Elements rainLevels = doc.select("tr.forecast__table-rain td");
                    Elements snowLevels = doc.select("tr.forecast__table-snow td");
                    List<String> days = doc.select("div.forecast__table-days-name").eachText();
                    return Flowable.zip(Flowable.<Element>fromArray(rainLevels.toArray(new Element[rainLevels.size()])),
                            Flowable.<Element>fromArray(snowLevels.toArray(new Element[snowLevels.size()])),
                            (el1, el2) -> {
                                Element rainResult = el1.select("span.rain").get(0);
                                Element snowResult = el2.select("span.snow").get(0);
                                DataElement result = new DataElement();
                                if (!days.isEmpty()) {
                                    if (rainResult.text().equals("-")) {
                                        result.setRainLevel(0);
                                    } else {
                                        result.setRainLevel(Integer.valueOf(rainResult.text()));
                                    }
                                    if (snowResult.text().equals("-")) {
                                        result.setSnowLevel(0);
                                    } else {
                                        result.setSnowLevel(Integer.valueOf(snowResult.text()));
                                    }
                                    result.setCategory(days.get(0) + " " + url);
                                    if (el1.select("td.forecast__table-day-end").size() != 0) {
                                        days.remove(0);
                                    }
                                }
                                return result;

                            });
                }).sequential();

    }

    private Single<DataElement> mostRainyDay() {
        return this.fetchData()
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
        return this.fetchData()
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