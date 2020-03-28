package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.jass.PackedCardSet.difference;
import static ch.epfl.javass.jass.PackedCardSet.get;
import static ch.epfl.javass.jass.PackedCardSet.remove;
import static ch.epfl.javass.jass.PackedCardSet.size;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.epfl.javass.jass.Card.Color;;

public class ParallelMctsPlayer implements Player {
	private PlayerId ownId;
	private SplittableRandom rng;
	private int iterations;
	private final static int c = 40;
	private final int nbTrees;

	public ParallelMctsPlayer(PlayerId ownId, long rngSeed, int iterations, int nbTrees)
			throws IllegalArgumentException {
		checkArgument(iterations >= Jass.HAND_SIZE);
		this.ownId = ownId;
		this.rng = new SplittableRandom(rngSeed);
		this.iterations = iterations;
		this.nbTrees = nbTrees;
	}

	public ParallelMctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
		this(ownId, rngSeed, iterations, Runtime.getRuntime().availableProcessors());
	}
	
	@Override
	public Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass) {
		Color mode = null;
		int highestFreq = 0;
		
		for (Color c : Color.ALL) {
			if (hand.subsetOfColor(c).size() > highestFreq) {
				mode = c;
			}
		}
		
		if (highestFreq == 3 && canPass) { // The other player's choice will be just as good, if not better
			return null;
		}
		
		return mode;
	}

	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		ExecutorService executorService = Executors.newWorkStealingPool(nbTrees);
		List<Future<Pair>> futurePointsToIterationsRatios = new ArrayList<>();
		List<Pair> pointsToIterationsRatios = new ArrayList<>();

		for (int i = 0; i < nbTrees; ++i) {
			futurePointsToIterationsRatios.add(
					executorService.submit(Executor.executor(rng.nextInt(), state, hand, ownId, iterations)::execute));
		}

		for (Future<Pair> f : futurePointsToIterationsRatios) {
			try {
				pointsToIterationsRatios.add(f.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		executorService.shutdown();
		return state.trick().playableCards(hand).get(Pair.bestRatioIndex(pointsToIterationsRatios));
	}

	@FunctionalInterface
	private interface Executor {
		Pair execute();

		static Executor executor(int rngSeed, TurnState state, CardSet hand, PlayerId ownId, int iterations) {
			return () -> {
				SplittableRandom seed = new SplittableRandom(rngSeed);
				Node node = new Node(state, hand.packed(), null, ownId);
				while (node.ownIterations < iterations) {
					node.main(ownId, seed, c);
				}
				int index = node.chooseNodeIndex(0);
				Node bestChild = node.childrenNodes[index];
				return Pair.build(index, bestChild.points / (double) bestChild.ownIterations);
			};
		}
	}
	
	private static final class Pair {
		private final int index;
		private final double ratio;
		
		Pair(int index, double ratio) {
			this.index = index;
			this.ratio = ratio;
		}
		
		static Pair build(int index, double ratio) {
			return new Pair(index, ratio);
		}
		
		static int bestRatioIndex(List<Pair> pairs) {
			int bestIndex = -1;
			double bestRatio = -1D;
			for (Pair p : pairs) {
				if (p.ratio > bestRatio) {
					bestIndex = p.index;
					bestRatio = p.ratio;
				}
			}
			return bestIndex;
		}
	}

	private static final class Node {
		private final Node[] childrenNodes;
		private final TurnState turnState;
		private final long hand;
		private int points;
		private int ownIterations;
		private final long playableCards;
		private final Node parent;

		Node(TurnState turnState, long hand, Node parent, PlayerId ownId) {
			this.turnState = turnState;
			this.hand = hand;
			this.parent = parent;
			if (turnState.isTerminal()) {
				playableCards = PackedCardSet.EMPTY;
			} else if (turnState.nextPlayer().equals(ownId)) {
				playableCards = PackedTrick.playableCards(turnState.packedTrick(), hand);
			} else {
				playableCards = PackedTrick.playableCards(turnState.packedTrick(),
						difference(turnState.packedUnplayedCards(), hand));
			}

			childrenNodes = new Node[PackedCardSet.size(playableCards)];
		}

		/*
		 * Adds a child node to this node and simulates the added node
		 */
		private void addChildNode(PlayerId ownId, SplittableRandom rng) {
			// because every node is simulated at least once except for the initial node
			int k = (parent == null) ? ownIterations : ownIterations - 1;
			int nodeCard = get(playableCards, k);
			(childrenNodes[k] = new Node(turnState.withNewCardPlayedAndTrickCollected(Card.ofPacked(nodeCard)),
					remove(hand, nodeCard), this, ownId)).simulateTurn(ownId, rng);
		}

		/*
		 * Chooses which node to explore
		 */
		private int chooseNodeIndex(int c) {
			double maxV = Double.NEGATIVE_INFINITY;
			int maxIndex = -1;
			double twolnNp = 2 * Math.log(ownIterations);
			for (int i = 0; i < childrenNodes.length; ++i) {
				Node child = childrenNodes[i];
				if (child == null) {
					break;
				}
				int childIterations = child.ownIterations;
				double v = child.points / (double) childIterations + c * (Math.sqrt(twolnNp / childIterations));
				if (v > maxV) {
					maxV = v;
					maxIndex = i;
				}
			}
			return maxIndex;
		}

		/*
		 * Updates the score associated to this node and propagates the score upwards in
		 * the tree
		 */
		private void updatePointsAndIterations(long score) {
			if (parent != null) {
				points += PackedScore.turnPoints(score, parent.turnState.nextPlayer().team());
				parent.updatePointsAndIterations(score);
			}
			++ownIterations;
		}

		/*
		 * Simulates a random turn continuing from the node's turn state
		 */
		private void simulateTurn(PlayerId ownId, SplittableRandom rng) {
			long othersCards = difference(turnState.packedUnplayedCards(), hand);
			long currentHand = hand;
			TurnState tempTurnState = turnState;
			while (!tempTurnState.isTerminal()) {
				if (tempTurnState.nextPlayer().equals(ownId)) {
					long playable = PackedTrick.playableCards(turnState.packedTrick(), currentHand);
					int cardToPlay = get(playable, rng.nextInt(size(playable)));
					currentHand = remove(currentHand, cardToPlay);
					tempTurnState = tempTurnState.withNewCardPlayedAndTrickCollected(Card.ofPacked(cardToPlay));
				} else {
					long playable = PackedTrick.playableCards(turnState.packedTrick(), othersCards);
					int cardToPlay = get(playable, rng.nextInt(size(playable)));
					othersCards = remove(othersCards, cardToPlay);
					tempTurnState = tempTurnState.withNewCardPlayedAndTrickCollected(Card.ofPacked(cardToPlay));
				}
			}
			updatePointsAndIterations(tempTurnState.packedScore());
		}

		/*
		 * Executes the MCTS algorithm (implemented recursively) for one iteration. It
		 * does the following in the different situations : 1. If the node is terminal,
		 * the turnstate score is simply stored and back propagated 2. If the node has
		 * more possible children, a new child node is added and simulated 3. Otherwise,
		 * the best node is chosen to explore further
		 */
		private void main(PlayerId ownId, SplittableRandom rng, int c) {
			if (childrenNodes.length == 0) {
				updatePointsAndIterations(turnState.packedScore());
			} else if (childrenNodes[childrenNodes.length - 1] == null) {
				addChildNode(ownId, rng);
			} else {
				childrenNodes[chooseNodeIndex(c)].main(ownId, rng, c);
			}
		}
	}
}