package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.Preconditions.checkIndex;

/**
 * Class representing a Trick
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class Trick {

	/**
	 * Represents an invalid trick
	 */
	public final static Trick INVALID = new Trick(PackedTrick.INVALID);

	private final int pkTrick;

	private Trick(int pkTrick) {
		this.pkTrick = pkTrick;
	}

	/**
	 * Constructs an empty trick with given a trump color and a first player
	 * 
	 * @param trump       (Color) : trump color
	 * @param firstPlayer (PlayerId) : the first player
	 * @return (Trick) : the empty trick given a trump color and first player
	 */
	public static Trick firstEmpty(Card.Color trump, PlayerId firstPlayer) {
		return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
	}

	/**
	 * Constructs a trick based on a packed trick
	 * 
	 * @param packed (int) : the packed representation of the trick
	 * @return (Trick) : the trick given its packed representation
	 * @throws IllegalArgumentException if the packed representation is not valid
	 */
	public static Trick ofPacked(int packed) throws IllegalArgumentException {
		checkArgument(PackedTrick.isValid(packed));
		return new Trick(packed);
	}

	/**
	 * Gets the packed representation of the trick
	 * 
	 * @return (int) : packed representation of the trick
	 */
	public int packed() {
		return pkTrick;
	}

	/**
	 * Constructs the next empty trick following a full trick
	 * 
	 * @return (Trick) : the next empty trick with the same trump color and the
	 *         first player being the winning player
	 * @throws IllegalStateException if the trick is not empty
	 */
	public Trick nextEmpty() throws IllegalStateException {
		if (!PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		return new Trick(PackedTrick.nextEmpty(pkTrick));
	}

	/**
	 * Determines if a trick is empty
	 * 
	 * @return (boolean) : true if the trick is empty, false otherwise
	 */
	public boolean isEmpty() {
		return PackedTrick.isEmpty(pkTrick);
	}

	/**
	 * Determines if a trick is full
	 * 
	 * @return (boolean) : true if the trick is full, false otherwise
	 */
	public boolean isFull() {
		return PackedTrick.isFull(pkTrick);
	}

	/**
	 * Determines if a trick is the last of a turn
	 * 
	 * @return (boolean) : true if the trick is the last trick of the turn, false
	 *         otherwise
	 */
	public boolean isLast() {
		return PackedTrick.isLast(pkTrick);
	}

	/**
	 * Determines the size of a trick
	 * 
	 * @return (int) : the size of the trick
	 */
	public int size() {
		return PackedTrick.size(pkTrick);
	}

	/**
	 * Gets the trump color of a trick
	 * 
	 * @return (Color) : the trump color of the trick
	 */
	public Card.Color trump() {
		return PackedTrick.trump(pkTrick);
	}

	/**
	 * Gets the index of a trick in its turn
	 * 
	 * @return (int) : the index of the trick
	 */
	public int index() {
		return PackedTrick.index(pkTrick);
	}

	/**
	 * Gets the player at an index of a trick with the player at the index 0 being
	 * the first player of the trick
	 * 
	 * @param index (int) : index of the player in the trick
	 * @return (PlayerId) : the player at the given index in the trick
	 * @throws IndexOutOfBoundsException if the index is not included between
	 *                                   0(included) and 4(excluded)
	 */
	public PlayerId player(int index) throws IndexOutOfBoundsException {
		checkIndex(index, 4);
		return PackedTrick.player(pkTrick, index);
	}

	/**
	 * Gets the card played at a given index of the trick
	 * 
	 * @param index (int) : index of the card
	 * @return (Card) : the card played at the given index of the trick
	 * @throws IndexOutOfBoundsException if the index is not included between
	 *                                   0(included) and the size of the trick
	 *                                   (excluded)
	 */
	public Card card(int index) throws IndexOutOfBoundsException {
		checkIndex(index, PackedTrick.size(pkTrick));
		return Card.ofPacked(PackedTrick.card(pkTrick, index));
	}

	/**
	 * Constructs a new trick following the previous one with a new card played
	 * 
	 * @param c (Card) : the added card
	 * @return (Trick) : updated trick, with the given card that has been played
	 * @throws IllegalStateException if the trick is full
	 */
	public Trick withAddedCard(Card c) throws IllegalStateException {
		if (PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		return new Trick(PackedTrick.withAddedCard(pkTrick, c.packed()));
	}

	/**
	 * Determines the base color of the trick (color of the first played card)
	 * 
	 * @return (Color) : the base color of the trick
	 * @throws IllegalStateException if the trick is empty
	 */
	public Card.Color baseColor() throws IllegalStateException {
		if (PackedTrick.isEmpty(pkTrick)) {
			throw new IllegalStateException();
		}

		return PackedTrick.baseColor(pkTrick);
	}

	/**
	 * Determines the set of playable cards given a trick and a hand
	 * 
	 * @param hand (CardSet) : the hand of cards
	 * @return (CardSet) : the set of possible playable cards from the given hand
	 * @throws IllegalStateException if the trick is full
	 */
	public CardSet playableCards(CardSet hand) throws IllegalStateException {
		if (PackedTrick.isFull(pkTrick)) {
			throw new IllegalStateException();
		}

		return CardSet.ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
	}

	/**
	 * Determines the points attributed to a trick
	 * 
	 * @return (int) : the value of the trick
	 */
	public int points() {
		return PackedTrick.points(pkTrick);
	}

	/**
	 * Determines the winning player of a trick
	 * 
	 * @return (PlayerId) : the winning player of the trick
	 * @throws IllegalStateException if the trick is empty
	 */
	public PlayerId winningPlayer() throws IllegalStateException {
		if (PackedTrick.isEmpty(pkTrick)) {
			throw new IllegalStateException();
		}

		return PackedTrick.winningPlayer(pkTrick);
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof Trick && ((Trick) that).pkTrick == pkTrick;
	}

	@Override
	public int hashCode() {
		return pkTrick;
	}

	@Override
	public String toString() {
		return PackedTrick.toString(pkTrick);
	}
}