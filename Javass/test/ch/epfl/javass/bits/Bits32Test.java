package ch.epfl.javass.bits;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public final class Bits32Test {
    @Test
    void maskProducesCorrectMasks() {
        for (int size = 0; size <= Integer.SIZE; ++size) {
            for (int start = 0; start <= Integer.SIZE - size; ++start) {
                int m = Bits32.mask(start, size);
                assertEquals(size, Integer.bitCount(m));
                assertEquals(size, Integer.bitCount(m >>> start));
                if (start + size < Integer.SIZE)
                    assertEquals(0, m >>> (start + size));
            }
        }
    }

    @Test
    void maskFailsWithNegativeStart() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = -(i + 1);
            int size = rng.nextInt(Integer.SIZE + 1);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.mask(start, size);
            });
        }
    }

    @Test
    void maskFailsWithNegativeSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = rng.nextInt(Integer.SIZE + 1);
            int size = -(i + 1);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.mask(start, size);
            });
        }
    }

    @Test
    void maskFailsWithTooBigSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = rng.nextInt(Integer.SIZE + 1);
            int size = Integer.SIZE - start + rng.nextInt(1, 10);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.mask(start, size);
            });
        }
    }

    @Test
    void extractCanExtractAllBitsInOneGo() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int bits = rng.nextInt();
            assertEquals(bits, Bits32.extract(bits, 0, Integer.SIZE));
        }
    }

    @Test
    void extractCanExtractSubgroupsOfBits() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int bits = rng.nextInt();
            for (int size = 1; size <= Integer.SIZE; size *= 2) {
                int reComputedBits = 0;
                for (int start = Integer.SIZE - size; start >= 0; start -= size)
                    reComputedBits = (reComputedBits << size) | Bits32.extract(bits, start, size);
                assertEquals(bits, reComputedBits);
            }
        }
    }

    @Test
    void extractFailsWithNegativeStart() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = -(i + 1);
            int size = rng.nextInt(Integer.SIZE + 1);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.extract(rng.nextInt(), start, size);
            });
        }
    }

    @Test
    void extractFailsWithNegativeSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = rng.nextInt(Integer.SIZE + 1);
            int size = -(i + 1);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.extract(rng.nextInt(), start, size);
            });
        }
    }

    @Test
    void extractFailsWithTooBigSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int start = rng.nextInt(Integer.SIZE + 1);
            int size = Integer.SIZE - start + rng.nextInt(1, 10);
            assertThrows(IllegalArgumentException.class, () -> {
                Bits32.extract(rng.nextInt(), start, size);
            });
        }
    }

    private int[] getSizes(SplittableRandom rng, int n) {
        int[] sizes = new int[n];
        int remainingBits = Integer.SIZE;
        for (int i = 0; i < n; ++i) {
            sizes[i] = rng.nextInt(1, remainingBits - (n - 1 - i) + 1);
            remainingBits -= sizes[i];
        }
        return sizes;
    }

    private int[] getValues(SplittableRandom rng, int[] sizes) {
        int[] values = new int[sizes.length];
        for (int i = 0; i < sizes.length; ++i)
            values[i] = (int) rng.nextLong(1L << sizes[i]);
        return values;
    }

    @Test
    void pack2Works() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int[] s = getSizes(rng, 2);
            int[] v = getValues(rng, s);
            int packed = Bits32.pack(v[0], s[0], v[1], s[1]);
            for (int j = 0; j < s.length; ++j) {
              assertEquals(v[j], packed & ((1 << s[j]) - 1));
              packed >>>= s[j];
            }
            assertEquals(0, packed);
        }
    }

    @Test
    void pack2FailsWithTooBigValues() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b100, 2, 0b11, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b100, 3, 0b11, 1);
        });
    }

    @Test
    void pack2FailsForTooManyBits() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0, 16, 0, 17);
        });
    }

    @Test
    void pack3Works() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int[] s = getSizes(rng, 3);
            int[] v = getValues(rng, s);
            int packed = Bits32.pack(v[0], s[0], v[1], s[1], v[2], s[2]);
            for (int j = 0; j < s.length; ++j) {
                assertEquals(v[j], packed & ((1 << s[j]) - 1));
                packed >>>= s[j];
            }
            assertEquals(0, packed);
        }
    }

    @Test
    void pack3FailsWithTooBigValues() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b100, 2, 0b11, 2, 0b1111, 4);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b100, 3, 0b11, 1, 0b1111, 4);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b100, 3, 0b11, 2, 0b1111, 3);
        });
    }

    @Test
    void pack3FailsForTooManyBits() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0, 11, 0, 11, 0, 11);
        });
    }

    @Test
    void pack7Works() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int[] s = getSizes(rng, 7);
            int[] v = getValues(rng, s);
            int packed = Bits32.pack(v[0], s[0], v[1], s[1], v[2], s[2], v[3], s[3], v[4], s[4], v[5], s[5], v[6], s[6]);
            for (int j = 0; j < s.length; ++j) {
                assertEquals(v[j], packed & ((1 << s[j]) - 1));
                packed >>>= s[j];
            }
            assertEquals(0, packed);
        }
    }

    @Test
    void pack7FailsWithTooBigValues() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 1, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 1, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 2, 0b10, 1, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 2, 0b10, 2, 0b10, 1, 0b10, 2, 0b10, 2, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 1, 0b10, 2, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 1, 0b10, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 2, 0b10, 1);
        });
    }

    @Test
    void pack7FailsForTooManyBits() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32.pack(0, 4, 0, 4, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5);
        });
    }
}
