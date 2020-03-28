package ch.epfl.javass.jass;

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

public class TurnStateTest {
    private static Score nextScore(SplittableRandom rng) {
        Score s = Score.INITIAL;
        for (int i = 0; i < rng.nextInt(5); ++i) {
            TeamId t = nextTeamId(rng);
            s = s.withAdditionalTrick(t, rng.nextInt(20));
        }
        return s;
    }

    private static TeamId nextTeamId(SplittableRandom rng) {
        return TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
    }

    private static PlayerId nextPlayerId(SplittableRandom rng) {
        return PlayerId.ALL.get(rng.nextInt(PlayerId.COUNT));
    }

    private static CardSet nextCardSet(SplittableRandom rng, int size) {
        CardSet s = CardSet.ALL_CARDS;
        while (s.size() > size) {
            int toRemoveIndex = rng.nextInt(s.size());
            s = s.remove(s.get(toRemoveIndex));
        }
        assert s.size() == size;
        return s;
    }

    private static Color nextColor(SplittableRandom rng) {
        return Color.ALL.get(rng.nextInt(Color.COUNT));
    }

    private static Card nextCard(SplittableRandom rng) {
        return CardSet.ALL_CARDS.get(rng.nextInt(CardSet.ALL_CARDS.size()));
    }

    private static Trick nextTrick(SplittableRandom rng) {
        Color trump = nextColor(rng);
        PlayerId firstPlayer = nextPlayerId(rng);
        Trick trick = Trick.firstEmpty(trump, firstPlayer);
        for (int i = 0; i < rng.nextInt(4); ++i)
            trick = trick.withAddedCard(nextCard(rng));
        return trick;
    }

    @Test
    void initialWorks() {
        SplittableRandom rng = newRandom();
        for (Color trump: Color.ALL) {
            for (PlayerId firstPlayer: PlayerId.ALL) {
                Score score = nextScore(rng);
                TurnState s = TurnState.initial(trump, score, firstPlayer);
                assertEquals(trump, s.trick().trump());
                assertEquals(score, s.score());
                assertEquals(firstPlayer, s.trick().player(0));
            }
        }
    }

