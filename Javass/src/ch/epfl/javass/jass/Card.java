package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.jass.PackedCard.isValid;
import static ch.epfl.javass.jass.PackedCard.pack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a card in a game of Jass
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class Card {

	private final int pkCard;

	private Card(int pkCard) {
		this.pkCard = pkCard;
	}

	/**
	 * Constructs a card of a given color and rank
	 * 
	 * @param c (Color) : color of the card
	 * @param r (Rank) : rank of the card
	 * @return (Card) : card of the given color and rank
	 */
	public static Card of(Card.Color c, Card.Rank r) {
		return new Card(pack(c, r));
	}

	/**
	 * Constructs a card using a packed card
	 * 
	 * @param packed (int) : integer value of the packed card
	 * @return (Card) : card with the same attributes as the packed card
	 * @throws IllegalArgumentException if packed does not represent a valid packed
	 *                                  card
	 */
	public static Card ofPacked(int packed) throws IllegalArgumentException {
		checkArgument(isValid(packed));
		return new Card(packed);
	}

	/**
	 * Gets the packed version of the card
	 * 
	 * @return (int) : the packed version of the card
	 */
	public int packed() {
		return pkCard;
	}

	/**
	 * Gets the color of the card
	 * 
	 * @return (Color) : color of the card
	 */
	public Color color() {
		return PackedCard.color(pkCard);
	}

	/**
	 * Gets the rank of the card
	 * 
	 * @return (Rank) : rank of the card
	 */
	public Rank rank() {
		return PackedCard.rank(pkCard);
	}

	/**
	 * Determines if the card is better than another card
	 * 
	 * @param trump (Color) : trump color for the current round
	 * @param that  (Card) : card to be compared to
	 * @return (boolean) : true if the card is better than the one passed in
	 *         argument, false otherwise
	 */
	public boolean isBetter(Color trump, Card that) {
		return PackedCard.isBetter(trump, pkCard, that.packed());
	}

	/**
	 * Calculates the points attributed to the card, given the trump color
	 * 
	 * @param trump (Color) : trump color for the current round
	 * @return (int) : value of the card
	 */
	public int points(Color trump) {
		return PackedCard.points(trump, pkCard);
	}

	@Override
	public boolean equals(Object thatO) {
		return thatO instanceof Card && ((Card) thatO).pkCard == pkCard;
	}

	@Override
	public int hashCode() {
		return pkCard;
	}

	@Override
	public String toString() {
		return PackedCard.toString(pkCard);
	}

	/**
	 * Represents the possible colors of cards in a game of Jass
	 * 
	 * @author Julian Blackwell (289803)
	 * @author Aman Bansal (297535)
	 */
	public enum Color {
		SPADE("pique"), HEART("coeur"), DIAMOND("carreau"), CLUB("trèfle");

		/**
		 * List containing all the colors
		 */
		public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));

		/**
		 * Number of colors
		 */
		public static final int COUNT = ALL.size();

		private final String colorName;

		private Color(String frenchName) {
			this.colorName = frenchName;
		}

		@Override
		public String toString() {
			switch (colorName) {
			case "pique":
				return "\u2660";
			case "coeur":
				return "\u2665";
			case "carreau":
				return "\u2666";
			case "trèfle":
				return "\u2663";
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Represents the possible ranks of cards in a game of Jass
	 * 
	 * @author Julian Blackwell (289803)
	 * @author Aman Bansal (297535)
	 */
	public enum Rank {
		SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A");

		/**
		 * List containing all the ranks
		 */
		public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(values()));

		/**
		 * Number of ranks
		 */
		public static final int COUNT = ALL.size();

		/**
		 * Determines the order of strength of a trump card
		 * 
		 * @return (int) : strength of the trump card
		 */
		public int trumpOrdinal() {
			switch (rankName) {
			case "6":
				return 0;
			case "7":
				return 1;
			case "8":
				return 2;
			case "10":
				return 3;
			case "Q":
				return 4;
			case "K":
				return 5;
			case "A":
				return 6;
			case "9":
				return 7;
			case "J":
				return 8;
			default:
				throw new IllegalArgumentException();
			}
		}

		private final String rankName;

		private Rank(String name) {
			this.rankName = name;
		}

		@Override
		public String toString() {
			return rankName;
		}
	}
}