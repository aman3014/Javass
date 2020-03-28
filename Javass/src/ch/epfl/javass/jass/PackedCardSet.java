package ch.epfl.javass.jass;

import static ch.epfl.javass.jass.PackedCard.color;
import static ch.epfl.javass.jass.PackedCard.rank;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits64;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * Represents the packed version of a card set in a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class PackedCardSet {

	private PackedCardSet() {
	}

	/**
	 * An empty set
	 */
	public static final long EMPTY = 0L;
	/**
	 * A full set
	 */
	public static final long ALL_CARDS = 0x01FF_01FF_01FF_01FFL;

	/*
	 * For use in the method trumpAbove(int pkCard)
	 */
	private final static long[] TRUMP_ABOVE = trumpAbove();

	/*
	 * For use in the method subsetOfColor(long pkCardSet, Color color)
	 */
	private final static long[] SUBSET_OF_COLOR = subsetOfColor();
	
	private final static long[] trumpAbove() {
		long[] array = new long[Rank.COUNT];
		
		Color trump = Color.ALL.get(0);
		for (Rank r1 : Rank.ALL) {
			int current = PackedCard.pack(trump, r1);
			for (Rank r : Rank.ALL) {
				int pkCard = PackedCard.pack(trump, r);
				if (PackedCard.isBetter(trump, pkCard, current)) {
					array[r1.ordinal()] = PackedCardSet.add(array[r1.ordinal()], pkCard);
				}
			}
		}
		
		return array;
	}
	
	private final static long[] subsetOfColor() {
		long[] array = new long[Color.COUNT];
		
		for (Color c : Color.ALL) {
			array[c.ordinal()] = Bits64.mask(c.ordinal() * (Long.SIZE / Color.COUNT), Rank.COUNT);
		}
		
		return array;
	}

	/**
	 * Checks if a packed card set is valid
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @return (boolean) : true if the parameter is valid
	 */
	public static boolean isValid(long pkCardSet) {
		return (pkCardSet | ALL_CARDS) == ALL_CARDS;
	}

	/**
	 * Determines the packed set of cards strictly stronger than a packed card,
	 * knowing that it's color is the trump color
	 * 
	 * @param pkCard (int) : the packed card
	 * @return (long) : the packed set of cards strictly stronger than the packed
	 *         card, knowing that it's color is the trump color
	 */
	public static long trumpAbove(int pkCard) {
		assert PackedCard.isValid(pkCard);
		return TRUMP_ABOVE[rank(pkCard).ordinal()] << (color(pkCard).ordinal() * (Long.SIZE / Color.COUNT));
	}

	/**
	 * Constructs a packed set of cards containing only one card
	 * 
	 * @param pkCard (int) : the packed card
	 * @return (long) : the packed set of cards containing only the packed card in
	 *         the parameter
	 */
	public static long singleton(int pkCard) {
		assert PackedCard.isValid(pkCard);
		return 1L << pkCard;
	}

	/**
	 * Checks if a packed card set is empty
	 * 
	 * @param pkCardSet (long) : the packed card set to be checked for emptiness
	 * @return (boolean) : true if the packed card set is empty
	 */
	public static boolean isEmpty(long pkCardSet) {
		assert isValid(pkCardSet);
		return pkCardSet == EMPTY;
	}

	/**
	 * Determines the size of a packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @return (int) : the size of the packed card set
	 */
	public static int size(long pkCardSet) {
		assert isValid(pkCardSet);
		return Long.bitCount(pkCardSet);
	}

	/**
	 * Gets the the packed version of the card at an index of a packed card set; the
	 * card of index 0 being the one represented by the lowest one bit
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @param index     (int) : the index
	 * @return (int) : the packed card at the parameter index of the packed card set
	 */
	public static int get(long pkCardSet, int index) {
		assert isValid(pkCardSet);
		for (int j = 0; j < index; ++j) {
			pkCardSet -= Long.lowestOneBit(pkCardSet);
		}

		int i = Long.numberOfTrailingZeros(pkCardSet);

		return PackedCard.pack(Card.Color.ALL.get(i / (Long.SIZE / Color.COUNT)),
				Card.Rank.ALL.get(i % (Long.SIZE / Color.COUNT)));
	}

	/**
	 * Adds a card to a packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @param pkCard    (int) : the packed card
	 * @return (long) : the updated packed card set
	 */
	public static long add(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet) && PackedCard.isValid(pkCard);
		return pkCardSet | singleton(pkCard);
	}

	/**
	 * Removes a card from a packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @param pkCard    (int) : the packed card
	 * @return (long) : the updated packed card set
	 */
	public static long remove(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet) && PackedCard.isValid(pkCard);
		return pkCardSet & (~singleton(pkCard));
	}

	/**
	 * Checks if a packed card set contains a card
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @param pkCard    (int) : the packed card
	 * @return (boolean) : true if the packed card set contains the packed card
	 */
	public static boolean contains(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet) && PackedCard.isValid(pkCard);
		return (pkCardSet & singleton(pkCard)) != EMPTY;
	}

	/**
	 * Constructs the complement of a packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @return (long) : the complement of the packed card set
	 */
	public static long complement(long pkCardSet) {
		assert isValid(pkCardSet);
		return pkCardSet ^ ALL_CARDS;
	}

	/**
	 * Constructs the union of two packed card sets
	 * 
	 * @param pkCardSet1 (long) : the packed card set 1
	 * @param pkCardSet2 (long) : the packed card set 2
	 * 
	 * @return (long) : the union set of the two packed card sets
	 */
	public static long union(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1) && isValid(pkCardSet2);
		return pkCardSet1 | pkCardSet2;
	}

	/**
	 * Constructs the intersection of two packed card sets
	 * 
	 * @param pkCardSet1 (long) : the packed card set 1
	 * @param pkCardSet2 (long) : the packed card set 2
	 * @return (long) : the intersection set of the two packed card sets
	 */
	public static long intersection(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1) && isValid(pkCardSet2);
		return pkCardSet1 & pkCardSet2;
	}

	/**
	 * Constructs the difference of two packed card sets
	 * 
	 * @param pkCardSet1 (long) : the packed card set 1
	 * @param pkCardSet2 (long) : the packed card set 2
	 * @return (long) : the difference set of the two packed card sets
	 */
	public static long difference(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1) && isValid(pkCardSet2);
		return pkCardSet1 & ~pkCardSet2;
	}

	/**
	 * Constructs the subset of a color of a packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @param color     (Card.Color) : the color
	 * @return (long) : the subset of the given color of the given packed card set
	 */
	public static long subsetOfColor(long pkCardSet, Card.Color color) {
		assert isValid(pkCardSet);
		return SUBSET_OF_COLOR[color.ordinal()] & pkCardSet;
	}

	/**
	 * Returns the textual representation of the packed card set
	 * 
	 * @param pkCardSet (long) : the packed card set
	 * @return (String) : the textual representation of the packed card set
	 */
	public static String toString(long pkCardSet) {
		assert isValid(pkCardSet);

		StringJoiner j = new StringJoiner(",", "{", "}");

		for (int i = 0; i < size(pkCardSet); ++i) {
			j.add(PackedCard.toString(get(pkCardSet, i)));
		}

		return j.toString();
	}
}