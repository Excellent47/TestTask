package ru.crpt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class CrptApi implements AutoCloseable {

    private final int requestLimit;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Значение requestLimit должно быть положительным");
        }
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler.scheduleAtFixedRate(this::resetRequestCount, 1, 1, timeUnit);
    }

    public <T> T makeRequest(Supplier<T> requestSupplier) throws InterruptedException {
        try {
            semaphore.acquire();
            if (requestCount.incrementAndGet() > requestLimit) {
                throw new IllegalStateException("Превышено кол-во запросов");
            }

            return requestSupplier.get();
        } finally {
            semaphore.release();
        }
    }

    private void resetRequestCount() {
        int current = requestCount.getAndSet(0);
        semaphore.release(current);
    }

    @Override
    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
