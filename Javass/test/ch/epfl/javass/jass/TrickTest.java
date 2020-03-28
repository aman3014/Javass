package ch.epfl.javass.jass;

import static ch.epfl.javass.jass.Card.Color.CLUB;
import static ch.epfl.javass.jass.Card.Color.DIAMOND;
import static ch.epfl.javass.jass.Card.Color.HEART;
import static ch.epfl.javass.jass.Card.Color.SPADE;
import static ch.epfl.javass.jass.Card.Rank.EIGHT;
import static ch.epfl.javass.jass.Card.Rank.NINE;
import static ch.epfl.javass.jass.Card.Rank.SEVEN;
import static ch.epfl.javass.jass.Card.Rank.SIX;
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class TrickTest {
    private static Card c(Color color, Rank rank) {
        return Card.of(color, rank);
    }

    private static Card[] c(Color c1, Rank r1, Color c2, Rank r2, Color c3, Rank r3, Color c4, Rank r4) {
        return new Card[] { c(c1, r1), c(c2, r2), c(c3, r3), c(c4, r4) };
    }

    @Test
    void firstEmptyWorks() {
        for (Color trump: Color.ALL) {
            for (PlayerId firstPlayer: PlayerId.ALL) {
                Trick t = Trick.firstEmpty(trump, firstPlayer);
                assertTrue(t.isEmpty());
                assertEquals(0, t.index());
                assertEquals(trump, t.trump());
                assertEquals(firstPlayer, t.player(0));
            }
        }
    }

    @Test
    void ofPackedFailsWithInvalidTrick() {
        assertThrows(IllegalArgumentException.class, () -> {
            Trick.ofPacked(PackedTrick.INVALID);
        });
    }

    @Test
    void ofPackedAndPackedAreInverses() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int pkTrick = rng.nextInt();
            if (! PackedTrick.isValid(pkTrick))
                continue;
            assertEquals(pkTrick, Trick.ofPacked(pkTrick).packed());
        }
    }

    @Test
    void nextEmptyFailsWithNonFullTrick() {
        assertThrows(IllegalStateException.class, () -> {
            Trick
            .firstEmpty(SPADE, PlayerId.PLAYER_1)
            .nextEmpty();
        });
    }

    @Test
    void isLastWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        CardSet cs = CardSet.ALL_CARDS;
        for (int i = 0; i < 9; ++i) {
            boolean last = (i == 8);
            for (int j = 0; j < 4; ++j) {
                t = t.withAddedCard(cs.get(0));
                cs = cs.remove(cs.get(0));
                assertEquals(last, t.isLast());
            }
            t = t.nextEmpty();
        }
    }

    @Test
    void isEmptyWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        assertTrue(t.isEmpty());
        for (Card cd: c(SPADE,SIX, SPADE,SEVEN, SPADE,EIGHT, SPADE,NINE)) {
            t = t.withAddedCard(cd);
            assertFalse(t.isEmpty());
        }
    }

    @Test
    void isFullWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        for (Card cd: c(SPADE,SIX, SPADE,SEVEN, SPADE,EIGHT, SPADE,NINE)) {
            assertFalse(t.isFull());
            t = t.withAddedCard(cd);
        }
        assertTrue(t.isFull());
    }

    @Test
    void sizeWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        int s = 0;
        for (Card cd: c(SPADE,SIX, SPADE,SEVEN, SPADE,EIGHT, SPADE,NINE)) {
            assertEquals(s, t.size());
            t = t.withAddedCard(cd);
            s += 1;
        }
        assertEquals(s, t.size());
    }

    @Test
    void trumpWorks() {
        for (Color c: Color.ALL) {
            Trick t = Trick.firstEmpty(c, PlayerId.PLAYER_1);
            assertEquals(c, t.trump());
        }
    }

    @Test
    void indexWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        CardSet cs = CardSet.ALL_CARDS;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 4; ++j) {
                t = t.withAddedCard(cs.get(0));
                cs = cs.remove(cs.get(0));
                assertEquals(i, t.index());
            }
            t = t.nextEmpty();
        }
    }

    @Test
    void playerWorks() {
        for (PlayerId p: PlayerId.ALL) {
            Trick t = Trick.firstEmpty(SPADE, p);
            for (int i = 0; i < 4; ++i) {
                PlayerId expPlayer = PlayerId.ALL.get((p.ordinal() + i) % PlayerId.COUNT);
                assertEquals(expPlayer, t.player(i));
            }
        }
    }

    @Test
    void cardWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        int i = 0;
        for (Card cd: c(HEART,SIX, HEART,SEVEN, HEART,EIGHT, HEART,NINE)) {
            t = t.withAddedCard(cd);
            assertEquals(cd, t.card(i++));
        }
    }

    @Test
    void baseColorWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        for (Card cd: c(HEART,SIX, SPADE,SEVEN, DIAMOND,EIGHT, CLUB,NINE)) {
            t = t.withAddedCard(cd);
            assertEquals(HEART, t.baseColor());
        }
    }

    @Test
    void playableCardsWorks() {
        for (Color c: Color.ALL) {
            Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
            CardSet hand = CardSet.EMPTY;
            for (Rank r: Rank.ALL)
                hand = hand.add(c(c, r));
            assertEquals(hand, t.playableCards(hand));
        }
    }

    @Test
    void pointsWork() {
        int totalPoints = 0;
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        for (Rank r: Rank.ALL) {
            for (Color c: Color.ALL) {
                t = t.withAddedCard(c(c, r));
            }
            totalPoints += t.points();
            t = t.nextEmpty();
        }
        assertEquals(157, totalPoints);
    }

    @Test
    void winningPlayerWorks() {
        Trick t = Trick.firstEmpty(SPADE, PlayerId.PLAYER_3);
        for (Rank r: Rank.ALL) {
            for (Color c: Color.ALL) {
                t = t.withAddedCard(c(c, r));
                assertEquals(PlayerId.PLAYER_3, t.winningPlayer());
            }
            t = t.nextEmpty();
        }
    }

    @Test
    void equalsWorksWithDifferentButEqualInstances() {
        Trick t1 = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        Trick t2 = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        for (Rank r: Rank.ALL) {
            for (Color c: Color.ALL) {
                t1 = t1.withAddedCard(c(c, r));
                t2 = t2.withAddedCard(c(c, r));
                assertEquals(t1, t2);
            }
            t1 = t1.nextEmpty();
            t2 = t2.nextEmpty();
        }
    }

    @Test
    void hashCodesOfDifferentButEqualInstancesAreEqual() {
        Trick t1 = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        Trick t2 = Trick.firstEmpty(SPADE, PlayerId.PLAYER_1);
        for (Rank r: Rank.ALL) {
            for (Color c: Color.ALL) {
                t1 = t1.withAddedCard(c(c, r));
                t2 = t2.withAddedCard(c(c, r));
                assertEquals(t1.hashCode(), t2.hashCode());
            }
            t1 = t1.nextEmpty();
            t2 = t2.nextEmpty();
        }
    }
}
