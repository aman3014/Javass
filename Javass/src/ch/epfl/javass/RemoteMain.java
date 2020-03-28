package ch.epfl.javass;

import ch.epfl.javass.gui.ChatBean;
import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Launches a remote player server
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class RemoteMain extends Application {

	/**
	 * Launches the remote player server and its graphical interface
	 * 
	 * @param args : arguments passed into the program
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ChatBean cb = new ChatBean();
		Thread serverThread = new Thread(() -> {
			(new RemotePlayerServer(new GraphicalPlayerAdapter(cb), cb)).run();
		});

		// Ensures the thread is correctly terminated
		serverThread.setDaemon(true);
		// Start the thread
		serverThread.start();

		System.out.println("La partie commencera à la connexion du client...");
	}
}