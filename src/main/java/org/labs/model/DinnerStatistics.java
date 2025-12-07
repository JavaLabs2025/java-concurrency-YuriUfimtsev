package org.labs.model;

import java.time.Duration;
import java.util.Map;

public record DinnerStatistics(
    long remainingPortionsInKitchen,
    Map<Integer, Integer> visitorIdToEatenCount,
    Duration dinnerDuration
) { }
