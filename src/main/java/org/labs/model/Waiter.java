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

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            logger.debug("Waiter {} is running", id);

            while (kitchenService.getSoupPortionsCount() > 0) {
                var nextOrder = ordersService.getOrder();
                if (nextOrder == null) {
                    continue;
                }

                if (!kitchenService.takeSoupPortion()) {
                    nextOrder.noMorePortionsNotifier().notifyVisitor();
                    logger.debug("Soup portions count equals to 0. Waiter {} was finished ", id);
                    break;
                }

                serveSoupPortion(nextOrder);
            }

            // Closes queue for new orders
            if (ordersService.getAreOrdersAccepted()) {
                ordersService.setOrdersNotAccepted(this);
            }

            // Notifies the visitors (who have already placed orders) that the portions have run out.
            // Также дополнительная проверка на случай программистов, публикующих заказ и не знающих, что очередь закрыта.
            var retryCount = 0;
            while (retryCount < 3 && ordersService.ordersCount() > 0) {
                var nextOrder = ordersService.getOrder();
                if (nextOrder != null) {
                    nextOrder.noMorePortionsNotifier().notifyVisitor();
                }
                retryCount++;
            }

            logger.debug("Soup portions count equals to 0. Waiter {} was finished ", id);
        } catch (InterruptedException exception) {
            logger.warn("Waiter {} was interrupted", id);
            Thread.currentThread().interrupt();
        }
    }

    private void serveSoupPortion(Order order) throws InterruptedException {
        logger.debug("Waiter {} starts serving soup to the visitor {} for {}",
                id, order.visitorId(), timePerClient);
        Thread.sleep(timePerClient);
        order.soupPortionNotifier().setSoupPortion();
    }
}
