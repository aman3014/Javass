package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class JassGameTest {
    private static Map<PlayerId, TestPlayer> testPlayers(List<PlayerId> playingOrderLog) {
        Map<PlayerId, TestPlayer> ps = new EnumMap<>(PlayerId.class);
        for (PlayerId pId: PlayerId.ALL)
            ps.put(pId, new TestPlayer(pId, playingOrderLog));
        return ps;
    }

    private static Map<PlayerId, TestPlayer> testPlayers() {
        return testPlayers(null);
    }

    private static Map<PlayerId, Player> toNormalPlayers(Map<PlayerId, TestPlayer> ps) {
        Map<PlayerId, Player> ps2 = new EnumMap<>(PlayerId.class);
        ps2.putAll(ps);
        return ps2;
    }

    private static Map<PlayerId, String> testPlayerNames() {
        Map<PlayerId, String> ps = new EnumMap<>(PlayerId.class);
        for (PlayerId pId: PlayerId.ALL)
            ps.put(pId, pId.toString());
        return ps;
    }

    @Test
    void deckIsCorrectlyDistributed() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);
                g.advanceToEndOfNextTrick();

                CardSet cards = CardSet.ALL_CARDS;
                for (TestPlayer p: ps.values()) {
                    assertEquals(9, cards.intersection(p.updateHandInitialHand).size());
                    cards = cards.difference(p.updateHandInitialHand);
                }
                assertTrue(cards.isEmpty());
            }
        });
    }

    @Test
    void playersAreCommunicatedOnFirstTrick() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Map<PlayerId, TestPlayer> ps = testPlayers();
            Map<PlayerId, String> ns = testPlayerNames();
            JassGame g = new JassGame(0, toNormalPlayers(ps), ns);
            g.advanceToEndOfNextTrick();
            for (TestPlayer p: ps.values()) {
                assertEquals(1, p.setPlayersCallCount);
                assertEquals(p.ownId, p.setPlayersOwnId);
                assertEquals(ns, p.setPlayersPlayerNames);
            }
        });
    }

    @Test
    void firstPlayerHasSevenOfDiamond() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                List<PlayerId> playingOrderLog = new ArrayList<>();
                Map<PlayerId, TestPlayer> ps = testPlayers(playingOrderLog);
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);
                g.advanceToEndOfNextTrick();
                assertEquals(PlayerId.COUNT, playingOrderLog.size());
                TestPlayer firstPlayer = ps.get(playingOrderLog.get(0));
                assertTrue(firstPlayer.updateHandInitialHand.contains(Card.of(Color.DIAMOND, Rank.SEVEN)));
            }
        });
    }

    @Test
    void trickContentsMatchesPlayedCards() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);

                for (int j = 0; j < 3; ++j) {
                    for (int t = 0; t < 9; ++t) {
                        g.advanceToEndOfNextTrick();
                        Trick fullTrick = ps.get(PlayerId.PLAYER_1).updateTrickNewTrick;
                        assertTrue(fullTrick.isFull());
                        for (int p = 0; p < 4; ++p) {
                            PlayerId pId = fullTrick.player(p);
                            TestPlayer tp = ps.get(pId);
                            assertEquals(tp.cardToPlayReturnedCard, fullTrick.card(p));
                        }
                    }
                }
            }
        });
    }

    @Test
    void handIsUpdatedAfterEachCardIsPlayed() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);

                for (int j = 0; j < 3; ++j) {
                    for (int t = 0; t < 9; ++t) {
                        g.advanceToEndOfNextTrick();
                        int expectedHandSize = 8 - t;
                        for (TestPlayer p: ps.values())
                            assertEquals(expectedHandSize, p.updateHandNewHand.size());
                    }
                }
            }
        });
    }

    @Test
    void trumpIsCommunicatedAtTheBeginningOfEachTurn() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);

                int setTrumpCallCount = 0;
                for (int j = 0; j < 3; ++j) {
                    for (int t = 0; t < 9; ++t) {
                        g.advanceToEndOfNextTrick();
                    }
                    int newSetTrumpCallCount = 0;
                    Color player1Trump = ps.get(PlayerId.PLAYER_1).setTrumpTrump;
                    for (TestPlayer p: ps.values()) {
                        assertTrue(p.setTrumpCallCount > setTrumpCallCount);
                        newSetTrumpCallCount = Math.max(newSetTrumpCallCount, p.setTrumpCallCount);
                        assertEquals(player1Trump, p.setTrumpTrump);
                    }
                }
            }
        });
    }

    @Test
    void updateTrickIsCalledFiveTimesPerTrick() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);

                for (int t = 0; t < 9; ++t) {
                    int expectedCallsToUpdateTrick = 5 * t;
                    Trick player1Trick = ps.get(PlayerId.PLAYER_1).updateTrickNewTrick;
                    for (TestPlayer p: ps.values()) {
                        assertTrue(p.updateTrickCallCount >= expectedCallsToUpdateTrick);
                        assertEquals(player1Trick, p.updateTrickNewTrick);
                    }
                    g.advanceToEndOfNextTrick();
                }
            }
        });
    }

    @Test
    void updateScoreIsCalledOncePerTrick() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);

                for (int t = 0; t < 9; ++t) {
                    int expectedCallsToUpdateScore = 1 * t;
                    Score player1Score = ps.get(PlayerId.PLAYER_1).updateScoreScore;
                    for (TestPlayer p: ps.values()) {
                        assertTrue(p.updateScoreCallCount >= expectedCallsToUpdateScore);
                        assertEquals(player1Score, p.updateScoreScore);
                    }
                    g.advanceToEndOfNextTrick();
                }
            }
        });
    }

    @Test
    void setWinningTeamIsCalledOncePerGame() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            int maxTurnsPerGame = (int) Math.ceil(2d * Jass.WINNING_POINTS / 157);
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);
                for (int j = 0; j <= Jass.TRICKS_PER_TURN * maxTurnsPerGame; ++j) {
                    g.advanceToEndOfNextTrick();
                }

                assertTrue(g.isGameOver());
                TeamId player1WinningTeam = ps.get(PlayerId.PLAYER_1).setWinningTeamWinningTeam;
                for (TestPlayer p: ps.values()) {
                    assertTrue(p.setWinningTeamCallCount >= 1);
                    assertEquals(player1WinningTeam, p.setWinningTeamWinningTeam);
                }
            }
        });
    }

    @Test
    void onlyWinningTeamIsOver1000PointsWhenGameIsOver() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            SplittableRandom rng = newRandom();
            for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
                Map<PlayerId, TestPlayer> ps = testPlayers();
                Map<PlayerId, String> ns = testPlayerNames();
                JassGame g = new JassGame(rng.nextLong(), toNormalPlayers(ps), ns);
                while (! g.isGameOver()) {
                    g.advanceToEndOfNextTrick();
                }

                TeamId winningTeam = ps.get(PlayerId.PLAYER_1).setWinningTeamWinningTeam;
                Score lastScore = ps.get(PlayerId.PLAYER_1).updateScoreScore;

                int winningP = lastScore.totalPoints(winningTeam);
                int loosingP = lastScore.totalPoints(winningTeam.other());
                assertTrue(loosingP < 1000 && 1000 <= winningP);
            }
        });
    }

    @SuppressWarnings("unused")
    private static class TestPlayer implements Player {
        final PlayerId ownId;
        final List<PlayerId> playingOrderLog;

        int cardToPlayCallCount = 0;
        TurnState cardToPlayState = null;
        CardSet cardToPlayHand = null;
        Card cardToPlayReturnedCard = null;

        int setPlayersCallCount = 0;
        PlayerId setPlayersOwnId = null;
        Map<PlayerId, String> setPlayersPlayerNames = null;

        int updateHandCallCount = 0;
        CardSet updateHandNewHand = null;
        CardSet updateHandInitialHand = null;

        int setTrumpCallCount = 0;
        Color setTrumpTrump = null;

        int updateTrickCallCount = 0;
        Trick updateTrickNewTrick = null;

        int updateScoreCallCount = 0;
        Score updateScoreScore = null;

        int setWinningTeamCallCount = 0;
        TeamId setWinningTeamWinningTeam = null;

        TestPlayer(PlayerId ownId, List<PlayerId> playingOrderLog) {
            this.ownId = ownId;
            this.playingOrderLog = playingOrderLog;
        }

        @Override
        public Card cardToPlay(TurnState state, CardSet hand) {
            if (playingOrderLog != null)
                playingOrderLog.add(ownId);

            cardToPlayCallCount += 1;
            cardToPlayState = state;
            cardToPlayHand = hand;
            cardToPlayReturnedCard = state.trick().playableCards(hand).get(0);
            return cardToPlayReturnedCard;
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
            if (updateHandInitialHand == null)
                updateHandInitialHand = newHand;
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
