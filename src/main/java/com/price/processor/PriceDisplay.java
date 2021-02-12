package com.price.processor;

public interface PriceDisplay {

    void display(String ccyPair, double rate);

    void stop();

    boolean isStop();
}
