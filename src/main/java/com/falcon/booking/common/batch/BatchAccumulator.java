package com.falcon.booking.common.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BatchAccumulator<T> {

    private final int batchSize;
    private final Consumer<List<T>> saveBatchFn;
    private final List<T> batch;
    private int total = 0;

    public BatchAccumulator(int batchSize, Consumer<List<T>> saveBatchFn) {
        this.batchSize = batchSize;
        this.saveBatchFn = saveBatchFn;
        this.batch = new ArrayList<>(batchSize);
    }

    public void add(T item) {
        batch.add(item);
        if (batch.size() >= batchSize) {
            flush();
        }
    }

    public int flushAndGetTotal() {
        flush();
        return total;
    }

    private void flush() {
        if (batch.isEmpty()) return;
        saveBatchFn.accept(batch);
        total += batch.size();
        batch.clear();
    }
}