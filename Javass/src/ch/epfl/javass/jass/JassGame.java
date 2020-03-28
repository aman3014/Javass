package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.jass.Card.Color;

/**
 * Represents a game of Jass
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class JassGame {

	private final static Card SEVEN_OF_DIAMOND = Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN);

	private final Random shuffleRng;
	private final Map<PlayerId, Player> players;
	private final Map<PlayerId, String> playerNames;

	private PlayerId currentTurnFirstPlayer;
	private TurnState turnState;
	private Map<PlayerId, CardSet> hands;

	/**
	 * Constructor for a JassGame
	 * 
	 * @param rngSeed     (long) : the seed used to generate randomness in the
	 *                    JassGame
	 * @param players     (Map<PlayerId, Player>) : the map of players associated to
	 *                    their PlayerIds
	 * @param playerNames (Map<PlayerId, String>) : the map of player names
	 *                    associated to their PlayerIds
	 */
	public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
		Random rng = new Random(rngSeed);
		this.shuffleRng = new Random(rng.nextLong());
		this.players = Collections.unmodifiableMap(new EnumMap<>(players));
		this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));

		for (PlayerId p : PlayerId.ALL) {
			players.get(p).setPlayers(p, this.playerNames);
		}

		distributeHands();

		// Choose the first player that has the seven of diamond in his hand
		for (PlayerId p : PlayerId.ALL) {
			if (hands.get(p).contains(SEVEN_OF_DIAMOND)) {
				this.currentTurnFirstPlayer = p;
				break;
			}
		}

		setTurnState(Score.INITIAL);
	}

	private void distributeHands() {

		CardSet[] hands = { CardSet.EMPTY, CardSet.EMPTY, CardSet.EMPTY, CardSet.EMPTY };

		ArrayList<Integer> indices = new ArrayList<>();
		for (int i = 0; i < Jass.HAND_SIZE * PlayerId.COUNT; i++) {
			indices.add(i);
		}

		Collections.shuffle(indices, shuffleRng);

		HashMap<PlayerId, CardSet> handsMap = new HashMap<>();

		for (int i = 0; i < PlayerId.COUNT; i++) {
			for (int j = 0; j < Jass.HAND_SIZE; ++j) {
				hands[i] = hands[i].add(CardSet.ALL_CARDS.get(indices.get(i * Jass.HAND_SIZE + j)));
			}
			PlayerId p = PlayerId.ALL.get(i);
			handsMap.put(p, hands[p.ordinal()]);
		}

		this.hands = new EnumMap<>(handsMap);

		for (PlayerId p : PlayerId.ALL) {
			players.get(p).updateHand(this.hands.get(p));
		}
	}

	private Color askTrump(PlayerId chooser, boolean canPass) {
		for (PlayerId p : PlayerId.ALL) {
			if (!p.equals(chooser)) {
				players.get(p).chooseTrump(chooser, hands.get(p), canPass);
			}
		}
		return players.get(chooser).chooseTrump(chooser, hands.get(chooser), canPass);
	}

	private void setTurnState(Score score) {
		Color trump = askTrump(currentTurnFirstPlayer, true);
		if (trump == null) {
			trump = askTrump(PlayerId.ALL.get((currentTurnFirstPlayer.ordinal() + TeamId.COUNT) % PlayerId.COUNT),
					false);
		}
		this.turnState = TurnState.initial(trump, score, currentTurnFirstPlayer);
		for (PlayerId p : PlayerId.ALL) {
			players.get(p).setTrump(trump);
		}
	}

	private TeamId winningTeam() {
		return turnState.score().totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS ? TeamId.TEAM_1
				: turnState.score().totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS ? TeamId.TEAM_2 : null;
	}

	/**
	 * Checks if the JassGame is over
	 * 
	 * @return (boolean) : true if the game is over
	 */
	public boolean isGameOver() {
		return winningTeam() != null;
	}

	/**
	 * Advances the JassGame by playing one trick
	 */
	public void advanceToEndOfNextTrick() {
		if (turnState.trick().isFull()) {
			turnState = turnState.withTrickCollected();
		}

		// Start a new turn with the updated score if the current turn has ended
		if (turnState.isTerminal()) {
			currentTurnFirstPlayer = PlayerId.ALL.get((currentTurnFirstPlayer.ordinal() + 1) % PlayerId.COUNT);
			distributeHands();
			setTurnState(turnState.score().nextTurn());
		}
		
		if (isGameOver()) {
			TeamId winningTeam = winningTeam();
			Score score = turnState.score();

			for (PlayerId p : PlayerId.ALL) {
				players.get(p).updateScore(score);
				players.get(p).setWinningTeam(winningTeam);
			}

			return;
		}

		Score score = turnState.score();
		Trick currentTrick = turnState.trick();

		for (PlayerId p : PlayerId.ALL) {
			players.get(p).updateScore(score);
			players.get(p).updateTrick(currentTrick);
		}

		// Play a trick
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			PlayerId currentPlayerId = turnState.nextPlayer();
			Player currentPlayer = players.get(currentPlayerId);

			// Choose a card to play
			CardSet currentHand = hands.get(currentPlayerId);
			Card cardToPlay = currentPlayer.cardToPlay(turnState, currentHand);

			// Play the card
			turnState = turnState.withNewCardPlayed(cardToPlay);

			// Remove the played card from the player's hand
			currentHand = currentHand.remove(cardToPlay);
			hands.replace(currentPlayerId, currentHand);
			currentPlayer.updateHand(currentHand);

			// Inform all players of the updated trick
			currentTrick = turnState.trick();
			for (PlayerId p : PlayerId.ALL) {
				players.get(p).updateTrick(currentTrick);
			}
		}
	}
}