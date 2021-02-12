package com.price.processor.impl;

import com.price.processor.AbstractPriceDisplay;
import com.price.processor.PriceDisplay;

public class ScreenPriceDisplay extends AbstractPriceDisplay implements PriceDisplay {

    @Override
    public void display(String ccyPair, double rate) {
        System.out.printf("ccyPair = '%s' with rate = '%s'", ccyPair, rate);
    }
}
