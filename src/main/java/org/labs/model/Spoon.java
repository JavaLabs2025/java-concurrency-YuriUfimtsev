package org.labs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Spoon {
    private static final Logger logger = LoggerFactory.getLogger(Spoon.class);

    private final int id;
    private final Lock lock = new ReentrantLock();

    public Spoon(int id) {
        this.id = id;
    }

    public void take(int holderId) {
        lock.lock();
        logger.debug("Spoon {} is taken by holder {}", id, holderId);
    }

    public void putDown(int holderId) {
        lock.unlock();
        logger.debug("Spoon {} is put down by holder {}", id, holderId);
    }
}
