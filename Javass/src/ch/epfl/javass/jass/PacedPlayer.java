package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

/**
 * Represents a paced player (who takes a minimum amount of time to play a card)
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public class PacedPlayer implements Player {

	private final Player underlyingPlayer;
	private final double minTime;

	/**
	 * Constructor for a Paced Player
	 * 
	 * @param underlyingPlayer (Player) : the underlying player of the paced player
	 * @param minTime          (double) : the minimum time (in seconds) the paced
	 *                         player should take to a play a card
	 */
	public PacedPlayer(Player underlyingPlayer, double minTime) {
		this.underlyingPlayer = underlyingPlayer;
		this.minTime = minTime;
	}

	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		long start = System.currentTimeMillis();

		Card cardToPlay = underlyingPlayer.cardToPlay(state, hand);
		long end = System.currentTimeMillis();
		long t = (long) (minTime * 1000 - (end - start));

		if (t > 0) {
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return cardToPlay;
	}

	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		underlyingPlayer.setPlayers(ownId, playerNames);
	}

	@Override
	public void updateHand(CardSet newHand) {
		underlyingPlayer.updateHand(newHand);
	}

	@Override
	public void setTrump(Card.Color trump) {
		underlyingPlayer.setTrump(trump);
	}

	@Override
	public void updateTrick(Trick newTrick) {
		underlyingPlayer.updateTrick(newTrick);
	}

	@Override
	public void updateScore(Score score) {
		underlyingPlayer.updateScore(score);
	}

	@Override
	public void setWinningTeam(TeamId winningTeam) {
		underlyingPlayer.setWinningTeam(winningTeam);
	}

	@Override
	public Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass) {
		return underlyingPlayer.chooseTrump(chooser, hand, canPass);
	}
}