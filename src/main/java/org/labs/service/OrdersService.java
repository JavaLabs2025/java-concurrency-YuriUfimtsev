package org.labs.service;

import org.labs.model.Order;
import org.labs.model.Programmer;
import org.labs.model.Waiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrdersService {
    private static final Logger logger =  LoggerFactory.getLogger(OrdersService.class);

    private final PriorityBlockingQueue<Order> orders;
    private final AtomicBoolean areOrdersAccepted = new AtomicBoolean(true);

    public OrdersService(int visitorsCount) {
        this.orders = new PriorityBlockingQueue<>(
                visitorsCount,
                Comparator.comparingInt(Order::portionsEaten)
        );
    }

    /**
     * Makes an order for soup portion.
     * @param programmer who makes an order.
     * @return true if order is accepted. Otherwise, false.
     */
    public boolean makeOrder(Programmer programmer) {
        var order = new Order(
            programmer.getId(),
            programmer.getEatenCount(),
            programmer::setSoupPortion,
            programmer::noMoreSoup
        );

        if (!areOrdersAccepted.get()) return false;
        return this.orders.add(order);
    }

    public Order getOrder() {
        return this.orders.poll();
    }

    public int ordersCount() {
        return this.orders.size();
    }

    public void setOrdersNotAccepted(Waiter waiter) {
        this.areOrdersAccepted.set(false);
        logger.debug("Order's queue was closed for visitors by waiter {}", waiter.getId());
    }

    public boolean getAreOrdersAccepted() {
        return this.areOrdersAccepted.get();
    }
}
