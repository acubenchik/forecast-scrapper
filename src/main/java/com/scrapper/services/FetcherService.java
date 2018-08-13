package com.scrapper.services;


import io.reactivex.Flowable;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class FetcherService implements IFetcherService {
    @Override
    public Flowable<DataElement> fetchData(Vertx vertx) {
        return Flowable.fromArray("https://www.mountain-forecast.com/peaks/Rysy/forecasts/2499",
                "https://www.mountain-forecast.com/peaks/Koscielec/forecasts/2155")
                .parallel(2).runOn(RxHelper.blockingScheduler(vertx, false))
                .flatMap(url -> {
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
}
