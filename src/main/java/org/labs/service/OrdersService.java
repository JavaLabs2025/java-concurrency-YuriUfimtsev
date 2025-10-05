package org.labs.service;

import org.labs.model.Order;
import org.labs.model.Programmer;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class OrdersService {
    private final PriorityBlockingQueue<Order> orders;

    public OrdersService(int visitorsCount) {
        this.orders = new PriorityBlockingQueue<>(
                visitorsCount,
                Comparator.comparingInt(Order::portionsEaten)
        );
    }

    public void makeOrder(Programmer programmer) {
        var order = new Order(programmer.getId(), programmer.getEatenCount(), programmer::setHasSoupPortion);
        orders.add(order);
    }

    public Order getOrder() {
        return orders.poll();
    }
}
