package org.labs;

import org.labs.config.DinnerConfig;
import org.labs.model.Programmer;
import org.labs.model.Spoon;
import org.labs.model.Waiter;
import org.labs.service.KitchenService;
import org.labs.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DinnerSimulation {
    private static final Logger logger = LoggerFactory.getLogger(DinnerSimulation.class);

    private final ExecutorService programmersExecutor;
    private final ExecutorService waitersExecutor;

    private final KitchenService kitchenService;
    private final OrdersService ordersService;

    private final ArrayList<Spoon> spoons;
    private final List<Programmer> programmers;
    private final List<Waiter> waiters;

    public DinnerSimulation(DinnerConfig config) {
        this.programmersExecutor = Executors.newFixedThreadPool(config.visitorsCount());
        this.waitersExecutor = Executors.newFixedThreadPool(config.waitersCount());

        this.kitchenService = new KitchenService(config.soupPortionsCount());
        this.ordersService = new OrdersService(config.visitorsCount());

        this.spoons = createSpoons(config.visitorsCount());
        this.programmers = createProgrammers(
                config.visitorsCount(),
                config.visitorsDiscussionDelay(),
                config.visitorsEatingDelay()
        );

        this.waiters = createWaiters(config.waitersCount(), config.waitersServingDelay());
    }

    public void simulateDinner() {
        for (var programmer: programmers) {
            programmersExecutor.submit(programmer);
        }

        for (var waiter: waiters) {
            waitersExecutor.submit(waiter);
        }

    }

    private List<Waiter> createWaiters(int waitersCount, Duration servingDelay) {
        var waiters = new ArrayList<Waiter>(waitersCount);
        for (var i = 0; i < waitersCount; i++) {
            var waiter = new Waiter(
                i + 1,
                    this.ordersService,
                    this.kitchenService,
                    servingDelay
            );
            waiters.add(waiter);
        }
        return waiters;
    }

    private List<Programmer> createProgrammers(int visitorsCount, Duration discussionDelay, Duration eatingDelay) {
        var programmers = new ArrayList<Programmer>(visitorsCount);
        for (var i = 0; i < visitorsCount; i++) {
            var firstSpoonIndex = i;
            var secondSpoonIndex = (i + 1) % visitorsCount;

            var leftSpoon = this.spoons.get(Math.min(firstSpoonIndex, secondSpoonIndex));
            var rightSpoon = this.spoons.get(Math.max(firstSpoonIndex, secondSpoonIndex));

            var programmer = new Programmer(
                    i + 1,
                    leftSpoon,
                    rightSpoon,
                    this.ordersService,
                    this.kitchenService,
                    discussionDelay,
                    eatingDelay
            );
            programmers.add(programmer);
        }
        return programmers;
    }

    private static ArrayList<Spoon> createSpoons(int visitorsCount) {
        var spoons = new ArrayList<Spoon>(visitorsCount);
        for (var i = 0; i < visitorsCount; i++) {
            var spoon = new Spoon(i + 1);
            spoons.add(spoon);
        }
        return spoons;
    }
}
