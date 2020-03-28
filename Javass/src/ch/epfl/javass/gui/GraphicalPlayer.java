package ch.epfl.javass.gui;

import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.unmodifiableObservableMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.ParallelMctsPlayer;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Graphical interface of a human player
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public class GraphicalPlayer {
	private final static int TRUMP_IMAGE_SIDE_LENGTH = 101;
	private final static int TRICK_CARD_IMAGE_WIDTH = 120, TRICK_CARD_IMAGE_HEIGHT = 180;
	private final static int HAND_CARD_IMAGE_WIDTH = 80, HAND_CARD_IMAGE_HEIGHT = 120;
	private final static int GAUSSIAN_BLUR_RADIUS = 4;
	private final static float PLAYABLE_OPACITY = 1f, UNPLAYABLE_OPACITY = 0.2f;
	private final static int[][] TRICK_GRIDPANE_POSITIONS = { { 1, 2, 1, 1 }, { 2, 0, 1, 3 }, { 1, 0, 1, 1 },
			{ 0, 0, 1, 3 } };

	// Small and big sizes are 160 and 240 respectively
	private static final ObservableMap<Card, Image> smallCardImages = unmodifiableObservableMap(cardImagesMap(160));
	private static final ObservableMap<Card, Image> bigCardImages = unmodifiableObservableMap(cardImagesMap(240));
	private static final ObservableMap<Color, Image> trumpImages = unmodifiableObservableMap(trumpImagesMap());

	private final StackPane stackPane;
	private final PlayerId ownId;
	private final ParallelMctsPlayer pmcts;
	private final Map<PlayerId, String> playerNames;

	/**
	 * Construct a GraphicalPlayer
	 * 
	 * @param ownId       : the id of the human player using the graphical interface
	 * @param playerNames : the names of all the players
	 * @param hb          : the bean associated to the player's hand
	 * @param sb          : the bean associated to the score
	 * @param tb          : the bean associated to the trick
	 * @param queue       : queue communicating with the player which card to play
	 */
	public GraphicalPlayer(PlayerId ownId, Map<PlayerId, String> playerNames, HandBean hb, ScoreBean sb, TrickBean tb,
			ChatBean cb, ArrayBlockingQueue<Card> cardToPlayQueue, ArrayBlockingQueue<Color> trumpQueue,
			ArrayBlockingQueue<TurnState> turnStateQueue) {
		this.ownId = ownId;
		this.pmcts = new ParallelMctsPlayer(ownId, System.nanoTime(), 100_000);
		this.playerNames = new EnumMap<>(playerNames);
		Pane[] hintAndHand = createHintAndHandPanes(tb, turnStateQueue, hb, cardToPlayQueue, cb);

		// The game screen containing the player's hand, the score, and the trick
		BorderPane main = new BorderPane(createTrickPane(ownId, playerNames, tb, trumpQueue),
				createScorePane(ownId, playerNames, sb), createChat(cb), hintAndHand[0], hintAndHand[1]);

		// Stack of the game screen and two victory panes, only one is shown at a time
		stackPane = new StackPane(main, createVictoryPane(ownId, playerNames, sb, TeamId.TEAM_1),
				createVictoryPane(ownId, playerNames, sb, TeamId.TEAM_2));
	}

	/**
	 * Creates the unique window of the interface
	 * 
	 * @return the window of the interface
	 */
	public Stage createStage() {
		Stage s = new Stage();
		s.setScene(new Scene(stackPane));
		s.setTitle("Javass - " + playerNames.get(ownId));
		return s;
	}

	private BorderPane createChat(ChatBean cb) {
		Text text = new Text();
		text.textProperty().bind(cb.getChatProperty());
		text.setStyle("-fx-alignment: center; -fx-font: 14 Optima; -fx-background-color : green;");
		text.setWrappingWidth(250);

		ScrollPane sp = new ScrollPane(text);
		text.textProperty().addListener((event) -> {
			sp.setVvalue(1D);
		});

		TextField tf = new TextField();
		tf.setPromptText("Entrer un message...");
		tf.setOnKeyReleased((key) -> {
			if (key.getCode().equals(KeyCode.ENTER)) {
				chatHandler(tf, cb);
			}
		});

		Button b = new Button("  Envoyer  ");
		b.pressedProperty().addListener((event) -> {
			chatHandler(tf, cb);
		});

		BorderPane bp = new BorderPane(sp, null, null, new HBox(tf, b), null);
		bp.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
		return bp;
	}

	private void chatHandler(TextField tf, ChatBean cb) {
		String s = tf.getText();
		if (!s.isEmpty()) {
			cb.addMessage(playerNames.get(ownId) + " : " + s);
			cb.setSent(playerNames.get(ownId) + " : " + s);
			tf.clear();
			if (s.contains("/blague")) {
				new Thread(() -> {
					String[] joke = new String[2];
					boolean success = false;
					while (!success) {
						try {
							joke = joke();
							success = true;
						} catch (StringIndexOutOfBoundsException e) {
						}
					}
					cb.addMessage(joke[0]);
					cb.setSent(joke[0]);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new IllegalStateException();
					}
					cb.addMessage(joke[1]);
					cb.setSent(joke[1]);
				}).start();
			}
		}
	}

	private String[] joke() {
		boolean itsNext = false;
		String line = "";
		try {
			URL url = new URL("https://random-ize.com/bad-jokes/");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				str = in.readLine().toString();
				if (itsNext) {
					line = str;
					itsNext = false;
				}
				if (str.contains("Give Me Another!")) {
					itsNext = true;
				}
			}
			in.close();
		} catch (Exception e) {
		}

		line = line.substring(line.indexOf("+2\">") + 4);
		int i = line.indexOf("<br>");
		int j = line.indexOf("</font");
		String[] joke = { "Bot : " + line.substring(0, i),
				"Bot : " + line.substring(i + "<br><br>".length(), j) + " ;p" };
		return joke;
	}

	private Pane[] createHintAndHandPanes(TrickBean tb, ArrayBlockingQueue<TurnState> turnStateQueue, HandBean hb,
			ArrayBlockingQueue<Card> cardToPlayQueue, ChatBean cb) {

		Timeline t = new Timeline();
		IntegerProperty time = new SimpleIntegerProperty(Jass.MAX_TIME_TO_PlAY);

		Label timer = new Label();
		timer.textProperty().bind(Bindings.convert(time));
		timer.setStyle("-fx-font-size : 30;");
		timer.setTextFill(javafx.scene.paint.Color.RED);

		BooleanBinding startTimer = Bindings.createBooleanBinding(() -> {
			return !hb.playableCards().isEmpty();
		}, hb.playableCards());

		startTimer.addListener((event) -> {
			time.set(Jass.MAX_TIME_TO_PlAY);
			t.getKeyFrames().add(new KeyFrame(Duration.seconds(Jass.MAX_TIME_TO_PlAY), new KeyValue(time, 0)));
			t.playFromStart();
		});

		StackPane[] sps = new StackPane[Jass.HAND_SIZE];
		Rectangle[] halos = new Rectangle[Jass.HAND_SIZE];

		for (int i = 0; i < sps.length; ++i) {
			ImageView iv = new ImageView();
			iv.imageProperty().bind(Bindings.valueAt(smallCardImages, Bindings.valueAt(hb.hand(), i)));
			iv.setFitWidth(HAND_CARD_IMAGE_WIDTH);
			iv.setFitHeight(HAND_CARD_IMAGE_HEIGHT);

			Rectangle r = new Rectangle(HAND_CARD_IMAGE_WIDTH, HAND_CARD_IMAGE_HEIGHT);
			r.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent;"
					+ " -fx-stroke: blue; -fx-stroke-width: 5; -fx-opacity: 0.5;");
			r.setEffect(new GaussianBlur(GAUSSIAN_BLUR_RADIUS));
			r.setVisible(false);

			int j = i;
			BooleanBinding isPlayable = Bindings.createBooleanBinding(() -> {
				return hb.playableCards().contains(hb.hand().get(j));
			}, hb.playableCards(), hb.hand());

			iv.opacityProperty().bind(Bindings.when(isPlayable).then(PLAYABLE_OPACITY).otherwise(UNPLAYABLE_OPACITY));
			iv.disableProperty().bind(isPlayable.not());
			iv.setOnMouseClicked((event) -> {
				cardToPlayQueue.add(hb.hand().get(j));
				for (Rectangle rec : halos) {
					rec.setVisible(false);
				}
				t.stop();
			});

			halos[i] = r;
			sps[i] = new StackPane(r, iv);
		}

		Text hintText = new Text(
				"\n\nAttention ! Je joue seulement aussi bien que mes créateurs ! (c.à.d pas très bien...)\n\n");
		hintText.setVisible(false);
		hintText.setWrappingWidth(120);
		hintText.setTextAlignment(TextAlignment.CENTER);
		hintText.visibleProperty().bind(startTimer);

		Button hint = new Button("   Indice du\njoueur simulé");
		hint.visibleProperty().bind(startTimer);
		hint.setAlignment(Pos.CENTER);
		hint.setOnMouseClicked((event) -> {
			TurnState state = turnStateQueue.peek();
			if (state != null) {
				ArrayList<Card> hand = new ArrayList<>();
				for (Card c : hb.hand()) {
					if (c != null)
						hand.add(c);
				}

				File f = new File("sounds/hint.wav");
				try {
					AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
					Clip clip = AudioSystem.getClip();
					clip.open(audioIn);
					new Thread(() -> {
						halos[hb.hand().indexOf(pmcts.cardToPlay(state, CardSet.of(hand)))].setVisible(true);
						cb.setSent(playerNames.get(ownId) + " vient d'utiliser un indice!");
						clip.start();
					}).start();
					audioIn.close();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});

		time.addListener((event) -> {
			if (time.get() == 0 && hb.playableCards().size() != 0) {
				ArrayList<Card> playableCards = new ArrayList<>();
				for (Card c : hb.playableCards()) {
					if (c != null) {
						playableCards.add(c);
					}
				}
				cardToPlayQueue.add(playableCards.get((new Random(System.nanoTime())).nextInt(playableCards.size())));
				for (Rectangle rec : halos) {
					rec.setVisible(false);
				}

				File f = new File("sounds/time_out.wav");
				try {
					AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
					Clip clip = AudioSystem.getClip();
					clip.open(audioIn);
					clip.start();
					audioIn.close();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});

		Text youHave = new Text("Vous avez");
		Text toPlayACard = new Text("secondes pour joueur\n une carte. Sinon,\nune carte aléatoire\nsera jouée !");
		toPlayACard.setTextAlignment(TextAlignment.CENTER);
		GridPane gp = new GridPane();
		gp.add(youHave, 0, 0);
		gp.add(timer, 0, 1, 1, 2);
		gp.add(toPlayACard, 0, 3);
		gp.visibleProperty().bind(startTimer);
		GridPane.setHalignment(youHave, HPos.CENTER);
		GridPane.setHalignment(timer, HPos.CENTER);

		HBox hBox = new HBox(sps);
		hBox.setStyle("-fx-background-color: lightgray; -fx-spacing: 5px; -fx-padding: 5px;");
		VBox vBox = new VBox(hint, hintText, gp);
		vBox.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");

		Pane[] toReturn = { hBox, vBox };
		return toReturn;
	}

	private BorderPane createVictoryPane(PlayerId ownId, Map<PlayerId, String> playerNames, ScoreBean sb, TeamId team) {
		Text text = new Text();
		text.textProperty()
				.bind(Bindings.format("%s ont gagné avec %d points contre %d.",
						teamPlayerNames(ownId, team, playerNames), sb.totalPointsProperty(team),
						sb.totalPointsProperty(team.other())));

		BorderPane bp = new BorderPane(text);
		bp.setStyle("-fx-font: 16 Optima; -fx-background-color: white;");
		bp.visibleProperty().bind(sb.winningTeamProperty().isEqualTo(team));
		return bp;
	}

	private GridPane createScorePane(PlayerId ownId, Map<PlayerId, String> playerNames, ScoreBean sb) {
		GridPane gp = new GridPane();

		for (TeamId team : TeamId.ALL) {
			Text names = new Text(teamPlayerNames(ownId, team, playerNames) + " : ");

			Text turnPoints = new Text();
			turnPoints.textProperty().bind(Bindings.convert(sb.turnPointsProperty(team)));

			IntegerProperty oldTurnPoints = new SimpleIntegerProperty();

			Text trickPoints = new Text();
			sb.turnPointsProperty(team).addListener((observable, oldValue, newValue) -> {
				trickPoints.textProperty()
						.set(" (+" + Integer.toString(newValue.intValue() - oldValue.intValue()) + ")");
				oldTurnPoints.set(oldValue.intValue());
			});

			sb.turnTricksProperty(team).addListener((ob, ov, nv) -> {
				if (oldTurnPoints.get() == sb.turnPointsProperty(team).get()) {
					trickPoints.textProperty().set("(+0)");
				}
			});

			sb.gamePointsProperty(team).addListener((observable, oldValue, newValue) -> {
				trickPoints.textProperty().set("");
				oldTurnPoints.set(0);
			});

			Text total = new Text(" / Total : ");

			Text gamePoints = new Text();
			gamePoints.textProperty().bind(Bindings.format("%s\t", Bindings.convert(sb.gamePointsProperty(team))));

			ProgressBar pb = new ProgressBar(0.0);
			pb.progressProperty().bind(Bindings.divide(sb.totalPointsProperty(team), (double) Jass.WINNING_POINTS));

			gp.add(names, 0, team.ordinal());
			gp.add(turnPoints, 1, team.ordinal());
			gp.add(trickPoints, 2, team.ordinal());
			gp.add(total, 3, team.ordinal());
			gp.add(gamePoints, 4, team.ordinal());
			gp.add(pb, 5, team.ordinal());

			GridPane.setHalignment(names, HPos.RIGHT);
			GridPane.setHalignment(turnPoints, HPos.RIGHT);
			GridPane.setHalignment(trickPoints, HPos.LEFT);
			GridPane.setHalignment(total, HPos.LEFT);
			GridPane.setHalignment(gamePoints, HPos.RIGHT);
		}
		gp.setStyle("-fx-font: 16 Optima; -fx-background-color: lightgray; -fx-padding: 5px; -fx-alignment: center;");
		return gp;
	}

	private GridPane createTrickPane(PlayerId ownId, Map<PlayerId, String> playerNames, TrickBean tb,
			ArrayBlockingQueue<Color> trumpQueue) {
		GridPane gp = new GridPane();
		int own = ownId.ordinal();

		for (int i = 0; i < PlayerId.COUNT; ++i) {
			PlayerId player = PlayerId.ALL.get((own + i) % PlayerId.COUNT);
			Text text = new Text(playerNames.get(player));

			ImageView iv = new ImageView();
			iv.imageProperty().bind(Bindings.valueAt(bigCardImages, Bindings.valueAt(tb.trick(), player)));
			iv.setFitWidth(TRICK_CARD_IMAGE_WIDTH);
			iv.setFitHeight(TRICK_CARD_IMAGE_HEIGHT);

			Rectangle r = new Rectangle(TRICK_CARD_IMAGE_WIDTH, TRICK_CARD_IMAGE_HEIGHT);
			r.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent;"
					+ " -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
			r.setEffect(new GaussianBlur(4));
			r.visibleProperty().bind(tb.winningPlayerProperty().isEqualTo(player));

			StackPane sp = new StackPane(r, iv);
			VBox v = i == 0 ? new VBox(sp, text) : new VBox(text, sp);
			v.setStyle("-fx-alignment: center; -fx-font: 14 Optima;");
			gp.add(v, TRICK_GRIDPANE_POSITIONS[i][0], TRICK_GRIDPANE_POSITIONS[i][1], TRICK_GRIDPANE_POSITIONS[i][2],
					TRICK_GRIDPANE_POSITIONS[i][3]);
		}

		ImageView trump = new ImageView();
		trump.imageProperty().bind(Bindings.valueAt(trumpImages, tb.trumpProperty()));
		trump.setFitWidth(TRUMP_IMAGE_SIDE_LENGTH);
		trump.setFitHeight(TRUMP_IMAGE_SIDE_LENGTH);

		Text text = new Text("Choosing trump...");
		BorderPane choosingTrump = new BorderPane(text);
		choosingTrump.setStyle("-fx-font: 16 Optima; -fx-background-color: whitesmoke");

		GridPane trumpChoice = new GridPane();
		trumpChoice.setStyle("-fx-alignment : center; -fx-background-color: whitesmoke");
		int[][] choicePositions = { { 0, 0 }, { 0, 2 }, { 2, 0 }, { 2, 2 } };
		for (Color c : Color.ALL) {
			ImageView image = new ImageView();
			image.imageProperty().set(trumpImages.get(c));
			image.setFitWidth(33);
			image.setFitHeight(33);

			image.setOnMouseClicked((event) -> {
				trumpQueue.addAll(Arrays.asList(c, c));
			});
			trumpChoice.add(image, choicePositions[c.ordinal()][0], choicePositions[c.ordinal()][1]);
		}
		ImageView pass = new ImageView();
		pass.imageProperty().set(new Image("/pass.png"));
		pass.setFitWidth(33);
		pass.setFitHeight(33);
		pass.visibleProperty().bind(tb.choicePassingProperty());

		pass.setOnMouseClicked((event) -> {
			trumpQueue.addAll(Arrays.asList(Color.HEART, Color.SPADE));
			choosingTrump.setVisible(true);
			trumpChoice.setVisible(false);
		});
		trumpChoice.add(pass, 1, 1);

		StackPane trumpSection = new StackPane(new BorderPane(trump), trumpChoice, choosingTrump);

		tb.trumpChooserProperty().addListener((event) -> {
			if (tb.trumpChooserProperty().get() == null) {
				trumpChoice.setVisible(false);
				choosingTrump.setVisible(false);
			} else {
				if (ownId.equals(tb.trumpChooserProperty().get())) {
					trumpChoice.setVisible(true);
					choosingTrump.setVisible(false);
				} else {
					choosingTrump.setVisible(true);
					trumpChoice.setVisible(false);
					trumpQueue.addAll(Arrays.asList(Color.HEART, Color.SPADE));
				}
			}
		});

		gp.add(trumpSection, 1, 1);
		gp.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
		return gp;
	}

	private static ObservableMap<Card, Image> cardImagesMap(int size) {
		ObservableMap<Card, Image> map = observableHashMap();
		for (Color c : Color.ALL) {
			for (Rank r : Rank.ALL) {
				Card card = Card.of(c, r);
				map.put(card, new Image("/card_" + c.ordinal() + "_" + r.ordinal() + "_" + size + ".png"));
			}
		}
		return map;
	}

	private static ObservableMap<Color, Image> trumpImagesMap() {
		ObservableMap<Color, Image> map = observableHashMap();
		for (Color c : Color.ALL) {
			map.put(c, new Image("/trump_" + c.ordinal() + ".png"));
		}
		return map;
	}

	/*
	 * Creates a string describing the names of the players in a given team
	 * (ascending order beginning from ownId)
	 */
	private String teamPlayerNames(PlayerId ownId, TeamId t, Map<PlayerId, String> playerNames) {
		if (t.equals(ownId.team())) {
			return playerNames.get(ownId) + " et "
					+ playerNames.get(PlayerId.ALL.get((ownId.ordinal() + TeamId.COUNT) % PlayerId.COUNT));
		} else {
			int o = ownId.ordinal();
			return playerNames.get(PlayerId.ALL.get((o + 1) % PlayerId.COUNT)) + " et "
					+ playerNames.get(PlayerId.ALL.get((o + 1 + TeamId.COUNT) % PlayerId.COUNT));
		}
	}
}