package com.price.processor.impl;

import com.price.processor.PriceProcessor;
import com.price.processor.support.PriceUpdateStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

import static java.util.Objects.isNull;

public class PriceThrottler implements PriceProcessor {

    private static final int MAX_LISTENERS_SIZE = 200;

    private Set<PriceProcessor> priceProcessors = new CopyOnWriteArraySet<>();
    private Map<String, PriceUpdateStatus> ccyPairStatusUpdate = new ConcurrentHashMap<>();
    private Map<String, List<PriceUpdater>> activeTasks = new ConcurrentHashMap<>();
    private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Override
    public void onPrice(String ccyPair, double rate) {

        PriceUpdateStatus priceUpdateStatus = ccyPairStatusUpdate.get(ccyPair);

        if (isNull(priceUpdateStatus)) {
            priceUpdateStatus = new PriceUpdateStatus(true, System.currentTimeMillis());
            ccyPairStatusUpdate.put(ccyPair, priceUpdateStatus);
        } else {
            if (isNotChangePrice(ccyPair, priceUpdateStatus)) return;
        }

        List<PriceUpdater> priceUpdaters = new ArrayList<>();
        priceProcessors.forEach(priceProcessors -> executeTask(ccyPair, rate, priceUpdaters));
        activeTasks.put(ccyPair, priceUpdaters);
        priceUpdateStatus.setLastTimeUpdate(System.currentTimeMillis());
    }

    private void executeTask(String ccyPair, double rate, List<PriceUpdater> priceUpdaterList) {
        PriceUpdater task = new PriceUpdater(ccyPair, rate);
        forkJoinPool.execute(task);
        priceUpdaterList.add(task);
    }

    private boolean isNotChangePrice(String ccyPair, PriceUpdateStatus priceUpdateStatus) {
        if (priceUpdateStatus.isActiveUpdate()) {

            if (System.currentTimeMillis() - priceUpdateStatus.getLastTimeUpdate() <= 1000)
                return true;

            List<PriceUpdater> priceUpdaters = activeTasks.get(ccyPair);
            priceUpdaters.forEach(priceUpdater -> {
                priceUpdater.stop();
                while (!priceUpdater.isFinish()) {
                    System.out.println("stopping");
                }
            });

            priceUpdateStatus.setActiveUpdate(false);
            activeTasks.remove(ccyPair);
        }
        priceUpdateStatus.setActiveUpdate(true);
        return false;
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        if (!isCanSubscribe())
            return;

        priceProcessors.add(priceProcessor);
    }

    private boolean isCanSubscribe() {
        return priceProcessors.size() <= MAX_LISTENERS_SIZE;
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        priceProcessors.remove(priceProcessor);
    }
}
