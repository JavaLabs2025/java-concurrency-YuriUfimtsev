package org.labs;

import org.labs.config.DinnerConfig;
import org.labs.model.DinnerStatistics;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DinnerSimulation {
    private static final Logger logger = LoggerFactory.getLogger(DinnerSimulation.class);

    private ExecutorService programmersExecutor;
    private ExecutorService waitersExecutor;

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

    /**
     * Simulates programmers dinner based on the configuration set when creating an object.
     * The simulation time is potentially unlimited, which means
     * it will run until all the waiters and programmers have finished due to lack of food or until 365 day has passed.
     */
    public DinnerStatistics simulateDinner() throws InterruptedException {
        return simulateDinner(Duration.ofDays(365));
    }

    /**
     * Simulates programmers dinner based on the configuration set when creating an object.
     *
     * @param acceptableDuration time interval during which programmers and waiters will not be interrupted.
     *                           Counts down when all waiters have launched.
     * @return statistics of simulation.
     */
    public DinnerStatistics simulateDinner(Duration acceptableDuration) throws InterruptedException {
        // Clear state after previous iteration (if needed)
        if (this.waitersExecutor.isShutdown() || this.programmersExecutor.isShutdown()) {
            this.waitersExecutor = Executors.newFixedThreadPool(waiters.size());
            this.programmersExecutor = Executors.newFixedThreadPool(programmers.size());
        }

        logger.info("Launching {} waiters", waiters.size());
        for (var waiter : waiters) {
            waitersExecutor.submit(waiter);
        }

        logger.info("All waiters have been launched");
        var startTime = System.nanoTime();

        logger.info("Launching {} programmers", programmers.size());
        for (var programmer : programmers) {
            programmersExecutor.submit(programmer);
        }

        logger.info("All programmers have been launched. Acceptable duration: {}", acceptableDuration);

        programmersExecutor.shutdown();
        waitersExecutor.shutdown();

        var remainingNanosecond = acceptableDuration.toNanos() - (System.nanoTime() - startTime);
        var programmersFinished = programmersExecutor.awaitTermination(remainingNanosecond, TimeUnit.NANOSECONDS);
        var waitersFinished = waitersExecutor.awaitTermination(remainingNanosecond, TimeUnit.NANOSECONDS);

        var simulationTime = System.nanoTime() - startTime;

        programmersExecutor.shutdownNow();
        waitersExecutor.shutdownNow();

        var statistics = getStatistics(
                !waitersFinished || !programmersFinished,
                simulationTime
        );
        printStatistics(statistics);

        return statistics;
    }

    private static void printStatistics(DinnerStatistics statistics) {
        logger.info("------------------Dinner Statistics----------------");
        logger.info("Duration: {}", statistics.dinnerDuration());
        logger.info(statistics.isInterrupted()
                ? "Dinner was interrupted due to overtime"
                : "Dinner was successfully completed");
        logger.info("Remaining portions in the kitchen: {}", statistics.remainingPortionsInKitchen());
        logger.info("------------------Programmers eaten soups statistics: -----------------");
        statistics.visitorIdToEatenCount().forEach((key, value) ->
                logger.info("Programmer {}: {} portions were eaten", key, value)
        );
    }

    private DinnerStatistics getStatistics(boolean simulationInterrupted, long elapsedNanoseconds) {
        var remainingFood = this.kitchenService.getSoupPortionsCount();
        var visitorIdToEatenCount = programmers.stream()
                .collect(Collectors.toMap(
                        Programmer::getId,
                        Programmer::getEatenCount
                ));

        return new DinnerStatistics(
                remainingFood,
                visitorIdToEatenCount,
                Duration.ofNanos(elapsedNanoseconds),
                simulationInterrupted
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
