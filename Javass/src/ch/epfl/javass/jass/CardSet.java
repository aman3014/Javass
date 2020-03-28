package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;

import java.util.List;

/**
 * Represents a Card Set in a game of Jass
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class CardSet {

	/**
	 * The empty Jass card set
	 */
	public final static CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);
	/**
	 * The full Jass card set
	 */
	public final static CardSet ALL_CARDS = new CardSet(PackedCardSet.ALL_CARDS);

	private final long pkCardSet;

	private CardSet(long pkCardSet) {
		this.pkCardSet = pkCardSet;
	}

	/**
	 * Returns a new card set containing the cards in the list given as a parameter
	 * 
	 * @param cards (List<Card>) : the list of cards to be put into the set
	 * @return (CardSet) : the card set containing all the cards in the list
	 */
	public static CardSet of(List<Card> cards) {
		long p = PackedCardSet.EMPTY;

		for (Card card : cards) {
			p = PackedCardSet.add(p, card.packed());
		}

		return new CardSet(p);
	}

	/**
	 * Returns a new card set representing the cards the given packed card set
	 * 
	 * @param packed (long) : the packed card set
	 * @return (CardSet) : a new card set representing the packed card set
	 * @throws IllegalArgumentException if the packed card set isn't valid
	 */
	public static CardSet ofPacked(long packed) throws IllegalArgumentException {
		checkArgument(PackedCardSet.isValid(packed));
		return new CardSet(packed);
	}

	/**
	 * Returns the card set as a packed card set
	 * 
	 * @return (long) : the packed card set representing this card set
	 */
	public long packed() {
		return pkCardSet;
	}

	/**
	 * Method to check if the card set is empty
	 * 
	 * @return (boolean) : true if the card set is empty
	 */
	public boolean isEmpty() {
		return PackedCardSet.isEmpty(pkCardSet);
	}

	/**
	 * Method to find the size of the card set
	 * 
	 * @return (int) : the size of the card set
	 */
	public int size() {
		return PackedCardSet.size(pkCardSet);
	}

	/**
	 * Gives the card at the index (parameter) of a packed card set
	 * 
	 * @param index (int) : the index
	 * @return (Card) : the card at the index of the packed card set
	 */
	public Card get(int index) {
		return Card.ofPacked(PackedCardSet.get(pkCardSet, index));
	}

	/**
	 * Adds a given card to the card set to make a new card set
	 * 
	 * @param card (Card) : the card to be added
	 * @return (CardSet) : the new updated card set
	 */
	public CardSet add(Card card) {
		return new CardSet(PackedCardSet.add(pkCardSet, card.packed()));
	}

	/**
	 * Removes a given card from the card set to make a new card set
	 * 
	 * @param card (Card) : the card to be removed
	 * @return (CardSet) : the new updated card set
	 */
	public CardSet remove(Card card) {
		return new CardSet(PackedCardSet.remove(pkCardSet, card.packed()));
	}

	/**
	 * Checks if the card set contains a given card
	 * 
	 * @param card (Card) : the card
	 * @return (boolean) : true if the card set contains the given card
	 */
	public boolean contains(Card card) {
		return PackedCardSet.contains(pkCardSet, card.packed());
	}

	/**
	 * Constructs the complement of the card set
	 * 
	 * @return (CardSet) : the complement set of the card set
	 */
	public CardSet complement() {
		return new CardSet(PackedCardSet.complement(pkCardSet));
	}

	/**
	 * Constructs the union set of this card set with another card set
	 * 
	 * @param that : the other card set
	 * @return (CardSet) : the union set of the two card sets
	 */
	public CardSet union(CardSet that) {
		return new CardSet(PackedCardSet.union(pkCardSet, that.pkCardSet));
	}

	/**
	 * Constructs the intersection set of this card set with another card set
	 * 
	 * @param that : the other card set
	 * @return (CardSet) : the intersection set of the two card sets
	 */
	public CardSet intersection(CardSet that) {
		return new CardSet(PackedCardSet.intersection(pkCardSet, that.pkCardSet));
	}

	/**
	 * Constructs the difference set of this card set with another card set
	 * 
	 * @param that : the other card set
	 * @return (CardSet) : the difference set of the two card sets
	 */
	public CardSet difference(CardSet that) {
		return new CardSet(PackedCardSet.difference(pkCardSet, that.pkCardSet));
	}

	/**
	 * Constructs the subset of a particular color of the card set
	 * 
	 * @param color (Card.Color) : the color
	 * @return (CardSet) : the subset of a particular colored cards of the card set
	 */
	public CardSet subsetOfColor(Card.Color color) {
		return new CardSet(PackedCardSet.subsetOfColor(pkCardSet, color));
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof CardSet && ((CardSet) that).pkCardSet == pkCardSet;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(pkCardSet);
	}

	@Override
	public String toString() {
		return PackedCardSet.toString(pkCardSet);
	}
}