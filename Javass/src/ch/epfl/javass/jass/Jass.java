package ch.epfl.javass.jass;

/**
 * Interface containing constants used in a game of Jass
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public interface Jass {

	/**
	 * The hand size in a game of Jass
	 */
	public static final int HAND_SIZE = 9;

	/**
	 * The number of tricks in a turn in a game of Jass
	 */
	public static final int TRICKS_PER_TURN = 9;

	/**
	 * The number of points required for a team to win a game of Jass
	 */
	public static final int WINNING_POINTS = 1000;

	/**
	 * The number of additional points awarded if a team wins all the tricks in a
	 * turn
	 */
	public static final int MATCH_ADDITIONAL_POINTS = 100;

	/**
	 * The number of additional points awarded when a team wins the last trick in a
	 * turn
	 */
	public static final int LAST_TRICK_ADDITIONAL_POINTS = 5;

	/**
	 * The TCP Port used for game related communication between players
	 */
	public static final int GAME_PORT = 5108;
	
	/**
	 * The TCP Port used for chat communication between players
	 */
	public static final int CHAT_PORT = 5109;
	
	/**
	 * The maximum time (in seconds) a player has to play a card
	 */
	public static final int MAX_TIME_TO_PlAY = 10;
}