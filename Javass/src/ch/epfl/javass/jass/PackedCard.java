package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits32.extract;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * Contains methods allowing to manipulate cards (packed in the form of
 * integers) of a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class PackedCard {

	private PackedCard() {
	}

	// The following constants determine how a packed card represents a card
	private static final int RANK_START = 0;
	private static final int RANK_SIZE = 4;
	private static final int COLOR_START = RANK_START + RANK_SIZE;
	private static final int COLOR_SIZE = 2;
	private static final int UNUSED_BITS_START = RANK_START + RANK_SIZE + COLOR_SIZE;

	/*
	 * Array containing the points associated to each card rank (assuming its color
	 * is not the trump)
	 */
	private static final int[] POINTS = { 0, 0, 0, 0, 10, 2, 3, 4, 11 };

	/*
	 * Array containing the points associated to each card rank (assuming its color
	 * is the trump)
	 */
	private static final int[] TRUMP_POINTS = { 0, 0, 0, 14, 10, 20, 3, 4, 11 };

	/**
	 * Represents an invalid card
	 */
	public static final int INVALID = 0b111111;

	/**
	 * Determines if a card is valid
	 * 
	 * @param pkCard (int) : card packed as integer
	 * @return (boolean) : true if card is valid, false otherwise
	 */
	public static boolean isValid(int pkCard) {
		return (extract(pkCard, UNUSED_BITS_START, Integer.SIZE - UNUSED_BITS_START) == 0
				&& extract(pkCard, RANK_START, RANK_SIZE) < Rank.COUNT);
	}

	/**
	 * Packs a card with a given color and a rank
	 * 
	 * @param c (Card.Color) : given color
	 * @param r (Card.Rank) : given rank
	 * @return (int) packed card with color c and rank r
	 */
	public static int pack(Card.Color c, Card.Rank r) {
		return Bits32.pack(r.ordinal(), RANK_SIZE, c.ordinal(), COLOR_SIZE, 0, Integer.SIZE - UNUSED_BITS_START);
	}

	/**
	 * Returns the color of a card
	 * 
	 * @param pkCard (int) : packed card
	 * @return (Card.Color) color of the packed card
	 */
	public static Card.Color color(int pkCard) {
		assert isValid(pkCard);
		return Color.ALL.get(extract(pkCard, COLOR_START, COLOR_SIZE));
	}

	/**
	 * Returns the rank of a card
	 * 
	 * @param pkCard (int) : packed card
	 * @return (Card.Rank) rank of the packed card
	 */
	public static Card.Rank rank(int pkCard) {
		assert isValid(pkCard);
		return Rank.ALL.get(extract(pkCard, RANK_START, RANK_SIZE));
	}

	/**
	 * Determines if a card is better than another
	 * 
	 * @param trump   (Card.Color) : the trump color
	 * @param pkCardL (int) : the first card
	 * @param pkCardR (int) : the second card
	 * @return (boolean) : true if the first card is better than the second, false
	 *         otherwise
	 */
	public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
		assert isValid(pkCardL) && isValid(pkCardR);

		Card.Color one = color(pkCardL);
		Card.Color two = color(pkCardR);

		return one.equals(trump) ? !two.equals(trump) || rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal()
				: one.equals(two) && rank(pkCardL).ordinal() > rank(pkCardR).ordinal();
	}

	/**
	 * Determines the points attributed to a given card, given a trump color
	 * 
	 * @param trump  (Card.Color) : the trump color for the current round
	 * @param pkCard (int) : card to collect points
	 * @return (int) : points awarded to the card
	 */
	public static int points(Card.Color trump, int pkCard) {
		assert isValid(pkCard);
		return color(pkCard).equals(trump) ? TRUMP_POINTS[rank(pkCard).ordinal()] : POINTS[rank(pkCard).ordinal()];
	}

	/**
	 * Returns the textual representation of the packed card
	 * 
	 * @param pkCard (int) : card to be represented
	 * @return (String) : textual representation of the card
	 */
	public static String toString(int pkCard) {
		assert isValid(pkCard);
		return color(pkCard).toString() + rank(pkCard).toString();
	}
}