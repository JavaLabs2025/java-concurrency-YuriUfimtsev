package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.labs.config.DinnerConfig;
import org.labs.model.DurationRange;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

class ValidMultithreadingTests {
    @ParameterizedTest
    @Timeout(120)
    @ValueSource(ints = { 3, 5, 10, 15 })
    void noDeadlockDuringDinnerSimulationTest(int programmersCount) throws InterruptedException, ExecutionException {
        var dinnerConfig = new DinnerConfig(
                programmersCount,
                programmersCount,
                1000,
                Duration.ofMillis(1),
                new DurationRange(Duration.ofMillis(1), Duration.ofMillis(2)),
                new DurationRange(Duration.ofMillis(5), Duration.ofMillis(6))
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        dinnerSimulation.simulateDinner();
    }

    @Timeout(50)
    @Test
    void programmersShouldNotEatConsistently() throws InterruptedException, ExecutionException {
        var dinnerConfig = new DinnerConfig(
                10,
                2,
                10,
                Duration.ofMillis(1),
                new DurationRange(Duration.ofMillis(0), Duration.ofMillis(1)),
                new DurationRange(Duration.ofSeconds(5), Duration.ofSeconds(6))
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        dinnerSimulation.simulateDinner();
    }
}
