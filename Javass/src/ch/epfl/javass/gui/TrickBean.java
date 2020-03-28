package ch.epfl.javass.gui;

import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.unmodifiableObservableMap;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;

/**
 * A JavaFX bean containing a trick of Jass
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class TrickBean {
	private final ObjectProperty<Color> trumpProperty;
	private final ObjectProperty<PlayerId> trumpChooserProperty;
	private final BooleanProperty choicePassingProperty;
	private final ObservableMap<PlayerId, Card> trick;
	private final ObjectProperty<PlayerId> winningPlayerProperty;

	/**
	 * Constructs a new trick bean
	 */
	public TrickBean() {
		trumpProperty = new SimpleObjectProperty<>();
		trumpChooserProperty = new SimpleObjectProperty<>();
		trick = observableHashMap();
		winningPlayerProperty = new SimpleObjectProperty<>();
		choicePassingProperty = new SimpleBooleanProperty(false);

		for (PlayerId p : PlayerId.ALL) {
			trick.put(p, null);
		}
	}
	
	/**
	 * Gives the choice passing property (as read only) of the trick bean
	 * @return the choice passing property
	 */
	public ReadOnlyBooleanProperty choicePassingProperty() {
		return choicePassingProperty;
	}
	
	/**
	 * Gives the trump chooser property (as read only) of the trick bean
	 * @return the trump chooser property
	 */
	public ReadOnlyObjectProperty<PlayerId> trumpChooserProperty() {
		return trumpChooserProperty;
	}

	/**
	 * Gives the trump property (as read only) of the trick bean
	 * 
	 * @return the trump property
	 */
	public ReadOnlyObjectProperty<Color> trumpProperty() {
		return trumpProperty;
	}

	/**
	 * Gives the winning player property (as read only) of the trick bean
	 * 
	 * @return the winning player property
	 */
	public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
		return winningPlayerProperty;
	}

	/**
	 * Gives the trick (as an unmodifiable map from player to card) of the trick
	 * bean
	 * 
	 * @return the winning team property
	 */
	public ObservableMap<PlayerId, Card> trick() {
		return unmodifiableObservableMap(trick);
	}

	/**
	 * Updates the trick of the trick bean
	 * 
	 * @param newTrick : the new trick
	 */
	public void setTrick(Trick newTrick) {
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			trick.replace(newTrick.player(i), i < newTrick.size() ? newTrick.card(i) : null);
		}
		winningPlayerProperty.set(newTrick.isEmpty() ? null : newTrick.winningPlayer());
	}

	/**
	 * Updates the trump of the trick bean
	 * 
	 * @param newTrump : the new trump
	 */
	public void setTrump(Color newTrump) {
		trumpProperty.set(newTrump);
	}

	/**
	 * Updates the trump chooser property of the trick bean
	 * 
	 * @param chooser : the new trump chooser
	 */
	public void setTrumpChooser(PlayerId chooser) {
		trumpChooserProperty.set(chooser);
	}

	/**
	 * Updates the choice deferral property of the trick bean
	 * 
	 * @param canPass : indicates if the trump chooser can pass his choice
	 */
	public void setChoicePassing(boolean canPass) {
		choicePassingProperty.set(canPass);
	}
}