package org.labs.model;


public record Order(
    int visitorId,
    int portionsEaten,
    SoupPortionNotifier soupPortionNotifier
) { }
