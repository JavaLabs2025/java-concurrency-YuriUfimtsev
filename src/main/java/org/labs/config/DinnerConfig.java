package org.labs.config;

import org.labs.model.DurationRange;

import java.time.Duration;

public record DinnerConfig(
    int visitorsCount,
    int waitersCount,
    long soupPortionsCount,
    Duration waitersServingDelay,
    DurationRange visitorsDiscussionDelay,
    DurationRange visitorsEatingDelay
) { }
