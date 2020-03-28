package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits64.extract;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;

/**
 * Represents the packed version of the score of a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class PackedScore {

	private PackedScore() {
	}

	private static final int NB_TRICKS_START = 0;
	private static final int NB_TRICKS_SIZE = 4;
	private static final int TURN_POINTS_START = NB_TRICKS_START + NB_TRICKS_SIZE;
	private static final int TURN_POINTS_SIZE = 9;
	private static final int GAME_POINTS_START = NB_TRICKS_START + NB_TRICKS_SIZE + TURN_POINTS_SIZE;
	private static final int GAME_POINTS_SIZE = 11;
	private static final int UNUSED_BITS_START = NB_TRICKS_START + NB_TRICKS_SIZE + TURN_POINTS_SIZE + GAME_POINTS_SIZE;
	private static final int UNUSED_BITS_SIZE = 8;
	private static final int MAX_TURN_POINTS = 257;
	private static final int MAX_GAME_POINTS = 2000;

	private static final int teamTwoStart(int i) {
		return i + Integer.SIZE;
	}

	/**
	 * Initial state of the game score
	 */
	public static final long INITIAL = 0L;

	/**
	 * Determines if a packed score is valid. All unused bits have to be equal to 0;
	 * The number of tricks won can not exceed 9; The number of points won in a turn
	 * can not exceed 257; A team's points cannot exceed 2000
	 * 
	 * @param pkScore (long) : packed score to be checked
	 * @return (boolean) : true if the packed score is valid, false otherwise
	 */
	public static boolean isValid(long pkScore) {
		return extract(pkScore, UNUSED_BITS_START, UNUSED_BITS_SIZE) == 0
				&& extract(pkScore, teamTwoStart(UNUSED_BITS_START), UNUSED_BITS_SIZE) == 0
				&& extract(pkScore, NB_TRICKS_START, NB_TRICKS_SIZE) <= Jass.TRICKS_PER_TURN
				&& extract(pkScore, teamTwoStart(NB_TRICKS_START), NB_TRICKS_SIZE) <= Jass.TRICKS_PER_TURN
				&& extract(pkScore, TURN_POINTS_START, TURN_POINTS_SIZE) <= MAX_TURN_POINTS
				&& extract(pkScore, teamTwoStart(TURN_POINTS_START), TURN_POINTS_SIZE) <= MAX_TURN_POINTS
				&& extract(pkScore, GAME_POINTS_START, GAME_POINTS_SIZE) <= MAX_GAME_POINTS
				&& extract(pkScore, teamTwoStart(GAME_POINTS_START), GAME_POINTS_SIZE) <= MAX_GAME_POINTS;
	}

	/**
	 * Constructs a packed score given the current statistics of both teams
	 * 
	 * @param turnTricks1 (int) : number of tricks won by team 1 in the current turn
	 * @param turnPoints1 (int) : number of points won by team 1 in the current turn
	 * @param gamePoints1 (int) : current number of points won by team 1 in the game
	 * @param turnTricks2 (int) : number of tricks won by team 2 in the current turn
	 * @param turnPoints2 (int) : number of points won by team 2 in the current turn
	 * @param gamePoints2 (int) : current number of points won by team 2 in the game
	 * @return (long) : packed score
	 */
	public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2,
			int gamePoints2) {
		return Bits64.pack(
				Bits32.pack(turnTricks1, NB_TRICKS_SIZE, turnPoints1, TURN_POINTS_SIZE, gamePoints1, GAME_POINTS_SIZE),
				Integer.SIZE,
				Bits32.pack(turnTricks2, NB_TRICKS_SIZE, turnPoints2, TURN_POINTS_SIZE, gamePoints2, GAME_POINTS_SIZE),
				Integer.SIZE);
	}

	/**
	 * Gets the number of tricks won by a team during the current turn
	 * 
	 * @param pkScore (long) : the current game score
	 * @param t       (TeamId) : the given team
	 * @return (int) : number of tricks won by the given team for the current turn
	 */
	public static int turnTricks(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (int) extract(pkScore, t.equals(TeamId.TEAM_1) ? NB_TRICKS_START : teamTwoStart(NB_TRICKS_START),
				NB_TRICKS_SIZE);
	}

	/**
	 * Gets the number of points won by a team during the current turn
	 * 
	 * @param pkScore (long) : the current game score
	 * @param t       (TeamId) : the given team
	 * @return (int) : number of points won by the given team for the current turn
	 */
	public static int turnPoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (int) extract(pkScore, t.equals(TeamId.TEAM_1) ? TURN_POINTS_START : teamTwoStart(TURN_POINTS_START),
				TURN_POINTS_SIZE);
	}

	/**
	 * Gets the number of points won by a team during the game
	 * 
	 * @param pkScore (long) : the current game score
	 * @param t       (TeamId) : the given team
	 * @return (int) : the number of points amassed by the given team during the
	 *         game
	 */
	public static int gamePoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (int) extract(pkScore, t.equals(TeamId.TEAM_1) ? GAME_POINTS_START : teamTwoStart(GAME_POINTS_START),
				GAME_POINTS_SIZE);
	}

	/**
	 * Determines the sum of the number of points won during the current turn and
	 * the game by a team
	 * 
	 * @param pkScore (long) : the current game score
	 * @param t       (TeamId) : the given team
	 * @return (int) : the sum of the number of points won during a turn and during
	 *         the game by a given team
	 */
	public static int totalPoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return gamePoints(pkScore, t) + turnPoints(pkScore, t);
	}

	/**
	 * Determines the packed score of the game after an additional trick is played
	 * 
	 * @param pkScore     (long) : the current game score
	 * @param winningTeam (TeamId) : the team that won the trick
	 * @param trickPoints (int) : number of points won by the team in the trick
	 * @return (long) : updated packed score of the game after the trick was played
	 */
	public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
		assert isValid(pkScore);

		int newTurnTricks = turnTricks(pkScore, winningTeam) + 1;

		int newWinningTeamTurnPoints = turnPoints(pkScore, winningTeam) + trickPoints;

		if (newTurnTricks == Jass.TRICKS_PER_TURN) {
			newWinningTeamTurnPoints += Jass.MATCH_ADDITIONAL_POINTS;
		}

		if (winningTeam.equals(TeamId.TEAM_1)) {
			return pack(newTurnTricks, newWinningTeamTurnPoints, gamePoints(pkScore, winningTeam),
					turnTricks(pkScore, winningTeam.other()), turnPoints(pkScore, winningTeam.other()),
					gamePoints(pkScore, winningTeam.other()));

		} else {
			return pack(turnTricks(pkScore, winningTeam.other()), turnPoints(pkScore, winningTeam.other()),
					gamePoints(pkScore, winningTeam.other()), newTurnTricks, newWinningTeamTurnPoints,
					gamePoints(pkScore, winningTeam));
		}
	}

	/**
	 * Constructs the packed score of the game after the end of a turn
	 * 
	 * @param pkScore (long) : the current game score
	 * @return (long) : the updated packed game score after the end of the turn
	 */
	public static long nextTurn(long pkScore) {
		assert isValid(pkScore) && turnTricks(pkScore, TeamId.TEAM_1) + turnTricks(pkScore, TeamId.TEAM_2) == Jass.TRICKS_PER_TURN;
		return pack(0, 0, totalPoints(pkScore, TeamId.TEAM_1), 0, 0, totalPoints(pkScore, TeamId.TEAM_2));
	}

	/**
	 * Returns the textual representation of the game's score
	 * 
	 * @param pkScore (long) : the current game score
	 * @return (String) : textual description of the current game score
	 */
	public static String toString(long pkScore) {
		assert isValid(pkScore);
		return "(" + turnTricks(pkScore, TeamId.TEAM_1) + "," + turnPoints(pkScore, TeamId.TEAM_1) + ","
				+ gamePoints(pkScore, TeamId.TEAM_1) + ")/(" + turnTricks(pkScore, TeamId.TEAM_2) + ","
				+ turnPoints(pkScore, TeamId.TEAM_2) + "," + gamePoints(pkScore, TeamId.TEAM_2) + ")";
	}
}