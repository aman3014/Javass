package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
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
 * Represents a player client who communicates the game information with a
 * remote server
 * 
 * @author Aman Bansal (297535)
 * @author Julian Blackwell (289803)
 */
public final class RemotePlayerClient implements Player, AutoCloseable {

	private final Socket s;
	private final BufferedReader r;
	private final BufferedWriter w;

	private Socket cs;
	private BufferedReader cr;
	private BufferedWriter cw;

	/**
	 * Constructs a player client. Connects to the remote server
	 * 
	 * @param host : the address of the remote server
	 * @param cb   : the chat bean associated with this player
	 * @throws IOException if an error occurs during the connection
	 */
	public RemotePlayerClient(String host, ChatBean cb) throws IOException {
		s = new Socket(host, Jass.GAME_PORT);
		r = new BufferedReader(new InputStreamReader(s.getInputStream(), US_ASCII));
		w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII));

		cs = new Socket(host, Jass.CHAT_PORT);
		cr = new BufferedReader(new InputStreamReader(cs.getInputStream(), US_ASCII));
		cw = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), US_ASCII));

		new Thread(() -> {
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
				try {
					String msg = cr.readLine();
					if (!msg.isEmpty()) {
						cb.setReceived(StringSerializer.deserializeString(msg));
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
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}).start();
	}

	@Override
	public void close() throws Exception {
		r.close();
		w.close();
		s.close();
		
		cr.close();
		cw.close();
		cs.close();
	}

	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		final String turnStateInformation = StringSerializer.combine(',',
				StringSerializer.serializeLong(state.packedScore()),
				StringSerializer.serializeLong(state.packedUnplayedCards()),
				StringSerializer.serializeInt(state.packedTrick()));

		send(JassCommand.CARD, turnStateInformation + " " + StringSerializer.serializeLong(hand.packed()));
		try {
			return Card.ofPacked(StringSerializer.deserializeInt(r.readLine()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		String[] names = new String[PlayerId.COUNT];
		for (PlayerId p : PlayerId.ALL) {
			names[p.ordinal()] = StringSerializer.serializeString(playerNames.get(p));
		}
		send(JassCommand.PLRS,
				StringSerializer.serializeInt(ownId.ordinal()) + " " + StringSerializer.combine(',', names));
	}

	@Override
	public void updateHand(CardSet newHand) {
		send(JassCommand.HAND, StringSerializer.serializeLong(newHand.packed()));
	}

	@Override
	public void setTrump(Color trump) {
		send(JassCommand.TRMP, StringSerializer.serializeInt(trump.ordinal()));
	}

	@Override
	public void updateTrick(Trick newTrick) {
		send(JassCommand.TRCK, StringSerializer.serializeInt(newTrick.packed()));
	}

	@Override
	public void updateScore(Score score) {
		send(JassCommand.SCOR, StringSerializer.serializeLong(score.packed()));
	}

	@Override
	public void setWinningTeam(TeamId winningTeam) {
		send(JassCommand.WINR, StringSerializer.serializeInt(winningTeam.ordinal()));
	}

	@Override
	public Color chooseTrump(PlayerId chooser, CardSet hand, boolean canPass) {
		send(JassCommand.TRCH, StringSerializer.serializeInt(chooser.ordinal()) + " "
				+ StringSerializer.serializeInt(canPass ? 1 : 0) + " " + StringSerializer.serializeLong(hand.packed()));
		try {
			int answer = StringSerializer.deserializeInt(r.readLine());
			return answer == -1 ? null : Color.ALL.get(answer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void send(JassCommand command, String s) {
		try {
			w.write(command.name() + " " + s + '\n');
			w.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}