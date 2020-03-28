package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class PackedCardSetTest {
    private static final long EMPTY_SET = 0L;
    private static final long FULL_SET = 0x01FF_01FF_01FF_01FFL;

    static final long[] ALL_SINGLETONS = new long[] {
            0x0000000000000001L,
            0x0000000000000002L,
            0x0000000000000004L,
            0x0000000000000008L,
            0x0000000000000010L,
            0x0000000000000020L,
            0x0000000000000040L,
            0x0000000000000080L,
            0x0000000000000100L,
            0x0000000000010000L,
            0x0000000000020000L,
            0x0000000000040000L,
            0x0000000000080000L,
            0x0000000000100000L,
            0x0000000000200000L,
            0x0000000000400000L,
            0x0000000000800000L,
            0x0000000001000000L,
            0x0000000100000000L,
            0x0000000200000000L,
            0x0000000400000000L,
            0x0000000800000000L,
            0x0000001000000000L,
            0x0000002000000000L,
            0x0000004000000000L,
            0x0000008000000000L,
            0x0000010000000000L,
            0x0001000000000000L,
            0x0002000000000000L,
            0x0004000000000000L,
            0x0008000000000000L,
            0x0010000000000000L,
            0x0020000000000000L,
            0x0040000000000000L,
            0x0080000000000000L,
            0x0100000000000000L,
    };

    static final int[] ALL_PACKED_CARDS = new int[] {
            0b00_0000,
            0b00_0001,
            0b00_0010,
            0b00_0011,
            0b00_0100,
            0b00_0101,
            0b00_0110,
            0b00_0111,
            0b00_1000,
            0b01_0000,
            0b01_0001,
            0b01_0010,
            0b01_0011,
            0b01_0100,
            0b01_0101,
            0b01_0110,
            0b01_0111,
            0b01_1000,
            0b10_0000,
            0b10_0001,
            0b10_0010,
            0b10_0011,
            0b10_0100,
            0b10_0101,
            0b10_0110,
            0b10_0111,
            0b10_1000,
            0b11_0000,
            0b11_0001,
            0b11_0010,
            0b11_0011,
            0b11_0100,
            0b11_0101,
            0b11_0110,
            0b11_0111,
            0b11_1000,
    };

    static long nextSet(SplittableRandom rng) {
        return rng.nextLong() & FULL_SET;
    }

    private static int nextCard(SplittableRandom rng) {
        Color c = Color.ALL.get(rng.nextInt(Color.COUNT));
        Rank r = Rank.ALL.get(rng.nextInt(Rank.COUNT));
        return PackedCard.pack(c, r);
    }

    @Test
    void emptyHasTheRightValue() {
        assertEquals(EMPTY_SET, PackedCardSet.EMPTY);
    }

    @Test
    void allCardsHasTheRightValue() {
        assertEquals(FULL_SET, PackedCardSet.ALL_CARDS);
    }

    @Test
    void isValidAcceptsAllSingletons() {
        for (long s: ALL_SINGLETONS)
            assertTrue(PackedCardSet.isValid(s));
    }

    @Test
    void isValidRejectsAllInvalidBits() {
        for (int c = 0; c < 4; ++c) {
            for (int r = 9; r < 16; ++r) {
                long invalidS = (1L << r) << (c << 4);
                assertFalse(PackedCardSet.isValid(invalidS));
            }
        }
    }

    @Test
    void trumpAboveIsCorrect() {
        long[] expSets = new long[] {
                0x00000000000001feL,
                0x00000000000001fcL,
                0x00000000000001f8L,
                0x0000000000000020L,
                0x00000000000001e8L,
                0x0000000000000000L,
                0x00000000000001a8L,
                0x0000000000000128L,
                0x0000000000000028L,
                0x0000000001fe0000L,
                0x0000000001fc0000L,
                0x0000000001f80000L,
                0x0000000000200000L,
                0x0000000001e80000L,
                0x0000000000000000L,
                0x0000000001a80000L,
                0x0000000001280000L,
                0x0000000000280000L,
                0x000001fe00000000L,
                0x000001fc00000000L,
                0x000001f800000000L,
                0x0000002000000000L,
                0x000001e800000000L,
                0x0000000000000000L,
                0x000001a800000000L,
                0x0000012800000000L,
                0x0000002800000000L,
                0x01fe000000000000L,
                0x01fc000000000000L,
                0x01f8000000000000L,
                0x0020000000000000L,
                0x01e8000000000000L,
                0x0000000000000000L,
                0x01a8000000000000L,
                0x0128000000000000L,
                0x0028000000000000L,
        };
        int i = 0;
        for (int c: ALL_PACKED_CARDS) {
            assertEquals(expSets[i++], PackedCardSet.trumpAbove(c));
        }
    }

    @Test
    void singletonIsCorrect() {
        int i = 0;
        for (int c: ALL_PACKED_CARDS) {
            assertEquals(ALL_SINGLETONS[i++], PackedCardSet.singleton(c));
        }
    }

    @Test
    void isEmptyIsCorrect() {
        assertTrue(PackedCardSet.isEmpty(EMPTY_SET));
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            boolean expEmpty = s == 0;
            assertEquals(expEmpty, PackedCardSet.isEmpty(s));
        }
    }

    @Test
    void sizeIsCorrect() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            assertEquals(Long.bitCount(s), PackedCardSet.size(s));
        }
    }

    @Test
    void getWorksOnFullSet() {
        int i = 0;
        for (int c: ALL_PACKED_CARDS) {
            assertEquals(c, PackedCardSet.get(FULL_SET, i++));
        }
    }

    @Test
    void getWorksOnSingletons() {
        int i = 0;
        for (int c: ALL_PACKED_CARDS) {
            assertEquals(c, PackedCardSet.get(ALL_SINGLETONS[i++], 0));
        }
    }

    @Test
    void addCanBuildFullSet() {
        long s = PackedCardSet.EMPTY;
        int expectedSize = 0;
        for (int c: ALL_PACKED_CARDS) {
            s = PackedCardSet.add(s, c);
            expectedSize += 1;
            assertEquals(expectedSize, PackedCardSet.size(s));
        }
        assertEquals(FULL_SET, s);
    }

    @Test
    void addIsIdempotent() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            int c = nextCard(rng);
            s = PackedCardSet.add(s, c);
            assertEquals(s, PackedCardSet.add(s, c));
        }
    }

    @Test
    void addDoesAdd() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            int c = nextCard(rng);
            s = PackedCardSet.add(s, c);
            assertTrue(PackedCardSet.contains(s, c));
        }
    }

    @Test
    void removeCanEmptyFullSet() {
        long s = PackedCardSet.ALL_CARDS;
        int expectedSize = 36;
        for (int c: ALL_PACKED_CARDS) {
            s = PackedCardSet.remove(s, c);
            expectedSize -= 1;
            assertEquals(expectedSize, PackedCardSet.size(s));
        }
        assertEquals(EMPTY_SET, s);
    }

    @Test
    void removeIsIdempotent() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            int c = nextCard(rng);
            s = PackedCardSet.remove(s, c);
            assertEquals(s, PackedCardSet.remove(s, c));
        }
    }

    @Test
    void containsWorksOnEmptySet() {
        long s = PackedCardSet.EMPTY;
        for (int c: ALL_PACKED_CARDS) {
            assertFalse(PackedCardSet.contains(s, c));
        }
    }

    @Test
    void containsWorksOnFullSet() {
        long s = PackedCardSet.ALL_CARDS;
        for (int c: ALL_PACKED_CARDS) {
            assertTrue(PackedCardSet.contains(s, c));
        }
    }

    @Test
    void complementWorksOnEmptyAndFullSets() {
        assertEquals(PackedCardSet.ALL_CARDS, PackedCardSet.complement(PackedCardSet.EMPTY));
        assertEquals(PackedCardSet.EMPTY, PackedCardSet.complement(PackedCardSet.ALL_CARDS));
    }

    @Test
    void complementIsItsOwnInverse() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            long sC = PackedCardSet.complement(s);
            assertNotEquals(s, sC);
            assertEquals(s, PackedCardSet.complement(sC));
        }
    }

    @Test
    void sizeOfComplementIsComplementOfSize() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            long sC = PackedCardSet.complement(s);
            assertEquals(36 - PackedCardSet.size(s), PackedCardSet.size(sC));
        }
    }

    @Test
    void unionWorksOnEmptyAndFullSets() {
        assertEquals(PackedCardSet.EMPTY, PackedCardSet.union(PackedCardSet.EMPTY, PackedCardSet.EMPTY));
        assertEquals(PackedCardSet.ALL_CARDS, PackedCardSet.union(PackedCardSet.ALL_CARDS, PackedCardSet.ALL_CARDS));
    }

    @Test
    void unionWithItselfIsItself() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            assertEquals(s, PackedCardSet.union(s, s));
        }
    }

    @Test
    void unionWithComplementProducesFullSet() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            long sC = PackedCardSet.complement(s);
            assertEquals(PackedCardSet.ALL_CARDS, PackedCardSet.union(s, sC));
        }
    }

    @Test
    void unionIsAssociative() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s1 = nextSet(rng);
            long s2 = nextSet(rng);
            long s3 = nextSet(rng);

            long u1 = PackedCardSet.union(PackedCardSet.union(s1, s2), s3);
            long u2 = PackedCardSet.union(s1, PackedCardSet.union(s2, s3));

            assertEquals(u1, u2);
        }
    }

    @Test
    void unionIsCommutative() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s1 = nextSet(rng);
            long s2 = nextSet(rng);

            long u1 = PackedCardSet.union(s1, s2);
            long u2 = PackedCardSet.union(s2, s1);

            assertEquals(u1, u2);
        }
    }

    @Test
    void intersectionWorksOnEmptyAndFullSets() {
        assertEquals(PackedCardSet.EMPTY, PackedCardSet.intersection(PackedCardSet.EMPTY, PackedCardSet.EMPTY));
        assertEquals(PackedCardSet.ALL_CARDS, PackedCardSet.intersection(PackedCardSet.ALL_CARDS, PackedCardSet.ALL_CARDS));
    }

    @Test
    void intersectionWithItselfIsItself() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            assertEquals(s, PackedCardSet.intersection(s, s));
        }
    }

    @Test
    void intersectionWithComplementProducesEmptySet() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            long sC = PackedCardSet.complement(s);
            assertEquals(PackedCardSet.EMPTY, PackedCardSet.intersection(s, sC));
        }
    }

    @Test
    void intersectionIsAssociative() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s1 = nextSet(rng);
            long s2 = nextSet(rng);
            long s3 = nextSet(rng);

            long u1 = PackedCardSet.intersection(PackedCardSet.intersection(s1, s2), s3);
            long u2 = PackedCardSet.intersection(s1, PackedCardSet.intersection(s2, s3));

            assertEquals(u1, u2);
        }
    }

    @Test
    void intersectionIsCommutative() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s1 = nextSet(rng);
            long s2 = nextSet(rng);

            long u1 = PackedCardSet.intersection(s1, s2);
            long u2 = PackedCardSet.intersection(s2, s1);

            assertEquals(u1, u2);
        }
    }

    @Test
    void differenceWorksOnEmptyAndFullSets() {
        assertEquals(PackedCardSet.EMPTY, PackedCardSet.difference(PackedCardSet.EMPTY, PackedCardSet.EMPTY));
        assertEquals(PackedCardSet.EMPTY, PackedCardSet.difference(PackedCardSet.ALL_CARDS, PackedCardSet.ALL_CARDS));
    }

    @Test
    void differenceWithItselfIsEmpty() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            assertEquals(PackedCardSet.EMPTY, PackedCardSet.difference(s, s));
        }
    }

    @Test
    void differenceWithComplementIsItself() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            long sC = PackedCardSet.complement(s);
            assertEquals(s, PackedCardSet.difference(s, sC));
        }
    }

    @Test
    void differenceDoesNotMakeSetGrow() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s1 = nextSet(rng);
            long s2 = nextSet(rng);
            long d12 = PackedCardSet.difference(s1, s2);
            assertTrue(PackedCardSet.size(d12) <= PackedCardSet.size(s1));
            long d21 = PackedCardSet.difference(s2, s1);
            assertTrue(PackedCardSet.size(d21) <= PackedCardSet.size(s2));
        }
    }

    @Test
    void subsetOfColorHasRightSize() {
        for (Color c: Color.ALL) {
            assertEquals(9, PackedCardSet.size(PackedCardSet.subsetOfColor(PackedCardSet.ALL_CARDS, c)));
        }
    }

    @Test
    void subsetOfColorWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = nextSet(rng);
            Color c = Color.ALL.get(rng.nextInt(Color.COUNT));

            long expectedS = PackedCardSet.EMPTY;
            for (int j = 0; j < PackedCardSet.size(s); ++j) {
                int pkCard = PackedCardSet.get(s, j);
                if (PackedCard.color(pkCard) == c)
                    expectedS = PackedCardSet.add(expectedS, pkCard);
            }

            assertEquals(expectedS, PackedCardSet.subsetOfColor(s, c));
        }
    }
}
