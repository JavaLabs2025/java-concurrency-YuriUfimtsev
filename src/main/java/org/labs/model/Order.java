package org.labs.model;

import org.labs.model.notifier.NoMorePortionsNotifier;
import org.labs.model.notifier.SoupPortionNotifier;

public record Order(
    int visitorId,
    int portionsEaten,
    SoupPortionNotifier soupPortionNotifier,
    NoMorePortionsNotifier noMorePortionsNotifier
) { }
