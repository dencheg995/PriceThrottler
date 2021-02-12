package com.price.processor.impl;

import com.price.processor.PriceProcessor;
import com.price.processor.support.PriceUpdateStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;

import static java.util.Objects.isNull;

public class PriceThrottler implements PriceProcessor {

    private static final int MAX_LISTENERS_SIZE = 200;
    private static final int UPDATE_TIME_FOR_SKIP_CCY_PAIR = 1000;

    private final Set<PriceProcessor> priceProcessors = new ConcurrentSkipListSet<>();
    private final Map<String, List<PriceUpdater>> activeTasks = new ConcurrentHashMap<>();
    private final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private Map<String, PriceUpdateStatus> ccyPairStatusUpdate = new ConcurrentHashMap<>();


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

            if (System.currentTimeMillis() - priceUpdateStatus.getLastTimeUpdate() <= UPDATE_TIME_FOR_SKIP_CCY_PAIR)
                return true;

            List<PriceUpdater> priceUpdaters = activeTasks.get(ccyPair);
            priceUpdaters.forEach(this::stopping);
            priceUpdateStatus.setActiveUpdate(false);
            activeTasks.remove(ccyPair);
        }
        priceUpdateStatus.setActiveUpdate(true);
        return false;
    }

    private void stopping(PriceUpdater priceUpdater) {
        priceUpdater.stop();
        while (!priceUpdater.isFinish()) {
            System.out.println("correct stopping display ccyPair");
        }
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
