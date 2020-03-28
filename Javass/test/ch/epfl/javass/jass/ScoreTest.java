package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public class ScoreTest {
    private static long randomPkScore(SplittableRandom rng) {
        int t1 = rng.nextInt(10);
        int p1 = rng.nextInt(158);
        int g1 = rng.nextInt(2000);
        int t2 = rng.nextInt(10 - t1);
        int p2 = rng.nextInt(158 - p1);
        int g2 = rng.nextInt(2000 - g1);
        return PackedScore.pack(t1, p1, g1, t2, p2, g2);
    }

    @Test
    void initialIsCorrect() {
        assertEquals(PackedScore.INITIAL, Score.INITIAL.packed());
    }

    @Test
    void ofPackedAndPackedAreInverses() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore = randomPkScore(rng);
            assertEquals(pkScore, Score.ofPacked(pkScore).packed());
        }
    }

    @Test
    void withAdditionalTrickDoesNotChangeReceiver() {
        Score s0 = Score.INITIAL;
        Score s1 = s0.withAdditionalTrick(TeamId.TEAM_1, 5);
        assertEquals(PackedScore.INITIAL, s0.packed());

        long pkS1 = s1.packed();
        assertNotEquals(PackedScore.INITIAL, pkS1);

        Score s2 = s1.withAdditionalTrick(TeamId.TEAM_2, 5);
        assertEquals(pkS1, s1.packed());

        long pkS2 = s2.packed();
        assertNotEquals(pkS1, pkS2);
    }

    @Test
    void nextTurnDoesNotChangeReceiver() {
        Score s0 = Score.INITIAL;
        for (int i = 0; i < 9; ++i)
            s0 = s0.withAdditionalTrick(TeamId.TEAM_1, i == 0 ? 21 : 17);

        Score s1 = s0.nextTurn();

        assertNotEquals(s0.packed(), s1.packed());
    }

    @Test
    void equalsIsFalseForUnequalScores() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore1 = randomPkScore(rng);
            long pkScore2 = randomPkScore(rng);
            if (pkScore1 == pkScore2)
                continue; // very unlikely, but...
            Score s1 = Score.ofPacked(pkScore1);
            Score s2 = Score.ofPacked(pkScore2);
            assertFalse(s1.equals(s2));
        }
    }

    @Test
    void equalsIsTrueOnEqualButDifferentInstances() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            long pkScore = randomPkScore(rng);
            Score s1 = Score.ofPacked(pkScore);
            Score s2 = Score.ofPacked(pkScore);
            assertTrue(s1.equals(s2));
        }
    }
}
