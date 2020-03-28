package ch.epfl.javass.gui;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableSet;
import static javafx.collections.FXCollections.unmodifiableObservableList;
import static javafx.collections.FXCollections.unmodifiableObservableSet;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**
 * A JavaFX bean containing a hand of Jass
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class HandBean {
	private final ObservableList<Card> hand;
	private final ObservableSet<Card> playableCards;

	/**
	 * Constructs a new hand bean
	 */
	public HandBean() {
		hand = observableArrayList();
		playableCards = observableSet();

		for (int i = 0; i < Jass.HAND_SIZE; ++i) {
			hand.add(null);
		}
	}

	/**
	 * Gives the hand (as an unmodifiable observable list of cards) of the hand bean
	 * 
	 * @return the list of cards
	 */
	public ObservableList<Card> hand() {
		return unmodifiableObservableList(hand);
	}

	/**
	 * Updates the hand of the hand bean
	 * 
	 * @param newHand : the new hand
	 */
	public void setHand(CardSet newHand) {
		if (newHand.size() == Jass.HAND_SIZE) {
			for (int i = 0; i < Jass.HAND_SIZE; ++i) {
				hand.set(i, newHand.get(i));
			}
		} else {
			for (int i = 0; i < Jass.HAND_SIZE; ++i) {
				Card current = hand.get(i);
				if (current != null && !newHand.contains(current)) {
					hand.set(i, null);
				}
			}
		}
	}

	/**
	 * Gives the playable cards (as an unmodifiable set list of cards) of the hand
	 * bean
	 * 
	 * @return the unmodifiable set of playable cards
	 */
	public ObservableSet<Card> playableCards() {
		return unmodifiableObservableSet(playableCards);
	}

	/**
	 * Updates the playable cards of the hand bean
	 * 
	 * @param newPlayableCards : the playable cards
	 */
	public void setPlayableCards(CardSet newPlayableCards) {
		playableCards.clear();
		for (int i = 0; i < newPlayableCards.size(); ++i) {
			playableCards.add(newPlayableCards.get(i));
		}
	}
}