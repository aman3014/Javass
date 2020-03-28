package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import ch.epfl.javass.gui.ChatBean;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

/**
 * Represents a player server who communicates the game information with a
 * client
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class RemotePlayerServer {
	private final Player localPlayer;
	private final ChatBean cb;

	/**
	 * Constructs a remote player server mimicking the behavior of a given local
	 * player
	 * 
	 * @param localPlayer : the local player
	 * @param cb          : the chat bean associated with the local player
	 */
	public RemotePlayerServer(Player localPlayer, ChatBean cb) {
		this.localPlayer = localPlayer;
		this.cb = cb;
	}

	/**
	 * Runs the server allowing it to wait for connection from a client
	 */
	public void run() {
		new Thread(() -> {
			try (ServerSocket cs0 = new ServerSocket(Jass.CHAT_PORT);
					Socket cs = cs0.accept();
					BufferedReader cr = new BufferedReader(new InputStreamReader(cs.getInputStream(), US_ASCII));
					BufferedWriter cw = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), US_ASCII))) {

				cb.getSent().addListener((c, o, n) -> {
					if (!n.isEmpty()) {
						try {
							cw.write(StringSerializer.serializeString(n) + '\n');
							cw.flush();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
						cb.setSent("");
					}
				});

				while (true) {
					String line = cr.readLine();
					if (!line.isEmpty()) {
						cb.addMessage(StringSerializer.deserializeString(line));
						File f = new File("sounds/chat_notification.wav");
						try {
							AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
							Clip clip = AudioSystem.getClip();
							clip.open(audioIn);
							clip.start();
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).start();

		try (ServerSocket s0 = new ServerSocket(Jass.GAME_PORT);
				Socket s = s0.accept();
				BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), US_ASCII));
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII))) {
			while (true) {
				String line = r.readLine();
				// Any jass command can be used to get the command string length
				String code = line.substring(0, JassCommand.PLRS.name().length());
				String information = line.substring(JassCommand.PLRS.name().length() + 1);
				switch (JassCommand.valueOf(code)) {
				case PLRS:
					setPlayers(information);
					break;
				case TRMP:
					localPlayer.setTrump(Card.Color.ALL.get(StringSerializer.deserializeInt(information)));
					break;
				case HAND:
					localPlayer.updateHand(CardSet.ofPacked(StringSerializer.deserializeLong(information)));
					break;
				case TRCK:
					localPlayer.updateTrick(Trick.ofPacked(StringSerializer.deserializeInt(information)));
					break;
				case CARD:
					w.write(cardToPlaySerialized(information) + '\n');
					w.flush();
					break;
				case TRCH:
					w.write(trumpSerialized(information) + '\n');
					w.flush();
					break;
				case SCOR:
					localPlayer.updateScore(Score.ofPacked(StringSerializer.deserializeLong(information)));
					break;
				case WINR:
					localPlayer.setWinningTeam(TeamId.ALL.get(StringSerializer.deserializeInt(information)));
					return;
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String trumpSerialized(String information) {
		PlayerId chooser = PlayerId.ALL.get(StringSerializer.deserializeInt(information.substring(0, 1)));
		boolean canPass = StringSerializer.deserializeInt(information.substring(2, 3)) == 1;
		CardSet hand = CardSet.ofPacked(StringSerializer.deserializeLong(information.substring(4)));
		Color trump = localPlayer.chooseTrump(chooser, hand, canPass);
		return trump == null ? StringSerializer.serializeInt(-1) : StringSerializer.serializeInt(trump.ordinal());
	}

	/*
	 * Splits and decodes the received information about the current hand and turn
	 * state and asks the local player which card to play
	 */
	private String cardToPlaySerialized(String information) {
		String[] turnStateInformation = StringSerializer.split(',', information);

		int spaceIndex = turnStateInformation[2].indexOf(' ');
		String handString = turnStateInformation[2].substring(spaceIndex + 1);
		turnStateInformation[2] = turnStateInformation[2].substring(0, spaceIndex);

		CardSet hand = CardSet.ofPacked(StringSerializer.deserializeLong(handString));

		TurnState state = TurnState.ofPackedComponents(StringSerializer.deserializeLong(turnStateInformation[0]),
				StringSerializer.deserializeLong(turnStateInformation[1]),
				StringSerializer.deserializeInt(turnStateInformation[2]));

		return StringSerializer.serializeInt(localPlayer.cardToPlay(state, hand).packed());
	}

	/*
	 * Informs the local player of the player names
	 */
	private void setPlayers(String information) {
		PlayerId ownId = PlayerId.ALL.get(StringSerializer.deserializeInt(information.substring(0, 1)));

		Map<PlayerId, String> playerNames = new HashMap<>();
		String[] names = StringSerializer.split(',', information.substring(2));

		for (int i = 0; i < PlayerId.COUNT; ++i) {
			playerNames.put(PlayerId.ALL.get(i), StringSerializer.deserializeString(names[i]));
		}

		localPlayer.setPlayers(ownId, new EnumMap<>(playerNames));
	}
}