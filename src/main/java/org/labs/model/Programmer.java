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
    private volatile SoupPortionStatus soupPortionStatus = SoupPortionStatus.NO_PORTION;
    private final OrdersService ordersService;
    private int eatenCount = 0;

    private final Duration discussionTime;
    private final Duration eatTime;

    public Programmer(int id, Spoon leftFork, Spoon rightSpoon, OrdersService ordersService,
                      Duration discussionTime, Duration eatTime) {
        this.id = id;
        this.leftSpoon = leftFork;
        this.rightSpoon = rightSpoon;
        this.discussionTime = discussionTime;
        this.eatTime = eatTime;
        this.ordersService = ordersService;
    }

    public void setSoupPortion() {
        this.soupPortionStatus = SoupPortionStatus.HAS_PORTION;
    }

    public void noMoreSoup() {
        this.soupPortionStatus = SoupPortionStatus.NO_MORE_PORTIONS;
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

            while (ordersService.getAreOrdersAccepted()) {
                discuss();

                if (!ordersService.makeOrder(this)) {
                    break;
                }
                logger.debug("Programmer {} has placed an order and is now waiting for the soup", id);

                while (soupPortionStatus == SoupPortionStatus.NO_PORTION) {
                    Thread.onSpinWait();
                }

                if (soupPortionStatus == SoupPortionStatus.NO_MORE_PORTIONS) {
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
        soupPortionStatus = SoupPortionStatus.NO_PORTION;
    }

    private void discuss() throws InterruptedException {
        logger.debug("Programmer {} starts discussing for {}", id, discussionTime);
        Thread.sleep(discussionTime);
    }

    private enum SoupPortionStatus {
        HAS_PORTION,
        NO_PORTION,
        NO_MORE_PORTIONS
    }
}
