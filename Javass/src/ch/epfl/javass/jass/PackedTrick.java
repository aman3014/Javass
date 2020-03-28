package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits32.extract;
import static ch.epfl.javass.bits.Bits32.mask;
import static ch.epfl.javass.bits.Bits32.pack;
import static ch.epfl.javass.jass.PackedCardSet.difference;
import static ch.epfl.javass.jass.PackedCardSet.intersection;
import static ch.epfl.javass.jass.PackedCardSet.singleton;
import static ch.epfl.javass.jass.PackedCardSet.subsetOfColor;
import static ch.epfl.javass.jass.PackedCardSet.union;

import java.util.StringJoiner;

import ch.epfl.javass.jass.Card.Color;

/**
 * Represents the packed version of a trick in a Jass game
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class PackedTrick {

	private PackedTrick() {
	}

	private static final int TRICK_START = 0;
	private static final int CARD_SIZE = 6;
	private static final int FOUR_CARDS_SIZE = PlayerId.COUNT * CARD_SIZE;
	private static final int INDEX_START = TRICK_START + CARD_SIZE * 4;
	private static final int INDEX_SIZE = 4;
	private static final int FIRST_PLAYER_START = TRICK_START + FOUR_CARDS_SIZE + INDEX_SIZE;
	private static final int FIRST_PLAYER_SIZE = 2;
	private static final int TRUMP_START = TRICK_START + FOUR_CARDS_SIZE + INDEX_SIZE + FIRST_PLAYER_SIZE;
	private static final int TRUMP_SIZE = 2;

	/**
	 * Represents an invalid trick
	 */
	public static final int INVALID = -1;

	/**
	 * Determines if a packed trick is valid; Note : an empty card spot must only be
	 * represented by PackedCard.INVALID
	 * 
	 * @param pkTrick (int) : packed trick
	 * @return (boolean) : true if the packed trick is valid, false otherwise
	 */
	public static boolean isValid(int pkTrick) {
		// Index of the trick must be smaller than 9
		if (extract(pkTrick, INDEX_START, INDEX_SIZE) >= Jass.TRICKS_PER_TURN) {
		    return false;
		}

		boolean invalidFound = false;
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			int currentCard = extract(pkTrick, i * CARD_SIZE, CARD_SIZE);

			if (!PackedCard.isValid(currentCard)) {
				invalidFound = true;
				if (currentCard != PackedCard.INVALID) {
				    return false;
				}
			} else if (invalidFound) {
			    return false;
			}
		}

		return true;
	}

	/**
	 * Constructs an empty packed trick given the trump color and the first player
	 * 
	 * @param trump       (Color) : trump color
	 * @param firstPlayer (PlayerId) : the first player
	 * @return (int) : the empty packed trick given a trump color and first player
	 */
	public static int firstEmpty(Color trump, PlayerId firstPlayer) {
		return pack(mask(TRICK_START, FOUR_CARDS_SIZE), FOUR_CARDS_SIZE, 0, INDEX_SIZE,
				pack(firstPlayer.ordinal(), FIRST_PLAYER_SIZE, trump.ordinal(), TRUMP_SIZE),
				FIRST_PLAYER_SIZE + TRUMP_SIZE);
	}

	/**
	 * Constructs the next empty packed trick following a full packed trick
	 * 
	 * @param pkTrick (int) : the previous trick
	 * @return (int) : the next empty trick with the same trump color and the first
	 *         player being the winning player
	 */
	public static int nextEmpty(int pkTrick) {
		assert isValid(pkTrick);
		return isLast(pkTrick) ? INVALID
				: pack(mask(TRICK_START, FOUR_CARDS_SIZE), FOUR_CARDS_SIZE, index(pkTrick) + 1, INDEX_SIZE,
						pack(winningPlayer(pkTrick).ordinal(), FIRST_PLAYER_SIZE, trump(pkTrick).ordinal(), TRUMP_SIZE),
						FIRST_PLAYER_SIZE + TRUMP_SIZE);
	}

	/**
	 * Determines if a packed trick is the last of a turn
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (boolean) : true if the packed trick is the last of the turn
	 */
	public static boolean isLast(int pkTrick) {
		assert isValid(pkTrick);
		return index(pkTrick) == Jass.TRICKS_PER_TURN - 1;
	}

	/**
	 * Determines if a packed trick is empty
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (boolean) : true if the trick is empty, false otherwise
	 */
	public static boolean isEmpty(int pkTrick) {
		assert isValid(pkTrick);
		return card(pkTrick, 0) == PackedCard.INVALID;
	}

	/**
	 * Determines if a packed trick is full
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (boolean) : true if the given trick is full, false otherwise
	 */
	public static boolean isFull(int pkTrick) {
		assert isValid(pkTrick);
		return card(pkTrick, (PlayerId.COUNT - 1)) != PackedCard.INVALID;
	}

	/**
	 * Determines the size of a packed trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (int) : the size of the given trick
	 */
	public static int size(int pkTrick) {
		assert isValid(pkTrick);
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			if (card(pkTrick, i) == PackedCard.INVALID) {
				return i;
			}
		}
		return PlayerId.COUNT;
	}

	/**
	 * Gets the trump color of a packed trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (Color) : the trump color of the given trick
	 */
	public static Color trump(int pkTrick) {
		assert isValid(pkTrick);
		return Color.ALL.get(extract(pkTrick, TRUMP_START, TRUMP_SIZE));
	}

	/**
	 * Gets the player at the index of a packed trick with the player at the index 0
	 * being the first player of the trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @param index   (int) : index of the player in the trick
	 * @return (PlayerId) : the player at the given index in the trick
	 */
	public static PlayerId player(int pkTrick, int index) {
		assert isValid(pkTrick);
		return PlayerId.ALL.get((extract(pkTrick, FIRST_PLAYER_START, FIRST_PLAYER_SIZE) + index) % PlayerId.COUNT);
	}

	/**
	 * Gets the index a packed trick in its turn
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (int) : the index of the trick
	 */
	public static int index(int pkTrick) {
		assert isValid(pkTrick);
		return extract(pkTrick, INDEX_START, INDEX_SIZE);
	}

	/**
	 * Gets the packed representation of the card played at a index of a packed
	 * trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @param index   (int) : the index of the card in the trick
	 * @return (int) : the packed card representation of the card played at the
	 *         given index of the given trick
	 */
	public static int card(int pkTrick, int index) {
		assert isValid(pkTrick);
		return extract(pkTrick, index * CARD_SIZE, CARD_SIZE);
	}

	/**
	 * Constructs a packed trick following a packed trick with a new card played
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @param pkCard  (int) : the packed card played/added to the trick
	 * @return (int) : updated value of the given trick, with the given card played
	 */
	public static int withAddedCard(int pkTrick, int pkCard) {
		assert isValid(pkTrick) && PackedCard.isValid(pkCard);

		int sizeTimesSix = size(pkTrick) * CARD_SIZE;
		if (sizeTimesSix == 0) {
			return pack(pkCard, CARD_SIZE, extract(pkTrick, CARD_SIZE, Integer.SIZE - CARD_SIZE),
					Integer.SIZE - CARD_SIZE);
		}

		return pack(extract(pkTrick, TRICK_START, sizeTimesSix), sizeTimesSix, pkCard, CARD_SIZE,
				extract(pkTrick, sizeTimesSix + CARD_SIZE, Integer.SIZE - CARD_SIZE - sizeTimesSix),
				Integer.SIZE - CARD_SIZE - sizeTimesSix);
	}

	/**
	 * Determines the base color of a packed trick (color of the first played card)
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (Color) : the base color of the given trick
	 */
	public static Color baseColor(int pkTrick) {
		assert isValid(pkTrick);
		return PackedCard.color(card(pkTrick, 0));
	}

	/**
	 * Determines the set of packed set of playable cards given a packed trick and a
	 * hand
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @param pkHand  (long) : set of cards in a player's hand
	 * @return (long) : the set of playable cards given the trick and hand
	 */
	public static long playableCards(int pkTrick, long pkHand) {
		assert isValid(pkTrick) && PackedCardSet.isValid(pkHand);

		// Any card can be played if the trick is empty
		if (isEmpty(pkTrick)) {
			return pkHand;
		}

		Color trump = trump(pkTrick);
		Color baseColor = baseColor(pkTrick);
		long playableCards = subsetOfColor(pkHand, baseColor);

		// If the base color is trump and the only trump card in the hand is the Jack,
		// or he doesn't have any trumps,
		// any card in the hand can be played.
		// But, if he has any other trump card, he has to play a trump card
		if (trump.equals(baseColor)) {
			if (singleton(PackedCard.pack(trump, Card.Rank.JACK)) == playableCards
					|| PackedCardSet.isEmpty(playableCards)) {
				return pkHand;
			} else {
				return playableCards;
			}
		}

		long trumpCardsInHand = subsetOfColor(pkHand, trump);

		// If the player has no base color cards, all the non-trump cards are added to
		// the playable cards
		if (PackedCardSet.isEmpty(playableCards)) {
			playableCards = difference(pkHand, trumpCardsInHand);
		}

		// All the trump cards better than the best trump (if played, otherwise all
		// trumps are added) are also added
		int bestCardPlayed = card(pkTrick, bestCardIndex(pkTrick, trump));

		if (PackedCard.color(bestCardPlayed).equals(trump)) {
			playableCards = union(playableCards,
					intersection(PackedCardSet.trumpAbove(bestCardPlayed), trumpCardsInHand));
		} else {
			playableCards = union(playableCards, trumpCardsInHand);
		}

		// If the only cards in hand are worse trump cards than the best trump card
		// already played, any card can be played
		if (PackedCardSet.isEmpty(playableCards)) {
			playableCards = pkHand;
		}

		return playableCards;
	}

	/**
	 * Determines the points attributed to a packed trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (int) : the value of the trick
	 */
	public static int points(int pkTrick) {
		assert isValid(pkTrick);

		int points = 0;
		Color trump = trump(pkTrick);

		for (int i = 0; i < size(pkTrick); ++i) {
			points += PackedCard.points(trump, card(pkTrick, i));
		}

		return isLast(pkTrick) ? points + Jass.LAST_TRICK_ADDITIONAL_POINTS : points;
	}

	/**
	 * Determines the winning player of a packed trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (int) : the identity of the winning player
	 */
	public static PlayerId winningPlayer(int pkTrick) {
		assert isValid(pkTrick);
		return player(pkTrick, bestCardIndex(pkTrick, trump(pkTrick)));
	}

	/**
	 * Returns the textual representation of a packed trick
	 * 
	 * @param pkTrick (int) : the packed trick
	 * @return (String) : the textual representation of the trick
	 */
	public static String toString(int pkTrick) {
		StringJoiner j = new StringJoiner(",");

		for (int i = 0; i < size(pkTrick); ++i) {
			j.add(PackedCard.toString(card(pkTrick, i)));
		}

		return j.toString();
	}

	/*
	 * Determines the index of the best card played in a packed trick
	 */
	private static int bestCardIndex(int pkTrick, Color trump) {
		int index = 0;
		int bestCard = card(pkTrick, 0);

		for (int i = 1; i < size(pkTrick); ++i) {
			int currentCard = card(pkTrick, i);
			if (PackedCard.isBetter(trump, currentCard, bestCard)) {
				bestCard = currentCard;
				index = i;
			}
		}

		return index;
	}
}