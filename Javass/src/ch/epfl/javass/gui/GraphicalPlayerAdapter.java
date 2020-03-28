package ch.epfl.javass.gui;

import static javafx.application.Platform.runLater;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

/**
 * A player which can adapt a graphical interface (GraphicalPlayer) to make it a
 * player
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class GraphicalPlayerAdapter implements Player {
	private HandBean hb;
	private ScoreBean sb;
	private TrickBean tb;
	private ChatBean cb;
	private GraphicalPlayer gp;
	private ArrayBlockingQueue<Card> cardToPlayQueue;
	private ArrayBlockingQueue<Color> trumpQueue;
	private ArrayBlockingQueue<TurnState> turnStateQueue;

	/**
	 * Constructs a new graphical player adapter
	 * 
	 * @param cb : the chat bean associated to this player
	 */
	public GraphicalPlayerAdapter(ChatBean cb) {
		hb = new HandBean();
		sb = new ScoreBean();
		tb = new TrickBean();
		this.cb = cb;
		cardToPlayQueue = new ArrayBlockingQueue<>(1);
		trumpQueue = new ArrayBlockingQueue<>(2);
		turnStateQueue = new ArrayBlockingQueue<>(1);
	}

	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		runLater(() -> {
			hb.setPlayableCards(state.trick().playableCards(hand));
		});
		try {
			turnStateQueue.add(state);
			Card c = cardToPlayQueue.take();
			runLater(() -> {
				hb.setPlayableCards(CardSet.EMPTY);
			});
			if (turnStateQueue.remainingCapacity() == 0) {
				turnStateQueue.clear();
			}
			return c;
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		gp = new GraphicalPlayer(ownId, playerNames, hb, sb, tb, cb, cardToPlayQueue, trumpQueue, turnStateQueue);
		runLater(() -> {
			gp.createStage().show();
		});
	}

	@Override
	public void updateHand(CardSet newHand) {
		runLater(() -> {
			hb.setHand(newHand);
		});
	}

	@Override
	public void setTrump(Card.Color trump) {
		runLater(() -> {
			tb.setTrump(trump);
			tb.setTrumpChooser(null);
		});
	}

	@Override
	public void updateTrick(Trick newTrick) {
		runLater(() -> {
			tb.setTrick(newTrick);
		});
	}

	@Override
	public void updateScore(Score score) {
		runLater(() -> {
			for (TeamId t : TeamId.ALL) {
				sb.setTurnTricks(t, score.turnTricks(t));
				sb.setTurnPoints(t, score.turnPoints(t));
				sb.setTotalPoints(t, score.totalPoints(t));
				sb.setGamePoints(t, score.gamePoints(t));
			}
		});
	}

	@Override
	public void setWinningTeam(TeamId winningTeam) {
		runLater(() -> {
			sb.setWinningTeam(winningTeam);
		});
	}

	@Override
	public Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass) {
		runLater(() -> {
			tb.setTrumpChooser(chooser);
			tb.setChoicePassing(canPass);
		});

		try {
			Color trump = trumpQueue.take();
			if (trumpQueue.take().equals(trump)) {
				return trump;
			} else {
				return null;
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}