    @Test
    void ofPackedComponentsAndAccessorsWork() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Score score = nextScore(rng);
            CardSet unplayedCards = nextCardSet(rng, 32);
            Trick trick = nextTrick(rng);
            TurnState s = TurnState.ofPackedComponents(score.packed(), unplayedCards.packed(), trick.packed());
            assertEquals(score.packed(), s.packedScore());
            assertEquals(score, s.score());
            assertEquals(unplayedCards.packed(), s.packedUnplayedCards());
            assertEquals(unplayedCards, s.unplayedCards());
            assertEquals(trick.packed(), s.packedTrick());
            assertEquals(trick, s.trick());
        }
    }

    @Test
    void ofPackedComponentsFailsWithInvalidComponents() {
        assertThrows(IllegalArgumentException.class, () -> {
            TurnState.ofPackedComponents(~0, PackedCardSet.EMPTY, PackedTrick.firstEmpty(Color.CLUB, PlayerId.PLAYER_1));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            TurnState.ofPackedComponents(PackedScore.INITIAL, ~0, PackedTrick.firstEmpty(Color.CLUB, PlayerId.PLAYER_1));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            TurnState.ofPackedComponents(PackedScore.INITIAL, PackedCardSet.EMPTY, PackedTrick.INVALID);
        });
    }

    @Test
    void isTerminalWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            CardSet remainingCards = CardSet.ALL_CARDS;
            TurnState s = TurnState.initial(nextColor(rng), Score.INITIAL, nextPlayerId(rng));
            for (int t = 0; t < 9; ++t) {
                for (int p = 0; p < 4; ++p) {
                    assertFalse(s.isTerminal());
                    Card cardToPlay = remainingCards.get(rng.nextInt(remainingCards.size()));
                    remainingCards = remainingCards.remove(cardToPlay);
                    s = s.withNewCardPlayed(cardToPlay);
                }
                s = s.withTrickCollected();
            }
            assertTrue(s.isTerminal());
        }
    }

    @Test
    void nextPlayerWorks() {
        SplittableRandom rng = newRandom();
        for (PlayerId firstPlayer: PlayerId.ALL) {
            Color trump = nextColor(rng);
            Score score = nextScore(rng);
            CardSet cardsToPlay = nextCardSet(rng, 4);
            TurnState s = TurnState.initial(trump, score, firstPlayer);
            for (int i = 0; i < 4; ++i) {
                PlayerId expNextPlayer = PlayerId.ALL.get((firstPlayer.ordinal() + i) % PlayerId.COUNT);
                assertEquals(expNextPlayer, s.nextPlayer());
                s = s.withNewCardPlayed(cardsToPlay.get(i));
            }
        }
    }

    @Test
    void nextPlayerFailsWhenTrickIsFull() {
        TurnState s = TurnState.initial(Color.DIAMOND, Score.INITIAL, PlayerId.PLAYER_4)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.NINE))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.ACE));
        assertThrows(IllegalStateException.class, () -> {
            s.nextPlayer();
        });
    }

    @Test
    void withNewCardPlayedUpdatesUnplayedCards() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            CardSet remainingCards = CardSet.ALL_CARDS;
            TurnState s = TurnState.initial(nextColor(rng), Score.INITIAL, nextPlayerId(rng));
            for (int t = 0; t < 9; ++t) {
                for (int p = 0; p < 4; ++p) {
                    assertEquals(remainingCards, s.unplayedCards());
                    Card cardToPlay = remainingCards.get(rng.nextInt(remainingCards.size()));
                    remainingCards = remainingCards.remove(cardToPlay);
                    s = s.withNewCardPlayed(cardToPlay);
                }
                s = s.withTrickCollected();
            }
            assertEquals(CardSet.EMPTY, s.unplayedCards());
        }
    }

    @Test
    void withNewCardPlayedFailsWhenTrickIsFull() {
        TurnState s = TurnState.initial(Color.HEART, Score.INITIAL, PlayerId.PLAYER_2)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.NINE))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.ACE));
        assertThrows(IllegalStateException.class, () -> {
            s.withNewCardPlayed(Card.of(Color.HEART, Rank.ACE));
        });
    }

    @Test
    void withTrickCollectedFailsWhenTrickIsNotFull() {
        SplittableRandom rng = newRandom();
        CardSet deck = CardSet.ALL_CARDS;
        for (int i = 0; i < 3; ++i) {
            TurnState s = TurnState.initial(nextColor(rng), Score.INITIAL, nextPlayerId(rng));
            for (int j = 0; j <= i; ++j)
                s = s.withNewCardPlayed(deck.get(j));
            TurnState s1 = s;
            assertThrows(IllegalStateException.class, () -> {
                s1.withTrickCollected();
            });
        }
    }

    @Test
    void withNewCardPlayedAndTrickCollectedUpdatesUnplayedCards() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            CardSet remainingCards = CardSet.ALL_CARDS;
            TurnState s = TurnState.initial(nextColor(rng), Score.INITIAL, nextPlayerId(rng));
            for (int t = 0; t < 9; ++t) {
                for (int p = 0; p < 4; ++p) {
                    assertEquals(remainingCards, s.unplayedCards());
                    Card cardToPlay = remainingCards.get(rng.nextInt(remainingCards.size()));
                    remainingCards = remainingCards.remove(cardToPlay);
                    s = s.withNewCardPlayedAndTrickCollected(cardToPlay);
                }
            }
            assertEquals(CardSet.EMPTY, s.unplayedCards());
        }
    }

    @Test
    void withNewCardPlayedAndTrickCollectedFailsWhenTrickIsFull() {
        TurnState s = TurnState.initial(Color.HEART, Score.INITIAL, PlayerId.PLAYER_2)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.NINE))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.ACE));
        assertThrows(IllegalStateException.class, () -> {
            s.withNewCardPlayedAndTrickCollected(Card.of(Color.HEART, Rank.ACE));
        });
    }
}
