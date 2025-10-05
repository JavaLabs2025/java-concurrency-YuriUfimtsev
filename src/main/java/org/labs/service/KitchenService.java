package org.labs.service;

import java.util.concurrent.atomic.AtomicLong;

public class KitchenService {
    private final AtomicLong soupPortionsCount;

    public KitchenService(long initialSoupPortionsCount) {
        this.soupPortionsCount = new AtomicLong(initialSoupPortionsCount);
    }

    public long getSoupPortionsCount() {
        return soupPortionsCount.get();
    }

    public boolean takeSoupPortion() {
        return soupPortionsCount.decrementAndGet() > 0;
    }
}
