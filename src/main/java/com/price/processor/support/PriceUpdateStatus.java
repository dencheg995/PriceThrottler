package com.price.processor.support;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PriceUpdateStatus {

    private AtomicBoolean isActiveUpdate;
    private AtomicLong lastTimeUpdate;

    public PriceUpdateStatus(boolean isActiveUpdate, Long lastTimeUpdate) {
        this.isActiveUpdate = new AtomicBoolean(isActiveUpdate);
        this.lastTimeUpdate = new AtomicLong(lastTimeUpdate);
    }

    public boolean isActiveUpdate() {
        return isActiveUpdate.get();
    }

    public void setActiveUpdate(boolean activeUpdate) {
        isActiveUpdate.set(activeUpdate);
    }

    public Long getLastTimeUpdate() {
        return lastTimeUpdate.get();
    }

    public void setLastTimeUpdate(Long lastTimeUpdate) {
        this.lastTimeUpdate.set(lastTimeUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceUpdateStatus that = (PriceUpdateStatus) o;
        return isActiveUpdate == that.isActiveUpdate && Objects.equals(lastTimeUpdate, that.lastTimeUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActiveUpdate, lastTimeUpdate);
    }
}
