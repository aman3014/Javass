package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.jass.TeamId;

/**
 * Enum representing the four players of a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public enum PlayerId {
	PLAYER_1(), PLAYER_2(), PLAYER_3(), PLAYER_4();

	/**
	 * List containing all the players
	 */
	public static final List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));

	/**
	 * Number of players
	 */
	public static final int COUNT = ALL.size();

	/**
	 * Method returning the player's TeamId
	 * 
	 * @return (TeamId) : the player's TeamId
	 */
	public TeamId team() {
		return (ordinal() % 2) == 0 ? TeamId.TEAM_1 : TeamId.TEAM_2;
	}
}