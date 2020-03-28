package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum representing the two teams of a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public enum TeamId {
	TEAM_1(), TEAM_2();
	
	/**
	 * List containing both teams
	 */
	public static final List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));

	/**
	 * Number of teams
	 */
	public static final int COUNT = ALL.size();

	/**
	 * Method returning the other TeamId
	 * 
	 * @return (TeamId) : the other TeamId
	 */
	public TeamId other() {
		return this == TEAM_1 ? TEAM_2 : TEAM_1;
	}
}