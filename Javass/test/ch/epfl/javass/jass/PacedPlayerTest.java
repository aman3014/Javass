package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;

public class PacedPlayerTest {
    @Test
    void pacedPlayerCorrectlyReturnsCardPlayed() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (Color color: Color.ALL) {
                TurnState state = TurnState.initial(color, Score.INITIAL, PlayerId.PLAYER_1);
                CardSet hand = CardSet.ALL_CARDS.subsetOfColor(color);
                for (int i = 0; i < hand.size(); ++i) {
                    Player p = new PacedPlayer(new TestPlayer(i), 1e-3);
                    assertEquals(hand.get(i), p.cardToPlay(state, hand));
                }
            }
        });
    }

    @Test
    void pacedPlayedDoesPacing() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Player p = new PacedPlayer(new TestPlayer(), 1);
            long t0 = System.currentTimeMillis();
            p.cardToPlay(TurnState.initial(Color.SPADE, Score.INITIAL, PlayerId.PLAYER_1), CardSet.ALL_CARDS.subsetOfColor(Color.SPADE));
            long elapsedTime = System.currentTimeMillis() - t0;
            assertTrue(elapsedTime >= 995);
        });
    }

    @Test
    void setPlayersIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);
            for (PlayerId pId: PlayerId.ALL)
                playerNames.put(pId, "Le joueur : " + pId.name());
            pp.setPlayers(PlayerId.PLAYER_3, playerNames);
            assertEquals(1, tp.setPlayersCallCount);
            assertEquals(PlayerId.PLAYER_3, tp.setPlayersOwnId);
            assertEquals(playerNames, tp.setPlayersPlayerNames);
        });
    }

    @Test
    void updateHandIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            CardSet newHand = CardSet.ALL_CARDS.subsetOfColor(Color.DIAMOND);
            pp.updateHand(newHand);
            assertEquals(1, tp.updateHandCallCount);
            assertEquals(newHand, tp.updateHandNewHand);
        });
    }

    @Test
    void setTrumpIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            Color trump = Color.CLUB;
            pp.setTrump(trump);
            assertEquals(1, tp.setTrumpCallCount);
            assertEquals(trump, tp.setTrumpTrump);
        });
    }

    @Test
    void updateTrickIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            Trick trick = Trick.firstEmpty(Color.HEART, PlayerId.PLAYER_2);
            pp.updateTrick(trick);
            assertEquals(1, tp.updateTrickCallCount);
            assertEquals(trick, tp.updateTrickNewTrick);
        });
    }

    @Test
    void updateScoreIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            Score score = Score.INITIAL;
            pp.updateScore(score);
            assertEquals(1, tp.updateScoreCallCount);
            assertEquals(score, tp.updateScoreScore);
        });
    }

    @Test
    void setWinningTeamIsCorrectlyForwarded() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TestPlayer tp = new TestPlayer();
            Player pp = new PacedPlayer(tp, 1e-3);
            TeamId winningTeam = TeamId.TEAM_2;
            pp.setWinningTeam(winningTeam);
            assertEquals(1, tp.setWinningTeamCallCount);
            assertEquals(winningTeam, tp.setWinningTeamWinningTeam);
        });
    }

    private static class TestPlayer implements Player {
        private final int indexOfCardToPlay;

        int setPlayersCallCount = 0;
        PlayerId setPlayersOwnId = null;
        Map<PlayerId, String> setPlayersPlayerNames = null;

        int updateHandCallCount = 0;
        CardSet updateHandNewHand = null;

        int setTrumpCallCount = 0;
        Color setTrumpTrump = null;

        int updateTrickCallCount = 0;
        Trick updateTrickNewTrick = null;

        int updateScoreCallCount = 0;
        Score updateScoreScore = null;

        int setWinningTeamCallCount = 0;
        TeamId setWinningTeamWinningTeam = null;

        public TestPlayer(int indexOfCardToPlay) {
            this.indexOfCardToPlay = indexOfCardToPlay;
        }

        public TestPlayer() {
            this(0);
        }

        @Override
        public Card cardToPlay(TurnState state, CardSet hand) {
            return hand.get(indexOfCardToPlay);
        }

        @Override
        public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
            this.setPlayersCallCount += 1;
            this.setPlayersOwnId = ownId;
            this.setPlayersPlayerNames = playerNames;
        }

        @Override
        public void updateHand(CardSet newHand) {
            updateHandCallCount += 1;
            updateHandNewHand = newHand;
        }

        @Override
        public void setTrump(Color trump) {
            setTrumpCallCount += 1;
            setTrumpTrump = trump;
        }

        @Override
        public void updateTrick(Trick newTrick) {
            updateTrickCallCount += 1;
            updateTrickNewTrick = newTrick;
        }

        @Override
        public void updateScore(Score score) {
            updateScoreCallCount += 1;
            updateScoreScore = score;
        }

        @Override
        public void setWinningTeam(TeamId winningTeam) {
            setWinningTeamCallCount += 1;
            setWinningTeamWinningTeam = winningTeam;
        }

		@Override
		public Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}

