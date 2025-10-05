package org.labs.model;

import org.labs.service.KitchenService;
import org.labs.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Programmer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Programmer.class);

    private final int id;
    private final Spoon leftFork;
    private final Spoon rightFork;
    private volatile boolean hasSoupPortion = false;
    private final OrdersService ordersService;
    private final KitchenService kitchenService;
    private int eatenCount = 0;

    private final Duration discussionTime;
    private final Duration eatTime;

    public Programmer(int id, Spoon leftFork, Spoon rightFork, OrdersService ordersService,
                      KitchenService kitchenService, Duration discussionTime, Duration eatTime) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.discussionTime = discussionTime;
        this.eatTime = eatTime;
        this.ordersService = ordersService;
        this.kitchenService = kitchenService;
    }

    public void setHasSoupPortion() {
        this.hasSoupPortion = true;
    }

    public int getId() {
        return id;
    }

    public int getEatenCount() {
        return eatenCount;
    }

    @Override
    public void run() {
        try {
            while (kitchenService.getSoupPortionsCount() > 0) {
                discuss();

                ordersService.makeOrder(this);
                while (!hasSoupPortion) {
                    Thread.onSpinWait();
                }

                synchronized (leftFork) {
                    synchronized (rightFork) {
                        eat();
                    }
                }

            }
        } catch (InterruptedException exception) {
            logger.info("Programmer {} was interrupted", id);
            Thread.currentThread().interrupt();
        }
    }

    private void eat() throws InterruptedException {
        logger.info("Programmer {} starts eating for {}", id, eatTime);
        Thread.sleep(eatTime);

        ++eatenCount;
        hasSoupPortion = false;
    }

    private void discuss() throws InterruptedException {
        logger.info("Programmer {} start discussing for {}", id, discussionTime);
        Thread.sleep(discussionTime);
    }
}
