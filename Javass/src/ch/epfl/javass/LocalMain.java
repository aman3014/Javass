package ch.epfl.javass;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.gui.ChatBean;
import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.ParallelMctsPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import ch.epfl.javass.net.StringSerializer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Launches a game of Jass and connects to remote players if any
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class LocalMain extends Application {
	private final static int MIN_ARGS = 4, MAX_ARGS = 5, MAX_HUMAN_ARGS = 2, MAX_SIMULATED_ARGS = 3,
			MAX_REMOTE_ARGS = 3, MIN_SIMULATED_ITERATIONS = 10;
	private static final String[] DEFAULT_NAMES = { "Aline", "Bastien", "Colette", "David" };
	private static final int DEFAULT_MCTS_ITERATIONS = 10_000;
	private static final String DEFAULT_IP = "localhost";
	private static final double MINIMUM_MCTS_PLAYTIME = 2.0;

	private final Map<PlayerId, Player> players = new HashMap<>();
	private final Map<PlayerId, String> names = new HashMap<>();
	private Random rng;
	private ChatBean[] cbs = new ChatBean[PlayerId.COUNT];

	/**
	 * Launches the application
	 * 
	 * @param args : the arguments passed into the program
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Verify the arguments passed into the program
		// and initialize necessary elements to begin the game
		verifyArgumentsAndInitializeParameters(getParameters().getRaw());
		startChatting();

		// Define the game thread
		Thread gameThread = new Thread(() -> {
			JassGame g = new JassGame(rng.nextLong(), players, names);
			while (!g.isGameOver()) {
				g.advanceToEndOfNextTrick();
				try {
					// Wait 1 second before the trick is collected
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
		});

		// Ensure the thread ends correctly
		gameThread.setDaemon(true);
		// Start the game
		gameThread.start();
		// The name of the game window is specified
	}
	
	private void startChatting() {
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			ChatBean cb = cbs[i];
			int index = i;
			Player player = players.get(PlayerId.ALL.get(index));
			if (player instanceof RemotePlayerClient) {
				cb.getRecieved().addListener((c, o, n) -> {
					chatListener(index, n);
					cb.setReceived("");
				});
			} else if (player instanceof GraphicalPlayerAdapter) {
				cb.getSent().addListener((c, o, n) -> {
					chatListener(index, n);
					cb.setSent("");
				});
			}
		}
	}
	
	private void chatListener(int index, String newValue) {
		if (!newValue.isEmpty()) {
			for (int j = 0; j < PlayerId.COUNT; ++j) {
				if (index != j) {
					Player p = players.get(PlayerId.ALL.get(j));
					if (p instanceof RemotePlayerClient) {
						cbs[j].setSent(newValue);
					} else if (p instanceof GraphicalPlayerAdapter) {
						cbs[j].addMessage(newValue);
					}
				}
			}
		}
	}

	/*
	 * Verify if the arguments are correct and initialize the game elements given
	 * the arguments
	 */
	private void verifyArgumentsAndInitializeParameters(List<String> args) {
		if (args.size() < MIN_ARGS || args.size() > MAX_ARGS) {
			invalidNumberOfArguments();
		} else if (args.size() == MAX_ARGS) {
			try {
				rng = new Random(Long.parseLong(args.get(MAX_ARGS - 1)));
			} catch (NumberFormatException e) {
				error("Représentation invalide du noyaux : " + args.get(MAX_ARGS - 1));
			}
		} else {
			rng = new Random();
		}
		
		for (int i = 0; i < PlayerId.COUNT; ++i) {
			cbs[i] = new ChatBean();
		}

		for (PlayerId p : PlayerId.ALL) {
			String[] information = StringSerializer.split(':', args.get(p.ordinal()));
			switch (information[0]) {
			case "h":
				human(args, information, p);
				break;
			case "s":
				simulated(args, information, p);
				break;
			case "r":
				remote(args, information, p);
				break;
			default:
				error("Les types de joueurs ne peuvent être spécifiées que par les caractères 'h', 's' ou 'r'");
			}
		}
	}

	private void human(List<String> args, String[] information, PlayerId p) {
		if (information.length > MAX_HUMAN_ARGS) {
			error("Spécification invalide du joueur : " + args.get(p.ordinal()));
		}
		players.put(p, new GraphicalPlayerAdapter(cbs[p.ordinal()]));
		names.put(p, information.length == MAX_HUMAN_ARGS ? information[1] : DEFAULT_NAMES[p.ordinal()]);
	}

	private void simulated(List<String> args, String[] information, PlayerId p) {
		if (information.length > MAX_SIMULATED_ARGS) {
			error("Spécification invalide du joueur : " + args.get(p.ordinal()));
		}
		names.put(p, information.length > 1 ? !information[1].isEmpty() ? information[1] : DEFAULT_NAMES[p.ordinal()]
				: DEFAULT_NAMES[p.ordinal()]);
		try {
			int iterations = DEFAULT_MCTS_ITERATIONS;
			if (information.length == MAX_SIMULATED_ARGS) {
				if (iterations < MIN_SIMULATED_ITERATIONS) {
					error("Le nombre minimum d'itérations est 10 : " + args.get(p.ordinal()));
				}
				iterations = Integer.parseInt(information[2]);
			}
			players.put(p, new PacedPlayer(new ParallelMctsPlayer(p, rng.nextLong(), iterations), MINIMUM_MCTS_PLAYTIME));
		} catch (NumberFormatException e) {
			error("Représentation invalide du nombre d'itérations : " + args.get(p.ordinal()));
		}
	}

	private void remote(List<String> args, String[] information, PlayerId p) {
		if (information.length > MAX_REMOTE_ARGS) {
			error("spécification invalide du joueur : " + args.get(p.ordinal()));
		}
		names.put(p, information.length > 1 ? !information[1].isEmpty() ? information[1] : DEFAULT_NAMES[p.ordinal()]
				: DEFAULT_NAMES[p.ordinal()]);
		try {
			players.put(p, new RemotePlayerClient(information.length == MAX_REMOTE_ARGS ? information[2] : DEFAULT_IP, cbs[p.ordinal()]));
		} catch (IOException e) {
			error("Un problème est survenu lors du connexion avec le server distant");
		}
	}

	/*
	 * Displays an error message if a player was incorrectly specified
	 */
	private void error(String message) {
		System.err.println("Erreur : " + message + ".");
		System.exit(1);
	}

	/*
	 * Displays a help message on how to correctly execute the program if the number
	 * of arguments is invalid
	 */
	private void invalidNumberOfArguments() {
		System.err.println("Utilisation : java ch.epfl.javass.LocalMain <p1>...<p4> [<seed>] où :"
				+ "\n<pn> spécifie le joueur player n, ainsi :"
				+ "\n\th[:<name>] un joueur humain nommé <name>"
				+ "\n\ts[:<name>][:<iterations>] un joueur simulé nommé <name> utilisant <iterations> nombre d'itérations"
				+ "\n\tr[:<name>][:<serveur_ip>] un joueur distant nommé <name> avec l'addresse IP <serveur_ip>"
				+ "\n[<seed>] spécifie le noyaux à utiliser pour générer le hasard dans le jeu."
				+ "\nLes éléments entre crochets [] sont facultatifs. Les valeurs par défaut seront utilisées lorsqu'elles ne sont pas spécifiées.");
		System.exit(1);
	}
}