package org.labs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.labs.config.DinnerConfig;
import org.labs.model.DurationRange;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DinnerSimulationTests {
    @ParameterizedTest
    @ValueSource(ints = { 1, 100, 1000, 10_000 })
    void noRemainingPortionsAfterSimulationTest(int initialPortionsCount)
            throws ExecutionException, InterruptedException {
        var dinnerConfig = new DinnerConfig(
                7,
                2,
                initialPortionsCount,
                Duration.ofMillis(5),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20)),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20))
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        var statistics = dinnerSimulation.simulateDinner();

        assertEquals(0, statistics.remainingPortionsInKitchen(),
                "All soup portions should be eaten");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 100, 1000, 10_000 })
    void sumOfProgrammersEatenShouldBeEqualToTheInitialPortionsTest(int initialPortionsCount)
            throws InterruptedException, ExecutionException {
        var dinnerConfig = new DinnerConfig(
                7,
                2,
                initialPortionsCount,
                Duration.ofMillis(5),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20)),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20))
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        var statistics = dinnerSimulation.simulateDinner();

        var programmersEaten = statistics.visitorIdToEatenCount().values().stream()
                .mapToInt(Integer::intValue).sum();

        assertEquals(initialPortionsCount, programmersEaten,
                "Programmers eaten portions sum should be equal to the initial portions count");
    }

    @ParameterizedTest
    @ValueSource(ints = { 5, 10, 15, 30 })
    void programmersEatingDifferenceShouldNoMoreThanFivePercentTest(int visitorsCount)
            throws InterruptedException, ExecutionException {
        var initialPortionsCount = 10_000;
        var dinnerConfig = new DinnerConfig(
                visitorsCount,
                5,
                initialPortionsCount,
                Duration.ofNanos(5),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20)),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(20))
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        var statistics = dinnerSimulation.simulateDinner();

        var eatenCount = statistics.visitorIdToEatenCount().values();
        int minEaten = eatenCount.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxEaten = eatenCount.stream().mapToInt(Integer::intValue).max().orElse(0);

        var percentageDifference = ((maxEaten - minEaten) / (double)initialPortionsCount) * 100;

        assertTrue(Double.compare(percentageDifference, 5) <= 0,
                "Percentage difference between min and max eaten count should be less than or equal to " + 0.5);
    }
}
