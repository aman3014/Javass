package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Represents the state of turn of a Jass Game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class TurnState {

	private final long pkScore;
	private final long pkUnplayedCards;
	private final int pkTrick;

	private TurnState(long pkScore, long pkUnplayedCards, int pkTrick) {
		this.pkScore = pkScore;
		this.pkUnplayedCards = pkUnplayedCards;
		this.pkTrick = pkTrick;
	}

	/**
	 * Creates a new turn state constructed using the values given in the parameters
	 * 
	 * @param trump       (Color) : the trump color of the turn
	 * @param score       (Score) : the current score of the game
	 * @param firstPlayer (PlayedId) : the first player of the first trick of the
	 *                    turn
	 * @return (TurnState) : a new turn state with values given in the parameters
	 */
	public static TurnState initial(Card.Color trump, Score score, PlayerId firstPlayer) {
		return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, PackedTrick.firstEmpty(trump, firstPlayer));
	}

	/**
	 * Creates a new turn state constructed using the packed values given in the
	 * parameters
	 * 
	 * @param pkScore         (long) : the current score of the game
	 * @param pkUnplayedCards (long) : the packed set of cards yet to be played in
	 *                        the turn
	 * @param pkTrick         (int) : the current packed trick of the turn
	 * @return (TurnState) : a new turn state with packed values given in the
	 *         parameters
	 * @throws IllegalArgumentException if any of the values provided are invalid
	 */
	public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick)
			throws IllegalArgumentException {
		checkArgument(
				PackedScore.isValid(pkScore) && PackedCardSet.isValid(pkUnplayedCards) && PackedTrick.isValid(pkTrick));
		return new TurnState(pkScore, pkUnplayedCards, pkTrick);
	}

	/**
	 * Gets the current score (packed) of the game
	 * 
	 * @return (long) : the packed score of the game
	 */
	public long packedScore() {
		return pkScore;
	}

	/**
	 * Gets the packed set of unplayed cards of the turn
	 * 
	 * @return (long) : the packed set of unplayed cards of the turn
	 */
	public long packedUnplayedCards() {
		return pkUnplayedCards;
	}

	/**
	 * Gets the current trick (packed) of the turn
	 * 
	 * @return (int) : the current trick (packed) of the turn
	 */
	public int packedTrick() {
		return pkTrick;
	}

	/**
	 * Gets the current score of the game
	 * 
	 * @return (Score) : the current score of the game
	 */
	public Score score() {
		return Score.ofPacked(pkScore);
	}

	/**
	 * Gets the set of unplayed cards of the turn
	 * 
	 * @return (CardSet) : the set of unplayed cards of the turn
	 */
	public CardSet unplayedCards() {
		return CardSet.ofPacked(pkUnplayedCards);
	}

	/**
	 * Gets the current trick of the turn
	 * 
	 * @return (Trick) : the current trick of the turn
	 */
	public Trick trick() {
		return Trick.ofPacked(pkTrick);
	}

	/**
	 * Checks if the turn is terminal (if the last trick has been played)
	 * 
	 * @return (boolean) : true if the turn is terminal
	 */
	public boolean isTerminal() {
		return pkTrick == PackedTrick.INVALID;
	}

	/**
	 * Gets the identity of the player who needs to play the next card
	 * 
	 * @return (PlayerId) : the identity of the player who needs to play the next
	 *         card
	 * @throws IllegalStateException if the current trick of the turn is full (hence
	 *                               there is no next player for this trick)
	 */
	public PlayerId nextPlayer() throws IllegalStateException {
		if (PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		return PackedTrick.player(pkTrick, PackedTrick.size(pkTrick));
	}

	/**
	 * Creates the turn state corresponding to the one after a new card is played
	 * 
	 * @param card (Card) : the card that is played
	 * @return (TurnState) : the state of the turn after the new card is played
	 * @throws IllegalStateException if the current trick of the turn was already
	 *                               full (hence the new card cannot be played)
	 */
	public TurnState withNewCardPlayed(Card card) throws IllegalStateException {
		if (PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		int pkCard = card.packed();
		return new TurnState(pkScore, PackedCardSet.remove(pkUnplayedCards, pkCard),
				PackedTrick.withAddedCard(pkTrick, pkCard));
	}

	/**
	 * Creates the turn state corresponding to the one after a full trick is
	 * collected
	 * 
	 * @return (TurnState) : the state of the turn after the full trick is collected
	 * @throws IllegalStateException if the current trick of the turn wasn't full
	 *                               (hence it cannot be collected)
	 */
	public TurnState withTrickCollected() throws IllegalStateException {
		if (!PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		return new TurnState(PackedScore.withAdditionalTrick(pkScore, PackedTrick.winningPlayer(pkTrick).team(),
				PackedTrick.points(pkTrick)), pkUnplayedCards, PackedTrick.nextEmpty(pkTrick));
	}

	/**
	 * Creates the turn state corresponding to the one after a new card is played
	 * and the new trick is collected if the trick is then full
	 * 
	 * @param card (Card) : the card that is played
	 * @return (TurnState) : the updated turn state after the new card is played
	 *         (and trick colleceted if necessary)
	 * @throws IllegalStateException if the current trick of the turn was already
	 *                               full (hence the new card cannot be played)
	 */
	public TurnState withNewCardPlayedAndTrickCollected(Card card) throws IllegalStateException {
		TurnState t = withNewCardPlayed(card);
		return PackedTrick.isFull(t.packedTrick()) ? t.withTrickCollected() : t;
	}
}