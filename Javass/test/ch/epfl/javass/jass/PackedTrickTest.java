package ch.epfl.javass.jass;

import static ch.epfl.javass.jass.Card.Color.CLUB;
import static ch.epfl.javass.jass.Card.Color.DIAMOND;
import static ch.epfl.javass.jass.Card.Color.HEART;
import static ch.epfl.javass.jass.Card.Color.SPADE;
import static ch.epfl.javass.jass.Card.Rank.ACE;
import static ch.epfl.javass.jass.Card.Rank.EIGHT;
import static ch.epfl.javass.jass.Card.Rank.JACK;
import static ch.epfl.javass.jass.Card.Rank.KING;
import static ch.epfl.javass.jass.Card.Rank.NINE;
import static ch.epfl.javass.jass.Card.Rank.QUEEN;
import static ch.epfl.javass.jass.Card.Rank.SIX;
import static ch.epfl.javass.jass.Card.Rank.TEN;
import static ch.epfl.javass.jass.PlayerId.PLAYER_1;
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class PackedTrickTest {
    // Short syntax to create cards, DO NOT DO THIS IN YOUR PROGRAM!!!
    private static int c(Color color, Rank rank) {
        return PackedCard.pack(color, rank);
    }

    private static long cardSet(int... pkCards) {
        long pkCardSet = PackedCardSet.EMPTY;
        for (int pkCard: pkCards)
            pkCardSet = PackedCardSet.add(pkCardSet, pkCard);
        return pkCardSet;
    }

    private static int trick(int index, Color trump, PlayerId firstPlayer, int... pkCards) {
        int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
        for (int pkCard: pkCards)
            pkTrick = PackedTrick.withAddedCard(pkTrick, pkCard);
        pkTrick = (pkTrick & ~(0b1111 << 24)) | (index << 24);
        return pkTrick;
    }

    private static int indexForHand(long pkHand) {
        return 9 - PackedCardSet.size(pkHand);
    }

    private static final List<Long> ALL_SINGLETONS =
            Collections.unmodifiableList(
                    Arrays.asList(
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
                            0x0100000000000000L));

    private static long nextCardSet(SplittableRandom rng, int size) {
        List<Long> cards = new ArrayList<>(ALL_SINGLETONS);
        Collections.shuffle(cards, new Random(rng.nextLong()));
        long s = 0;
        for (long l: cards.subList(0, size))
            s |= l;
        assert Long.bitCount(s) == size;
        return s;
    }

    private static Color nextColor(SplittableRandom rng) {
        return Color.ALL.get(rng.nextInt(Color.COUNT));
    }

    private static Rank nextRank(SplittableRandom rng) {
        return Rank.ALL.get(rng.nextInt(Rank.COUNT));
    }

    private static PlayerId nextPlayerId(SplittableRandom rng) {
        return PlayerId.ALL.get(rng.nextInt(PlayerId.COUNT));
    }

    private static int nextPackedCard(SplittableRandom rng) {
        return PackedCard.pack(nextColor(rng), nextRank(rng));
    }

    @Test
    void packedTrickInvalidHasCorrectValue() {
        assertEquals(~0, PackedTrick.INVALID);
    }

    @Test
    void isValidWorksWithZeroToFourValidCards() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int pkTrick = ~0;
            for (int size = 0; size <= 4; ++size)
                pkTrick = (pkTrick << 6) | nextPackedCard(rng);
            pkTrick &= 0b111111_111111_111111_111111;
            assertTrue(PackedTrick.isValid(pkTrick));
        }
    }

    @Test
    void isValidWorksWithInvalidMixOfValidAndInvalidCards() {
        int[][] invalidCardPos = new int[][] {
            { 0 },
            { 1 },
            { 2 },
            { 0, 1 },
            { 0, 2 },
            { 0, 3 },
            { 1, 2 },
            { 1, 3 },
            { 0, 1, 2 },
            { 0, 1, 3 },
            { 0, 2, 3 },
        };

        SplittableRandom rng = newRandom();
        for (int[] invalidCards: invalidCardPos) {
            int pkTrick = 0;
            for (int i = 0; i < 4; ++i)
                pkTrick = (pkTrick << 6) | nextPackedCard(rng);
            for (int i: invalidCards)
                pkTrick |= PackedCard.INVALID << (i * 6);
            assertFalse(PackedTrick.isValid(pkTrick));
        }
    }

    @Test
    void firstEmptyWorks() {
        for (Color trump: Color.ALL) {
            for (PlayerId firstPlayer: PlayerId.ALL) {
                int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
                assertTrue(PackedTrick.isEmpty(pkTrick));
                assertEquals(0, PackedTrick.index(pkTrick));
                assertEquals(trump, PackedTrick.trump(pkTrick));
                assertEquals(firstPlayer, PackedTrick.player(pkTrick, 0));
            }
        }
    }

    @Test
    void nextEmptyWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                assertTrue(PackedTrick.isEmpty(pkTrick));
                assertEquals(j, PackedTrick.index(pkTrick));
                assertEquals(trump, PackedTrick.trump(pkTrick));

                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k)
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
            assertEquals(PackedTrick.INVALID, pkTrick);
        }
    }

    @Test
    void isLastWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 8; ++j) {
                assertFalse(PackedTrick.isLast(pkTrick));

                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k)
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
            assertTrue(PackedTrick.isLast(pkTrick));
        }
    }

    @Test
    void isEmptyWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                assertTrue(PackedTrick.isEmpty(pkTrick));
                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k) {
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                    assertFalse(PackedTrick.isEmpty(pkTrick));
                }
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void isFullWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k) {
                    assertFalse(PackedTrick.isFull(pkTrick));
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                }
                assertTrue(PackedTrick.isFull(pkTrick));
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void sizeWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                long pkTrickCards = nextCardSet(rng, 4);
                assertEquals(0, PackedTrick.size(pkTrick));
                for (int k = 0; k < 4; ++k) {
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                    assertEquals(k + 1, PackedTrick.size(pkTrick));
                }
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void trumpWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                long pkTrickCards = nextCardSet(rng, 4);
                assertEquals(trump, PackedTrick.trump(pkTrick));
                for (int k = 0; k < 4; ++k) {
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                    assertEquals(trump, PackedTrick.trump(pkTrick));
                }
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void playerWorks() {
        for (Color trump: Color.ALL) {
            for (PlayerId firstPlayer: PlayerId.ALL) {
                int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
                PlayerId player = firstPlayer;
                for (int j = 0; j < PlayerId.COUNT; ++j) {
                    assertEquals(player, PackedTrick.player(pkTrick, j));
                    player = PlayerId.ALL.get((player.ordinal() + 1) % PlayerId.COUNT);
                }
            }
        }
    }

    @Test
    void indexWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k) {
                    assertEquals(j, PackedTrick.index(pkTrick));
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                }
                assertEquals(j, PackedTrick.index(pkTrick));
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void cardWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            for (int j = 0; j < 9; ++j) {
                long pkTrickCards = nextCardSet(rng, 4);
                for (int k = 0; k < 4; ++k) {
                    pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, k));
                    for (int l = 0; l <= k; ++l)
                        assertEquals(PackedCardSet.get(pkTrickCards, l), PackedTrick.card(pkTrick, l));
                }
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void baseColorWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int pkTrick = PackedTrick.firstEmpty(nextColor(rng), nextPlayerId(rng));
            int trickSize = rng.nextInt(1, 5);
            long pkTrickCards = nextCardSet(rng, trickSize);
            for (int j = 0; j < trickSize; ++j)
                pkTrick = PackedTrick.withAddedCard(pkTrick, PackedCardSet.get(pkTrickCards, j));
            Color baseColor = PackedCard.color(PackedCardSet.get(pkTrickCards, 0));
            assertEquals(baseColor, PackedTrick.baseColor(pkTrick));
        }
    }

    @Test
    void allCardsArePlayableAtTrickBeginning() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int pkTrick = PackedTrick.firstEmpty(nextColor(rng), nextPlayerId(rng));
            long pkHand = nextCardSet(rng, 9);
            assert PackedCardSet.size(pkHand) == 9;
            assertEquals(pkHand, PackedTrick.playableCards(pkTrick, pkHand));
        }
    }

    @Test
    void playableCardsIsNeverEmptyIfHandIsNotEmpty() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);

            CardSet remainingCards = CardSet.ALL_CARDS;
            for (int j = 0; j < 9; ++j) {
                int handSize = 9 - j;
                for (int k = 0; k < 4; ++k) {
                    CardSet hand = remainingCards;
                    assert hand.size() >= handSize;
                    while (hand.size() > handSize) {
                        int l = rng.nextInt(hand.size());
                        hand = hand.remove(hand.get(l));
                    }
                    assertTrue(! PackedCardSet.isEmpty(PackedTrick.playableCards(pkTrick, hand.packed())));

                    int cardI = rng.nextInt(remainingCards.size());
                    Card card = remainingCards.get(cardI);
                    remainingCards = remainingCards.remove(card);
                    pkTrick = PackedTrick.withAddedCard(pkTrick, card.packed());
                }
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
        }
    }

    @Test
    void playableCardsWorksInTrickyCase1() {
        // No obligation to follow when one only has the jack of trump
        long pkHand = cardSet(c(SPADE, SIX), c(SPADE, JACK), c(HEART, JACK));
        int pkTrick = trick(indexForHand(pkHand), HEART, PLAYER_1, c(HEART, SIX), c(SPADE, TEN), c(SPADE, NINE));
        assertEquals(pkHand, PackedTrick.playableCards(pkTrick, pkHand));
    }

    @Test
    void playableCardsWorksInTrickyCase2() {
        // Under-cutting is forbidden
        long pkHand = cardSet(c(SPADE, EIGHT), c(SPADE, JACK), c(SPADE, KING));
        int pkTrick = trick(indexForHand(pkHand), SPADE, PLAYER_1, c(HEART, SIX), c(SPADE, TEN), c(SPADE, QUEEN));

        long expPlayable = cardSet(c(SPADE, JACK), c(SPADE, KING));
        long actPlayable = PackedTrick.playableCards(pkTrick, pkHand);

        assertEquals(expPlayable, actPlayable);
    }

    @Test
    void playableCardsWorksInTrickyCase3() {
        // Under-cutting is allowed when there is no other choice
        long pkHand = cardSet(c(SPADE, SIX), c(SPADE, EIGHT), c(SPADE, TEN));
        int pkTrick = trick(indexForHand(pkHand), SPADE, PLAYER_1, c(HEART, SIX), c(SPADE, ACE));
        assertEquals(pkHand, PackedTrick.playableCards(pkTrick, pkHand));
    }

    @Test
    void playableCardsWorksInTrickyCase4() {
        // Cutting with the 6 of trump is allowed
        long pkHand = cardSet(c(SPADE, SIX), c(HEART, SIX));
        int pkTrick = trick(indexForHand(pkHand), SPADE, PLAYER_1, c(HEART, TEN));
        assertEquals(pkHand, PackedTrick.playableCards(pkTrick, pkHand));
    }

    @Test
    void playableCardsWorksInTrickyCase5() {
        // Any card can be played if one cannot follow, and nobody cut
        long pkHand = cardSet(c(SPADE, SIX), c(HEART, SIX),  c(SPADE, TEN),  c(CLUB, TEN));
        int pkTrick = trick(indexForHand(pkHand), SPADE, PLAYER_1, c(DIAMOND, TEN),  c(DIAMOND, SIX));
        assertEquals(pkHand, PackedTrick.playableCards(pkTrick, pkHand));
    }

    @Test
    void pointsSumTo157() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Color trump = nextColor(rng);
            PlayerId firstPlayer = nextPlayerId(rng);
            int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
            CardSet remainingCards = CardSet.ALL_CARDS;
            int totalPoints = 0;
            for (int j = 0; j < 9; ++j) {
                for (int k = 0; k < 4; ++k) {
                    int cardI = rng.nextInt(remainingCards.size());
                    Card card = remainingCards.get(cardI);
                    pkTrick = PackedTrick.withAddedCard(pkTrick, card.packed());
                    remainingCards = remainingCards.remove(card);
                }
                totalPoints += PackedTrick.points(pkTrick);
                pkTrick = PackedTrick.nextEmpty(pkTrick);
            }
            assertEquals(157, totalPoints);
        }
    }

    @Test
    void winningPlayerWorksInTrickyCase1() {
        // If the 3rd player under-cut, the 2nd one is the winner
        int pkTrick = trick(0, SPADE, PLAYER_1, c(HEART, TEN),  c(SPADE, NINE), c(SPADE, EIGHT));
        assertEquals(PlayerId.PLAYER_2, PackedTrick.winningPlayer(pkTrick));
    }

    @Test
    void winningPlayerWorksInTrickyCase2() {
        // If nobody could follow or cut, the 1st player is the winner
        int pkTrick = trick(0, SPADE, PLAYER_1, c(HEART, SIX),  c(DIAMOND, NINE), c(DIAMOND, EIGHT));
        assertEquals(PlayerId.PLAYER_1, PackedTrick.winningPlayer(pkTrick));
    }
}
