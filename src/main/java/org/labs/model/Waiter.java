package org.labs.model;

import org.labs.service.KitchenService;
import org.labs.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Waiter implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Waiter.class);

    private final int id;
    private final Duration timePerClient;
    private final OrdersService ordersService;
    private final KitchenService kitchenService;

    public Waiter(int id, OrdersService ordersService, KitchenService kitchenService, Duration timePerClient) {
        this.id = id;
        this.timePerClient = timePerClient;
        this.ordersService = ordersService;
        this.kitchenService = kitchenService;
    }

    @Override
    public void run() {
        while (kitchenService.getSoupPortionsCount() > 0) {
            try {
                var nextOrder = ordersService.getOrder();
                if (nextOrder == null) {
                    continue;
                }

                if (!kitchenService.takeSoupPortion()) {
                    logger.info("Soup portions count is equals to 0. Waiter {} was finished ", id);
                    break;
                }

                serveSoupPortion(nextOrder);
            } catch (InterruptedException exception) {
                logger.info("Waiter {} was interrupted", id);
            }
        }
    }

    private void serveSoupPortion(Order order) throws InterruptedException {
        Thread.sleep(timePerClient);
        order.soupPortionNotifier().setSoupPortion();
    }
}
