package com.price.processor;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractPriceDisplay implements PriceDisplay {

    protected AtomicBoolean isStop = new AtomicBoolean(false);

    @Override
    public boolean isStop() {
        return isStop.get();
    }

    @Override
    public void stop() {
        isStop.set(true);
    }
}
