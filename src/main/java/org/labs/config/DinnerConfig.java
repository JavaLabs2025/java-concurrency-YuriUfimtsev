package org.labs.config;

import java.time.Duration;

public record DinnerConfig(
    int visitorsCount,
    int waitersCount,
    long soupPortionsCount,
    Duration waitersServingDelay,
    Duration visitorsDiscussionDelay,
    Duration visitorsEatingDelay
) { }
