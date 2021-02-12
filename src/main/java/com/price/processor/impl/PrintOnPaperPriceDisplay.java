package com.price.processor.impl;

import com.price.processor.AbstractPriceDisplay;
import com.price.processor.PriceDisplay;

public class PrintOnPaperPriceDisplay extends AbstractPriceDisplay implements PriceDisplay {

    @Override
    public void display(String ccyPair, double rate) {

        // some action to display
        // check for stopping display if change ccyPair
        if (isStop())
            return;
    }
}
