package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class MctsPlayerTest {
    private static final long SEED = 0;
    private static final int ITERATIONS = 10_000;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @Test
    void constructorFailsWithTooFewIterations() {
        for (int i = -10; i < 9; ++i) {
            int iterations = i;
            assertThrows(IllegalArgumentException.class, () -> {
                new MctsPlayer(PlayerId.PLAYER_1, 0, iterations);
            });
        }
    }

    @Test
    void MctsPlayerPlaysCorrectly1() {
        // Opponent team will win this trick, we have to minimize loss
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_2, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.SPADE, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.SEVEN))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.JACK));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.SPADE, Rank.EIGHT), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly2() {
        // Our team will win this trick, play the 10 to maximize points
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_4, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.QUEEN))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.SIX));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.CLUB, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.SEVEN))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.JACK));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.SPADE, Rank.TEN), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly3() {
        // Lots of points in this trick, over-cut to get them
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_4, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.HEART, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.NINE));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.JACK))
                .add(Card.of(Color.CLUB, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.SEVEN))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.QUEEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.CLUB, Rank.JACK), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly4() {
        // Lots of points in this trick, cut to get them, but don't waste the Jack
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_4, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.HEART, Rank.TEN))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.ACE));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.JACK))
                .add(Card.of(Color.CLUB, Rank.SEVEN))
                .add(Card.of(Color.CLUB, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.QUEEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.CLUB, Rank.SEVEN), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly5() {
        // Trick winner unclear, follow but don't risk the 10
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_3, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.DIAMOND, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.EIGHT))
                .withNewCardPlayed(Card.of(Color.SPADE, Rank.SEVEN));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.CLUB, Rank.SEVEN))
                .add(Card.of(Color.CLUB, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.QUEEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.SPADE, Rank.NINE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly6() {
        // Very strong hand in trump, enter with the Jack
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_1, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.SPADE, Score.INITIAL, PlayerId.PLAYER_1);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.SEVEN))
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.SPADE, Rank.JACK))
                .add(Card.of(Color.SPADE, Rank.KING))
                .add(Card.of(Color.SPADE, Rank.ACE))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.QUEEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.SPADE, Rank.JACK), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly7() {
        // We can only play one card, play it
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_2, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.JACK));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.SPADE, Rank.JACK))
                .add(Card.of(Color.SPADE, Rank.QUEEN))
                .add(Card.of(Color.SPADE, Rank.KING))
                .add(Card.of(Color.SPADE, Rank.ACE))
                .add(Card.of(Color.CLUB, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.JACK))
                .add(Card.of(Color.HEART, Rank.QUEEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.CLUB, Rank.NINE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly8() {
        // We don't have to follow, save the Jack of trump for later (0 points in trick)
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_4, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SIX))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SEVEN))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.EIGHT));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.CLUB, Rank.JACK))
                .add(Card.of(Color.DIAMOND, Rank.TEN))
                .add(Card.of(Color.DIAMOND, Rank.ACE))
                .add(Card.of(Color.SPADE, Rank.SIX))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.SPADE, Rank.ACE))
                .add(Card.of(Color.HEART, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.ACE))
                .add(Card.of(Color.HEART, Rank.KING));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.SPADE, Rank.SIX), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly9() {
        // Two tricks left, no trump left, we have an ace, we must enter with it
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.ACE))
                .remove(Card.of(Color.CLUB, Rank.SIX))
                .remove(Card.of(Color.CLUB, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.ACE))
                .remove(Card.of(Color.DIAMOND, Rank.SIX))
                .remove(Card.of(Color.DIAMOND, Rank.ACE));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.SPADE, PlayerId.PLAYER_1);
        assert state.trick().isEmpty();
        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.CLUB, Rank.SIX))
                .add(Card.of(Color.CLUB, Rank.ACE));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.CLUB, Rank.ACE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly10() {
        // Two tricks left, two trumps left, we have the higher one, we must enter with it
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.ACE))
                .remove(Card.of(Color.CLUB, Rank.SIX))
                .remove(Card.of(Color.CLUB, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.ACE))
                .remove(Card.of(Color.DIAMOND, Rank.SIX))
                .remove(Card.of(Color.DIAMOND, Rank.ACE));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.DIAMOND, PlayerId.PLAYER_1);
        assert state.trick().isEmpty();
        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.DIAMOND, Rank.ACE))
                .add(Card.of(Color.CLUB, Rank.ACE));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.DIAMOND, Rank.ACE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly11() {
        // Two tricks left, we are loosing the trick, we must cut to win the last tricks
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.SIX))
                .remove(Card.of(Color.CLUB, Rank.SEVEN))
                .remove(Card.of(Color.CLUB, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.ACE))
                .remove(Card.of(Color.DIAMOND, Rank.ACE));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.DIAMOND, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SEVEN))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SIX))
                .withNewCardPlayed(Card.of(Color.HEART, Rank.SEVEN));
        assert state.trick().size() == 3;

        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.DIAMOND, Rank.ACE))
                .add(Card.of(Color.HEART, Rank.KING));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.DIAMOND, Rank.ACE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly12() {
        // Same as above, but we're second to play
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.SIX))
                .remove(Card.of(Color.CLUB, Rank.SEVEN))
                .remove(Card.of(Color.CLUB, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.ACE))
                .remove(Card.of(Color.DIAMOND, Rank.ACE));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.DIAMOND, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SEVEN));
        assert state.trick().size() == 1;

        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.DIAMOND, Rank.ACE))
                .add(Card.of(Color.HEART, Rank.KING));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.DIAMOND, Rank.ACE), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly13() {
        // We have the last trump and a 10, our partner has the lead, we must play the 10
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.TEN))
                .remove(Card.of(Color.CLUB, Rank.JACK))
                .remove(Card.of(Color.CLUB, Rank.KING))
                .remove(Card.of(Color.CLUB, Rank.ACE))
                .remove(Card.of(Color.DIAMOND, Rank.SIX));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.DIAMOND, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.KING))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.ACE))
                .withNewCardPlayed(Card.of(Color.HEART, Rank.SIX));
        assert state.trick().size() == 3;

        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.CLUB, Rank.TEN))
                .add(Card.of(Color.DIAMOND, Rank.SIX));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.CLUB, Rank.TEN), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly14() {
        // Two zero-points tricks remain, we must accept loosing the first to get the 5 final points
        CardSet toPlay = CardSet.ALL_CARDS
                .remove(Card.of(Color.SPADE, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SIX))
                .remove(Card.of(Color.HEART, Rank.SEVEN))
                .remove(Card.of(Color.HEART, Rank.EIGHT))
                .remove(Card.of(Color.HEART, Rank.NINE))
                .remove(Card.of(Color.CLUB, Rank.SIX))
                .remove(Card.of(Color.CLUB, Rank.SEVEN))
                .remove(Card.of(Color.CLUB, Rank.EIGHT));
        TurnState state = stateAfterPlayingAllCardsIn(toPlay, Color.SPADE, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SIX))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.SEVEN))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.EIGHT));
        assert state.trick().size() == 3;

        MctsPlayer p = new MctsPlayer(state.nextPlayer(), SEED, ITERATIONS);
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.SIX));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.HEART, Rank.SIX), c);
        });
    }

    @Test
    void MctsPlayerPlaysCorrectly15() {
        // Cannot undercut, must accept loss (only one playable card)
        MctsPlayer p = new MctsPlayer(PlayerId.PLAYER_4, SEED, ITERATIONS);
        TurnState state = TurnState.initial(Color.CLUB, Score.INITIAL, PlayerId.PLAYER_1)
                .withNewCardPlayed(Card.of(Color.HEART, Rank.KING))
                .withNewCardPlayed(Card.of(Color.HEART, Rank.ACE))
                .withNewCardPlayed(Card.of(Color.CLUB, Rank.NINE));
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.CLUB, Rank.SIX))
                .add(Card.of(Color.CLUB, Rank.SEVEN))
                .add(Card.of(Color.CLUB, Rank.EIGHT))
                .add(Card.of(Color.CLUB, Rank.TEN))
                .add(Card.of(Color.CLUB, Rank.QUEEN))
                .add(Card.of(Color.CLUB, Rank.KING))
                .add(Card.of(Color.CLUB, Rank.ACE))
                .add(Card.of(Color.SPADE, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.TEN));
        assertTimeoutPreemptively(TIMEOUT, () -> {
            Card c = p.cardToPlay(state, hand);
            assertEquals(Card.of(Color.HEART, Rank.TEN), c);
        });
    }

    private static TurnState stateAfterPlayingAllCardsIn(CardSet cards, Color trump, PlayerId firstPlayer) {
        TurnState s = TurnState.initial(trump, Score.INITIAL, firstPlayer);
        for (int i = 0; i < cards.size(); ++i)
            s = s.withNewCardPlayedAndTrickCollected(cards.get(i));
        return s;
    }
}
