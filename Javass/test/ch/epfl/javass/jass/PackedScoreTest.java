package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public class PackedScoreTest {
    @Test
    void initialIsCorrect() {
        assertEquals(0, PackedScore.INITIAL);
    }

    @Test
    void isValidWorksForInitialScore() {
        assertTrue(PackedScore.isValid(PackedScore.INITIAL));
    }

    @Test
    void isValidWorksWhenTurnTricksTooBig() throws Exception {
        assertFalse(PackedScore.isValid(10L << 0));
        assertFalse(PackedScore.isValid(10L << (32 + 0)));
    }

    @Test
    void isValidWorksWhenTurnPointTooBig() throws Exception {
        assertFalse(PackedScore.isValid(258L << 4));
        assertFalse(PackedScore.isValid(258L << (32 + 4)));
    }

    @Test
    void isValidWorksWhenGamePointsTooBig() throws Exception {
        assertFalse(PackedScore.isValid(2001L << 13));
        assertFalse(PackedScore.isValid(2001L << (32 + 13)));
    }

    @Test
    void packedScoresCanBeUnpacked() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int t1 = rng.nextInt(10);
            int p1 = rng.nextInt(158);
            int g1 = rng.nextInt(2000);
            int t2 = rng.nextInt(10 - t1);
            int p2 = rng.nextInt(158 - p1);
            int g2 = rng.nextInt(2000 - g1);
            long pkScore = PackedScore.pack(t1, p1, g1, t2, p2, g2);
            assertEquals(t1, PackedScore.turnTricks(pkScore, TeamId.TEAM_1));
            assertEquals(p1, PackedScore.turnPoints(pkScore, TeamId.TEAM_1));
            assertEquals(g1, PackedScore.gamePoints(pkScore, TeamId.TEAM_1));
            assertEquals(t2, PackedScore.turnTricks(pkScore, TeamId.TEAM_2));
            assertEquals(p2, PackedScore.turnPoints(pkScore, TeamId.TEAM_2));
            assertEquals(g2, PackedScore.gamePoints(pkScore, TeamId.TEAM_2));
        }
    }

    @Test
    void totalPointsIsCorrect() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int t1 = rng.nextInt(10);
            int p1 = rng.nextInt(158);
            int g1 = rng.nextInt(2000);
            int t2 = rng.nextInt(10 - t1);
            int p2 = rng.nextInt(158 - p1);
            int g2 = rng.nextInt(2000 - g1);
            long pkScore = PackedScore.pack(t1, p1, g1, t2, p2, g2);
            assertEquals(p1 + g1, PackedScore.totalPoints(pkScore, TeamId.TEAM_1));
            assertEquals(p2 + g2, PackedScore.totalPoints(pkScore, TeamId.TEAM_2));
        }
    }

    @Test
    void withAdditionalTrickCorrectlyIncrementsTurnTricks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore = PackedScore.INITIAL;
            for (int trick = 0; trick < 5; ++trick) {
                TeamId winningTeam = TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
                int oldTurnTricks = PackedScore.turnTricks(pkScore, winningTeam);
                pkScore = PackedScore.withAdditionalTrick(pkScore, winningTeam, 0);
                int newTurnTricks = PackedScore.turnTricks(pkScore, winningTeam);
                assertEquals(oldTurnTricks + 1, newTurnTricks);
            }
        }
    }

    @Test
    void withAdditionalTrickCorrectlyCountsMatchPoints() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore = PackedScore.INITIAL;
            TeamId winningTeam = TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
            int remainingPoints = 157;
            for (int trick = 0; trick < 9; ++trick) {
                int trickPoints = trick == 8
                        ? remainingPoints
                                : rng.nextInt(Math.min(remainingPoints, 57));
                pkScore = PackedScore.withAdditionalTrick(pkScore, winningTeam, trickPoints);
                remainingPoints -= trickPoints;
            }
            assertEquals(257, PackedScore.turnPoints(pkScore, winningTeam));
            pkScore = PackedScore.nextTurn(pkScore);
        }
    }

    @Test
    void nextTurnCorrectlyResetsTurnTricksAndPoints() throws Exception {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            TeamId winningTeam = TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
            long pkScore = PackedScore.INITIAL;
            for (int j = 0; j < 4; ++j) {
                for (int trick = 0; trick < 9; ++trick) {
                    int trickPoints = trick == 0 ? 21 : 17;
                    pkScore = PackedScore.withAdditionalTrick(pkScore, winningTeam, trickPoints);
                }
                pkScore = PackedScore.nextTurn(pkScore);
                assertEquals(0, PackedScore.turnTricks(pkScore, TeamId.TEAM_1));
                assertEquals(0, PackedScore.turnPoints(pkScore, TeamId.TEAM_1));
                assertEquals(0, PackedScore.turnTricks(pkScore, TeamId.TEAM_2));
                assertEquals(0, PackedScore.turnPoints(pkScore, TeamId.TEAM_2));
            }
        }
    }

    @Test
    void scoreStaysValidDuringRandomGames() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore = PackedScore.INITIAL;
            while (PackedScore.gamePoints(pkScore, TeamId.TEAM_1) < 1000
                    && PackedScore.gamePoints(pkScore, TeamId.TEAM_2) < 1000) {
                int remainingPoints = 157;
                for (int trick = 0; trick < 9; ++trick) {
                    TeamId winningTeam = TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
                    int trickPoints = trick == 8
                            ? remainingPoints
                                    : rng.nextInt(Math.min(remainingPoints, 57));
                    pkScore = PackedScore.withAdditionalTrick(pkScore, winningTeam, trickPoints);
                    assertTrue(PackedScore.isValid(pkScore));
                    remainingPoints -= trickPoints;
                }
                int totalScore = PackedScore.turnPoints(pkScore, TeamId.TEAM_1)
                        + PackedScore.turnPoints(pkScore, TeamId.TEAM_2);
                assertTrue(totalScore == 157 || totalScore == 257);
                pkScore = PackedScore.nextTurn(pkScore);
                assertTrue(PackedScore.isValid(pkScore));
            }
        }
    }
}