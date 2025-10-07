package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.labs.config.DinnerConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ValidMultithreadingTests {
    @ParameterizedTest
    @Timeout(60)
    @ValueSource(ints = { 3, 5, 10, 15 })
    void noDeadlockDuringDinnerSimulationTest(int programmersCount) throws InterruptedException {
        var dinnerConfig = new DinnerConfig(
                programmersCount,
                programmersCount,
                1000,
                Duration.ofNanos(1),
                Duration.ofMillis(1),
                Duration.ofMillis(1)
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        dinnerSimulation.simulateDinner();
    }

    @Test
    void programmersShouldNotEatConsistently() throws InterruptedException {
        var dinnerConfig = new DinnerConfig(
                10,
                2,
                10,
                Duration.ofMillis(1),
                Duration.ofMillis(1),
                Duration.ofSeconds(5)
        );
        var dinnerSimulation = new DinnerSimulation(dinnerConfig);
        var statistics = dinnerSimulation.simulateDinner(Duration.ofSeconds(50));

        assertFalse(statistics.isInterrupted());
    }
}
