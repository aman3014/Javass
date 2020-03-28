package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class PlayerIdTest {
    private static PlayerId[] getAllPlayerIds() {
        return new PlayerId[] {
                PlayerId.PLAYER_1,
                PlayerId.PLAYER_2,
                PlayerId.PLAYER_3,
                PlayerId.PLAYER_4,
        };
    }

    @Test
    void playerIdsAreInRightOrder() {
        assertArrayEquals(getAllPlayerIds(), PlayerId.values());
    }

    @Test
    void playerIdAllIsCorrect() {
        assertEquals(Arrays.asList(PlayerId.values()), PlayerId.ALL);
    }

    @Test
    void playerIdAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            PlayerId.ALL.set(0, null);
        });
    }

    @Test
    void playerIdCountIsCorrect() {
        assertEquals(getAllPlayerIds().length, PlayerId.COUNT);
    }

    @Test
    void playerIdTeamIsCorrect() {
        TeamId[] expectedTeams = new TeamId[] {
                TeamId.TEAM_1, TeamId.TEAM_2, TeamId.TEAM_1, TeamId.TEAM_2
        };
        PlayerId[] playerIds = getAllPlayerIds();
        for (int i = 0; i < expectedTeams.length; ++i)
            assertEquals(expectedTeams[i], playerIds[i].team());
    }
}
