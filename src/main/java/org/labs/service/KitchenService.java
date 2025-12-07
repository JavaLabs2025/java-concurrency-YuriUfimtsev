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
        var previousPortionsCount = soupPortionsCount.getAndUpdate(portionsCount ->
                portionsCount > 0 ? portionsCount - 1 : portionsCount
        );
        return previousPortionsCount > 0;
    }
}
