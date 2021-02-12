package com.price.processor.impl;

import com.price.processor.PriceDisplay;

import java.util.Objects;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;


public class PriceUpdater extends RecursiveAction {

    private final PriceDisplay priceDisplay;
    private final String ccyPair;
    private final double rate;
    private AtomicBoolean isFinish = new AtomicBoolean(false);

    public PriceUpdater(String ccyPair, double rate) {

        // choose random display realisation for ccyPair
        this(ThreadLocalRandom.current().nextInt((2 - 1) + 1) + 1 == 1 ? new ScreenPriceDisplay() : new PrintOnPaperPriceDisplay(), ccyPair, rate);
    }

    public PriceUpdater(PriceDisplay priceDisplay, String ccyPair, double rate) {
        this.priceDisplay = priceDisplay;
        this.ccyPair = ccyPair;
        this.rate = rate;
    }

    public void stop() {
        priceDisplay.stop();
    }

    public boolean isFinish() {
        return isFinish.get();
    }

    @Override
    protected void compute() {
        priceDisplay.display(ccyPair, rate);
        isFinish.set(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceUpdater that = (PriceUpdater) o;
        return Double.compare(that.rate, rate) == 0 && Objects.equals(priceDisplay, that.priceDisplay) && Objects.equals(ccyPair, that.ccyPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceDisplay, ccyPair, rate);
    }
}
