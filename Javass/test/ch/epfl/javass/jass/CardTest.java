package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public final class CardTest {
    private static Card.Color[] getAllColors() {
        return new Card.Color[] {
                Card.Color.SPADE,
                Card.Color.HEART,
                Card.Color.DIAMOND,
                Card.Color.CLUB
        };
    }

    private static Card.Rank[] getAllRanks() {
        return new Card.Rank[] {
                Card.Rank.SIX,
                Card.Rank.SEVEN,
                Card.Rank.EIGHT,
                Card.Rank.NINE,
                Card.Rank.TEN,
                Card.Rank.JACK,
                Card.Rank.QUEEN,
                Card.Rank.KING,
                Card.Rank.ACE,
        };
    }

    private static Card.Rank[] getAllRanksTrumpOrdered() {
        return new Card.Rank[] {
                Card.Rank.SIX,
                Card.Rank.SEVEN,
                Card.Rank.EIGHT,
                Card.Rank.TEN,
                Card.Rank.QUEEN,
                Card.Rank.KING,
                Card.Rank.ACE,
                Card.Rank.NINE,
                Card.Rank.JACK,
        };
    }

    private static Card[] getAllCards() {
        Card[] allCards = new Card[36];
        int i = 0;
        for (Card.Color c: getAllColors()) {
            for (Card.Rank r: getAllRanks()) {
                allCards[i++] = Card.of(c, r);
            }
        }
        return allCards;
    }

    @Test
    void cardColorsAreInRightOrder() {
        assertArrayEquals(getAllColors(), Card.Color.values());
    }

    @Test
    void cardColorAllIsCorrect() {
        assertEquals(Arrays.asList(Card.Color.values()), Card.Color.ALL);
    }

    @Test
    void cardColorCountIsCorrect() {
        assertEquals(getAllColors().length, Card.Color.COUNT);
    }

    @Test
    void cardRanksAreInRightOrder() {
        assertArrayEquals(getAllRanks(), Card.Rank.values());
    }

    @Test
    void cardRankAllIsCorrect() {
        assertEquals(Arrays.asList(Card.Rank.values()), Card.Rank.ALL);
    }

    @Test
    void cardRankCountIsCorrect() {
        assertEquals(getAllRanks().length, Card.Rank.COUNT);
    }

    @Test
    void trumpOrdinalIsCorrect() throws Exception {
        Card.Rank[] trumpRanks = getAllRanksTrumpOrdered();
        for (int i = 0; i < trumpRanks.length; ++i)
            assertEquals(i, trumpRanks[i].trumpOrdinal());
    }

    @Test
    void ofWorks() {
        for (Card.Color c: getAllColors()) {
            for (Card.Rank r: getAllRanks()) {
                Card card = Card.of(c, r);
                assertEquals(c, card.color());
                assertEquals(r, card.rank());
            }
        }
    }

    @Test
    void ofPackedWorks() throws Exception {
        for (Card.Color c: getAllColors()) {
            for (Card.Rank r: getAllRanks()) {
                int pkCard = PackedCard.pack(c, r);
                Card card = Card.ofPacked(pkCard);
                assertEquals(c, card.color());
                assertEquals(r, card.rank());
            }
        }
    }

    @Test
    void isBetterWorksWithTrumpAndNonTrumpCards() throws Exception {
        for (Card.Color trump: getAllColors()) {
            for (Card.Rank trumpRank: getAllRanks()) {
                Card trumpCard = Card.of(trump, trumpRank);
                for (Card.Color c: getAllColors()) {
                    if (c == trump)
                        continue;
                    for (Card.Rank r: getAllRanks()) {
                        Card card = Card.of(c, r);
                        assertTrue(trumpCard.isBetter(trump, card));
                        assertFalse(card.isBetter(trump, trumpCard));
                    }
                }
            }
        }
    }

    @Test
    void isBetterWorksWithTrumpCards() throws Exception {
        Card.Rank[] trumpRanks = getAllRanksTrumpOrdered();
        for (Card.Color trump: getAllColors()) {
            for (int i = 0; i < trumpRanks.length - 1; ++i) {
                Card cardLow = Card.of(trump, trumpRanks[i]);
                for (int j = i + 1; j < trumpRanks.length; ++j) {
                    Card cardHigh = Card.of(trump, trumpRanks[j]);
                    assertTrue(cardHigh.isBetter(trump, cardLow));
                    assertFalse(cardLow.isBetter(trump, cardHigh));
                }
            }
        }
    }

    @Test
    void isBetterWorksWitNonTrumpCards() throws Exception {
        Card.Rank[] nonTrumpRanks = getAllRanks();
        for (Card.Color trump: getAllColors()) {
            for (Card.Color c: getAllColors()) {
                if (c == trump)
                    continue;
                for (int i = 0; i < nonTrumpRanks.length - 1; ++i) {
                    Card cardLow = Card.of(c, nonTrumpRanks[i]);
                    for (int j = i + 1; j < nonTrumpRanks.length; ++j) {
                        Card cardHigh = Card.of(c, nonTrumpRanks[j]);
                        assertTrue(cardHigh.isBetter(trump, cardLow));
                        assertFalse(cardLow.isBetter(trump, cardHigh));
                    }
                }
            }
        }
    }

    @Test
    void isBetterWorksWithIncomparableCards() throws Exception {
        for (Card.Color c1: getAllColors()) {
            for (Card.Color c2: getAllColors()) {
                if (c2 == c1)
                    continue;
                Card.Color trump = null;
                for (Card.Color t: getAllColors()) {
                    if (t != c1 && t != c2) {
                        trump = t;
                        break;
                    }
                }
                for (Card.Rank r1: getAllRanks()) {
                    Card card1 = Card.of(c1, r1);
                    for (Card.Rank r2: getAllRanks()) {
                        Card card2 = Card.of(c2, r2);
                        assertFalse(card1.isBetter(trump, card1));
                        assertFalse(card2.isBetter(trump, card2));
                    }
                }
            }
        }
    }

    @Test
    void pointsSumTo152() {
        for (Card.Color trump: Card.Color.values()) {
            int totalPoints = 0;
            for (Card card: getAllCards())
                totalPoints += card.points(trump);
            assertEquals(152, totalPoints);
        }
    }

    @Test
    void equalsIsCorrect() {
        Card[] cards1 = getAllCards();
        Card[] cards2 = getAllCards();
        for (int i1 = 0; i1 < cards1.length; ++i1) {
            for (int i2 = 0; i2 < cards2.length; ++i2) {
                assertEquals((i1 == i2), cards1[i1].equals(cards2[i2]));
            }
        }
    }

    @Test
    void allHashCodesAreDifferent() {
        Card[] allCards = getAllCards();
        int[] allHashCodes = new int[allCards.length];
        for (int i = 0; i < allCards.length; ++i)
            allHashCodes[i] = allCards[i].hashCode();
        Arrays.sort(allHashCodes);
        for (int i = 1; i < allHashCodes.length; ++i)
            assertTrue(allHashCodes[i] != allHashCodes[i - 1]);
    }
}
