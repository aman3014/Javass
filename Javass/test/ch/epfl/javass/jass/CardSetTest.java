package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public final class CardSetTest {
    private List<Card> listOfAllCards() {
        List<Card> cs = new ArrayList<>();
        for (Color c: Color.ALL) {
            for (Rank r: Rank.ALL) {
                cs.add(Card.of(c, r));
            }
        }
        return cs;
    }

    private CardSet nextSet(SplittableRandom rng) {
        return CardSet.ofPacked(PackedCardSetTest.nextSet(rng));
    }

    @Test
    void ofWorksWithEmtpyList() {
        CardSet s = CardSet.of(Collections.emptyList());
        assertEquals(PackedCardSet.EMPTY, s.packed());
    }

    @Test
    void ofWorksWithFullDeck() {
        CardSet s = CardSet.of(listOfAllCards());
        assertEquals(PackedCardSet.ALL_CARDS, s.packed());
    }

    @Test
    void ofWorksWithDuplicates() {
        List<Card> cs = new ArrayList<>();
        cs.addAll(listOfAllCards());
        CardSet s = CardSet.of(cs);
        assertEquals(PackedCardSet.ALL_CARDS, s.packed());
    }

    @Test
    void ofPackedWorksWithAllSingletons() {
        for (long s: PackedCardSetTest.ALL_SINGLETONS) {
            CardSet cs = CardSet.ofPacked(s);
            assertEquals(1, cs.size());
        }
    }

    @Test
    void ofPackedFailsWithInvalidBits() {
        for (int c = 0; c < 4; ++c) {
            for (int r = 9; r < 16; ++r) {
                long invalidS = (1L << r) << (c << 4);
                assertThrows(IllegalArgumentException.class, () -> {
                   CardSet.ofPacked(invalidS);
                });
            }
        }
    }

    @Test
    void packedAndOfPackedAreInverse() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = PackedCardSetTest.nextSet(rng);
            assertEquals(s, CardSet.ofPacked(s).packed());
        }
    }

    @Test
    void isEmptyWorks() {
        assertTrue(CardSet.of(Collections.emptyList()).isEmpty());
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long s = PackedCardSetTest.nextSet(rng);
            boolean empty = s == 0;
            assertEquals(empty, CardSet.ofPacked(s).isEmpty());
        }
    }

    @Test
    void sizeWorks() {
        SplittableRandom rng = newRandom();
        List<Card> cs = new ArrayList<>(listOfAllCards());
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Collections.shuffle(cs, new Random(rng.nextLong()));
            int s = rng.nextInt(cs.size());
            CardSet set = CardSet.of(cs.subList(0, s));
            assertEquals(s, set.size());
        }
    }

    @Test
    void getWorksWithValidIndex() {
        SplittableRandom rng = newRandom();
        List<Card> cs = new ArrayList<>(listOfAllCards());
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Collections.shuffle(cs, new Random(rng.nextLong()));
            int s = rng.nextInt(cs.size());
            List<Card> subCs = cs.subList(0, s);
            CardSet set = CardSet.of(subCs);
            for (int j = 0; j < s; ++j) {
                assertTrue(set.get(j) instanceof Card);
            }
        }
    }

    @Test
    @Disabled  // This was unfortunately not specified.
    void getFailsWithInvalidIndex() {
        SplittableRandom rng = newRandom();
        List<Card> cs = new ArrayList<>(listOfAllCards());
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Collections.shuffle(cs, new Random(rng.nextLong()));
            int s = rng.nextInt(cs.size());
            CardSet set = CardSet.of(cs.subList(0, s));
            int j0 = rng.nextInt();
            int j = (0 <= j0 && j0 < s) ? -j0 : j0;
            assertThrows(IndexOutOfBoundsException.class, () -> {
                set.get(j);
            });
        }
    }

    @Test
    void addCanBuildFullSet() {
        CardSet s0 = CardSet.EMPTY;
        CardSet s = s0;
        for (int c: PackedCardSetTest.ALL_PACKED_CARDS) {
            s = s.add(Card.ofPacked(c));
        }
        assertEquals(CardSet.EMPTY, s0);
        assertEquals(CardSet.ALL_CARDS, s);
    }

    @Test
    void removeCanEmptyFullSet() {
        CardSet s0 = CardSet.ALL_CARDS;
        CardSet s = s0;
        for (int c: PackedCardSetTest.ALL_PACKED_CARDS) {
            s = s.remove(Card.ofPacked(c));
        }
        assertEquals(CardSet.ALL_CARDS, s0);
        assertEquals(CardSet.EMPTY, s);
    }

    @Test
    void containsWorksOnEmptyAndFullSets() {
        for (int pkCard: PackedCardSetTest.ALL_PACKED_CARDS) {
            Card card = Card.ofPacked(pkCard);
            assertFalse(CardSet.EMPTY.contains(card));
            assertTrue(CardSet.ALL_CARDS.contains(card));
        }
    }

    @Test
    void complementWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            CardSet s = nextSet(rng);
            for (int pkCard: PackedCardSetTest.ALL_PACKED_CARDS) {
                Card card = Card.ofPacked(pkCard);
                boolean inS = s.contains(card), inSc = s.complement().contains(card);
                assertTrue((inS && ! inSc) || (! inS && inSc));
            }
        }
    }

    @Test
    void unionWorks() {
        CardSet s0 = CardSet.EMPTY;
        CardSet s = s0;
        for (long pkSingleton: PackedCardSetTest.ALL_SINGLETONS) {
            s = s.union(CardSet.ofPacked(pkSingleton));
        }
        assertEquals(CardSet.EMPTY, s0);
        assertEquals(CardSet.ALL_CARDS, s);
    }

    @Test
    void intersectionWorks() {
        CardSet s0 = CardSet.ALL_CARDS;
        for (long pkSingleton: PackedCardSetTest.ALL_SINGLETONS) {
            CardSet singleton = CardSet.ofPacked(pkSingleton);
            assertEquals(singleton, s0.intersection(singleton));
        }
        assertEquals(CardSet.ALL_CARDS, s0);
    }

    @Test
    void differenceWorks() {
        CardSet s0 = CardSet.ALL_CARDS;
        CardSet s = s0;
        int expectedSize = 36;
        for (long pkSingleton: PackedCardSetTest.ALL_SINGLETONS) {
            s = s.difference(CardSet.ofPacked(pkSingleton));
            expectedSize -= 1;
            assertEquals(expectedSize, s.size());
        }
        assertEquals(CardSet.ALL_CARDS, s0);
        assertEquals(CardSet.EMPTY, s);
    }

    @Test
    void subsetOfColorWorks() {
        for (Color c: Color.ALL) {
            CardSet s0 = CardSet.ALL_CARDS;
            CardSet subset = s0.subsetOfColor(c);
            assertEquals(9, subset.size());
            for (int i = 0; i < subset.size(); ++i) {
                assertEquals(c, subset.get(i).color());
            }
            assertEquals(CardSet.ALL_CARDS, s0);
        }
    }

    @Test
    void equalsWorksWithDifferentButEqualInstances() {
        SplittableRandom rng = newRandom();
        List<Card> cs = new ArrayList<>(listOfAllCards());
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Collections.shuffle(cs, new Random(rng.nextLong()));
            List<Card> subL = cs.subList(0, rng.nextInt(cs.size()));
            CardSet s1 = CardSet.of(subL);
            CardSet s2 = CardSet.of(subL);
            assertEquals(s1, s2);
        }
    }

    @Test
    void hashCodeIsCompatibleWithEquals() {
        SplittableRandom rng = newRandom();
        List<Card> cs = new ArrayList<>(listOfAllCards());
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Collections.shuffle(cs, new Random(rng.nextLong()));
            List<Card> subL = cs.subList(0, rng.nextInt(cs.size()));
            CardSet s1 = CardSet.of(subL);
            CardSet s2 = CardSet.of(subL);
            assertEquals(s1.hashCode(), s2.hashCode());
        }
    }
}
