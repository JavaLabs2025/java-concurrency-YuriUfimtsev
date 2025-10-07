package org.labs.model;

import org.labs.service.KitchenService;
import org.labs.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Programmer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Programmer.class);

    private final int id;
    private final Spoon leftSpoon;
    private final Spoon rightSpoon;
    private volatile boolean hasSoupPortion = false;
    private final OrdersService ordersService;
    private final KitchenService kitchenService;
    private int eatenCount = 0;

    private final Duration discussionTime;
    private final Duration eatTime;

    public Programmer(int id, Spoon leftFork, Spoon rightSpoon, OrdersService ordersService,
                      KitchenService kitchenService, Duration discussionTime, Duration eatTime) {
        this.id = id;
        this.leftSpoon = leftFork;
        this.rightSpoon = rightSpoon;
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
            logger.debug("Programmer {} is running", id);

            while (kitchenService.getSoupPortionsCount() > 0) {
                discuss();

                ordersService.makeOrder(this);
                logger.debug("Programmer {} has placed an order and is now waiting for the soup", id);

                while (!hasSoupPortion && kitchenService.getSoupPortionsCount() > 0) {
                    Thread.onSpinWait();
                }

                if (kitchenService.getSoupPortionsCount() == 0) {
                    break;
                }

                synchronized (leftSpoon) {
                    synchronized (rightSpoon) {
                        eat();
                    }
                }
            }

            logger.debug("Soup portions count equals to 0. Programmer {} was finished ", id);
        } catch (InterruptedException exception) {
            logger.warn("Programmer {} was interrupted", id);
            Thread.currentThread().interrupt();
        }
    }

    private void eat() throws InterruptedException {
        logger.debug("Programmer {} starts eating for {}", id, eatTime);
        Thread.sleep(eatTime);

        ++eatenCount;
        hasSoupPortion = false;
    }

    private void discuss() throws InterruptedException {
        logger.debug("Programmer {} starts discussing for {}", id, discussionTime);
        Thread.sleep(discussionTime);
    }
}
