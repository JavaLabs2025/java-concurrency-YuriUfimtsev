package org.labs.model;

import java.time.Duration;

public record DurationRange(
    Duration minDuration,
    Duration maxDuration
) { }
