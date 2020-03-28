package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import ch.epfl.javass.jass.PackedScore;

/**
 * Represents the score of a game of Jass
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class Score {

	/**
	 * Initial score of a game of Jass
	 */
	public static final Score INITIAL = new Score(PackedScore.INITIAL);

	private long pkScore;

	private Score(long pkScore) {
		this.pkScore = pkScore;
	}

	/**
	 * Constructs a score based on a packed score
	 * 
	 * @param packed (long) : the packed score
	 * @return (Score) : representing the packed score
	 * @throws IllegalArgumentException : if the parameter packed doesn't represent
	 *                                  a valid packed score
	 */
	public static Score ofPacked(long packed) throws IllegalArgumentException {
		checkArgument(PackedScore.isValid(packed));
		return new Score(packed);
	}

	/**
	 * Gets the packed score
	 * 
	 * @return (long) : the packed score
	 */
	public long packed() {
		return pkScore;
	}

	/**
	 * Gets the number of tricks won by a team in the current turn
	 * 
	 * @param t (TeamId) : representing the team
	 * @return (int) : the number of tricks won by the team in the current game
	 */
	public int turnTricks(TeamId t) {
		return PackedScore.turnTricks(pkScore, t);
	}

	/**
	 * Gets the number of points won by a team in the current turn
	 * 
	 * @param t (TeamId) : representing the team
	 * @return (int) : the number of points won by the team in the current game
	 */
	public int turnPoints(TeamId t) {
		return PackedScore.turnPoints(pkScore, t);
	}

	/**
	 * Gets the number of points won by a team in the current game excluding the
	 * current turn
	 * 
	 * @param t (TeamId) : representing the team
	 * @return (int) : the number of points won by the team in the current game
	 *         excluding the current turn
	 */
	public int gamePoints(TeamId t) {
		return PackedScore.gamePoints(pkScore, t);
	}

	/**
	 * Determines the number of points won by a team in the current game including
	 * the current turn
	 * 
	 * @param t (TeamId) : representing the team
	 * @return (int) : the number of points won by the team in the current game
	 *         including the current turn
	 */
	public int totalPoints(TeamId t) {
		return PackedScore.totalPoints(pkScore, t);
	}

	/**
	 * Constructs the new score of the game after an additional trick played
	 * 
	 * @param winningTeam (TeamId) : the team
	 * @param trickPoints (int) : the points to be won from the last trick
	 * @return (Score) the updated score
	 * @throws IllegalArgumentException if trickPoints is less than 0
	 */
	public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) throws IllegalArgumentException {
		checkArgument(trickPoints >= 0);
		return new Score(PackedScore.withAdditionalTrick(pkScore, winningTeam, trickPoints));
	}

	/**
	 * Constructs the score of the game after the end of a turn
	 * 
	 * @return (Score) : the updated score
	 */
	public Score nextTurn() {
		return new Score(PackedScore.nextTurn(pkScore));
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Score && ((Score) other).pkScore == pkScore;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(pkScore);
	}

	@Override
	public String toString() {
		return PackedScore.toString(pkScore);
	}
}