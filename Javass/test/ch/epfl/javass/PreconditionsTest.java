package ch.epfl.javass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public final class PreconditionsTest {
    @Test
    void checkArgumentSucceedsForTrue() {
        Preconditions.checkArgument(true);
    }

    @Test
    void checkArgumentFailsForFalse() {
        assertThrows(IllegalArgumentException.class, () -> {
            Preconditions.checkArgument(false);
        });
    }

    @Test
    void checkIndexSucceedsForValidIndices() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int size = rng.nextInt(1000);
            int index = rng.nextInt(size);
            Preconditions.checkIndex(index, size);
        }
    }

    @Test
    void checkIndexFailsForTooBigIndex() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int size = rng.nextInt(1000);
            int index = size + rng.nextInt(5);
            assertThrows(IndexOutOfBoundsException.class, () -> {
                Preconditions.checkIndex(index, size);
            });
        }
    }

    @Test
    void checkIndexFailsForNegativeIndex() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int size = rng.nextInt(1000);
            int index = -(1 + rng.nextInt(size));
            assertThrows(IndexOutOfBoundsException.class, () -> {
                Preconditions.checkIndex(index, size);
            });
        }
    }

    @Test
    void checkIndexFailsForNegativeSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int size = rng.nextInt(1000);
            int index = rng.nextInt(size);
            assertThrows(IndexOutOfBoundsException.class, () -> {
                Preconditions.checkIndex(index, -size);
            });
        }
    }
}
