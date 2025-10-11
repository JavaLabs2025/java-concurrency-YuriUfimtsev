package org.labs;

import org.labs.config.DinnerConfig;
import org.labs.model.*;
import org.labs.service.KitchenService;
import org.labs.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

public class DinnerSimulation {
    private static final Logger logger = LoggerFactory.getLogger(DinnerSimulation.class);

    private final long initialPortionsCount;

    private final KitchenService kitchenService;
    private final OrdersService ordersService;

    private final ArrayList<Spoon> spoons;
    private final List<Programmer> programmers;
    private final List<Waiter> waiters;

    public DinnerSimulation(DinnerConfig config) {
        this.kitchenService = new KitchenService(config.soupPortionsCount());
        this.ordersService = new OrdersService(config.visitorsCount());

        this.spoons = createSpoons(config.visitorsCount());
        this.programmers = createProgrammers(
                config.visitorsCount(),
                config.visitorsDiscussionDelay(),
                config.visitorsEatingDelay()
        );

        this.waiters = createWaiters(config.waitersCount(), config.waitersServingDelay());
        this.initialPortionsCount = config.soupPortionsCount();
    }

    /**
     * Simulates programmers dinner based on the configuration set when creating an object.
     *
     * @return statistics of simulation.
     */
    public DinnerStatistics simulateDinner() throws ExecutionException, InterruptedException {
        var programmersExecutor = Executors.newFixedThreadPool(programmers.size());
        var waitersExecutor = Executors.newFixedThreadPool(waiters.size());
        try {

            logger.info("Launching {} waiters", waiters.size());
            var waitersFutures = waiters.stream()
                    .map(waitersExecutor::submit)
                    .toList();

            var startTime = System.nanoTime();

            logger.info("Launching {} programmers", programmers.size());
            var programmersFutures = programmers.stream()
                    .map(programmersExecutor::submit)
                    .toList();

            // Stop accepting new tasks
            programmersExecutor.shutdown();
            waitersExecutor.shutdown();

            // Wait for the waiters to deliver all portions and finish
            for (Future<?> future : waitersFutures) {
                future.get();
            }

            // Wait for the programmers to finish
            for (Future<?> future : programmersFutures) {
                future.get();
            }

            // Waits for terminating executors
            waitersExecutor.close();
            programmersExecutor.close();

            var simulationTime = System.nanoTime() - startTime;
            var statistics = getStatistics(simulationTime);
            printStatistics(statistics);

            return statistics;
        } catch (InterruptedException | ExecutionException e) {
            waitersExecutor.shutdownNow();
            programmersExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private void printStatistics(DinnerStatistics statistics) {
        logger.info("------------------Dinner Statistics----------------");
        logger.info("Programmers count: {}, waiters count: {}", this.programmers.size(), this.waiters.size());
        logger.info("Soup portions count: {}", this.initialPortionsCount);
        logger.info("Duration: {}", statistics.dinnerDuration());
        logger.info("Dinner was successfully completed");
        logger.info("Remaining portions in the kitchen: {}", statistics.remainingPortionsInKitchen());
        logger.info("------------------Programmers eaten soups statistics: -----------------");
        statistics.visitorIdToEatenCount().forEach((key, value) ->
                logger.info("Programmer {}: {} portions were eaten", key, value)
        );
    }

    private DinnerStatistics getStatistics(long elapsedNanoseconds) {
        var remainingFood = this.kitchenService.getSoupPortionsCount();
        var visitorIdToEatenCount = programmers.stream()
                .collect(Collectors.toMap(
                        Programmer::getId,
                        Programmer::getEatenCount
                ));

        return new DinnerStatistics(
                remainingFood,
                visitorIdToEatenCount,
                Duration.ofNanos(elapsedNanoseconds)
        );
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

    private List<Programmer> createProgrammers(int visitorsCount, DurationRange discussionRange, DurationRange eatingRange) {
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
                    discussionRange,
                    eatingRange
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
