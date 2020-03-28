package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A JavaFX bean containing a score of Jass
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class ScoreBean {
	private final IntegerProperty[] turnPoints;
	private final IntegerProperty[] gamePoints;
	private final IntegerProperty[] totalPoints;
	private final IntegerProperty[] turnTricks;
	private final ObjectProperty<TeamId> winningTeamProperty;

	/**
	 * Constructs a new score bean
	 */
	public ScoreBean() {
		turnPoints = new SimpleIntegerProperty[TeamId.COUNT];
		gamePoints = new SimpleIntegerProperty[TeamId.COUNT];
		totalPoints = new SimpleIntegerProperty[TeamId.COUNT];
		turnTricks = new SimpleIntegerProperty[TeamId.COUNT];

		for (int i = 0; i < TeamId.COUNT; ++i) {
			turnPoints[i] = new SimpleIntegerProperty();
			gamePoints[i] = new SimpleIntegerProperty();
			totalPoints[i] = new SimpleIntegerProperty();
			turnTricks[i] = new SimpleIntegerProperty();
		}

		winningTeamProperty = new SimpleObjectProperty<>();
	}

	/**
	 * Gives the turn tricks property (as read only) of a given team of the score
	 * bean
	 * 
	 * @param team : the team
	 * @return the turn tricks property
	 */
	public ReadOnlyIntegerProperty turnTricksProperty(TeamId team) {
		return turnTricks[team.ordinal()];
	}

	/**
	 * Updates the turn tricks property of the score bean for a given team
	 * 
	 * @param team   : the team
	 * @param tricks : the new turn tricks
	 */
	public void setTurnTricks(TeamId team, int tricks) {
		turnTricks[team.ordinal()].set(tricks);
	}

	/**
	 * Gives the turn points property (as read only) of a given team of the score
	 * bean
	 * 
	 * @param team : the team
	 * @return the turn points property
	 */
	public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
		return turnPoints[team.ordinal()];
	}

	/**
	 * Updates the turn points property of the score bean for a given team
	 * 
	 * @param team          : the team
	 * @param newTurnPoints : the new turn points
	 */
	public void setTurnPoints(TeamId team, int newTurnPoints) {
		turnPoints[team.ordinal()].set(newTurnPoints);
	}

	/**
	 * Gives the game points property (as read only) of a given team of the score
	 * bean
	 * 
	 * @param team : the team
	 * @return the game points property
	 */
	public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
		return gamePoints[team.ordinal()];
	}

	/**
	 * Updates the game points property of the score bean for a given team
	 * 
	 * @param team          : the team
	 * @param newGamePoints : the new game points
	 */
	public void setGamePoints(TeamId team, int newGamePoints) {
		gamePoints[team.ordinal()].set(newGamePoints);
	}

	/**
	 * Gives the total points property (as read only) of a given team of the score
	 * bean
	 * 
	 * @param team : the team
	 * @return the total points property
	 */
	public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
		return totalPoints[team.ordinal()];
	}

	/**
	 * Updates the total points property of the score bean for a given team
	 * 
	 * @param team           : the team
	 * @param newTotalPoints : the new total points
	 */
	public void setTotalPoints(TeamId team, int newTotalPoints) {
		totalPoints[team.ordinal()].set(newTotalPoints);
	}

	/**
	 * Gives the winning team property (as read only) of the score bean
	 * 
	 * @return the winning team property
	 */
	public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
		return winningTeamProperty;
	}

	/**
	 * Updates the winning team property of the score bean
	 * 
	 * @param winningTeam : the winning team
	 */
	public void setWinningTeam(TeamId winningTeam) {
		winningTeamProperty.set(winningTeam);
	}
}