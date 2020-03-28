package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

/**
 * Represents a player in the Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public interface Player {

	/**
	 * Determines which card the player wants to play
	 * 
	 * @param state (TurnState) : the current TurnState of the game
	 * @param hand  (CardSet) : the current hand of the player
	 * @return (Card) : the card the player wants to play
	 */
	abstract Card cardToPlay(TurnState state, CardSet hand);

	/**
	 * Tells the player his PlayerId and the names of the other players in the game
	 * 
	 * @param ownId       (PlayerId) : the players PlayerId
	 * @param playerNames (Map<PlayerId, String>) : the map containing the player
	 *                    names associated with their PlayerIds
	 */
	default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
	}

	/**
	 * Updates the hand of the player
	 * 
	 * @param newHand (CardSet) : the new hand of the player
	 */
	default void updateHand(CardSet newHand) {
	}

	/**
	 * Updates the player's information about the current trump color of the game
	 * 
	 * @param trump (Card.Color) : the trump color
	 */
	default void setTrump(Card.Color trump) {
	}

	/**
	 * Updates the player's information about the current trick of the game
	 * 
	 * @param newTrick (Trick) : the new trick
	 */
	default void updateTrick(Trick newTrick) {
	}

	/**
	 * Updates the player's information about the current score of the game
	 * 
	 * @param score (Score) : the current score of the game
	 */
	default void updateScore(Score score) {
	}

	/**
	 * Informs the player about the winning team of the game
	 * 
	 * @param winningTeam (TeamId) : the TeamId of the winning team
	 */
	default void setWinningTeam(TeamId winningTeam) {
	}
	
	/**
	 * Determines which trump is going to be selected
	 * 
	 * @param chooser : the PlayerId of the player whose turn it is to choose the
	 *                trump
	 * @param hand    : the hand of the player
	 * @param canPass : indicates if the player has the choice to pass the choice of
	 *                the trump to his partner
	 * @return the chosen trump color
	 */
	abstract Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass);
}
