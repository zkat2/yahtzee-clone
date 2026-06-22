package yahtzee.ui;

import yahtzee.Dice;
import yahtzee.Game;
import yahtzee.Player;
import yahtzee.ScoreCategory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Top-level window for a game of Yahtzee. Supports any number of players:
 * the dice and the active player's scorecard occupy the center of the
 * window, while a running scoreboard across the top shows every player's
 * grand total with the player whose turn it is highlighted.
 */
public class GameWindow extends JFrame {

	private Game game;
	private JPanel diceRow;
	private DiePanel[] diePanels;
	private ScoreCardPanel scoreCardPanel;
	private JLabel statusLabel;
	private JLabel roundLabel;
	private JButton rollButton;
	private JPanel scoreboardPanel;
	private JLabel[] playerScoreLabels;

	/** Starts a new game with one {@link Player} created per name, in turn order. */
	public GameWindow(List<String> playerNames) {
		super("Yahtzee");
		this.game = new Game(playerNames);
		buildUi();
		refreshAll();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
	}

	private void buildUi() {
		setLayout(new BorderLayout(10, 10));

		add(buildTopPanel(), BorderLayout.NORTH);
		add(buildDicePanel(), BorderLayout.CENTER);

		scoreCardPanel = new ScoreCardPanel(game, this::handleScore);
		scoreCardPanel.setBorder(BorderFactory.createTitledBorder("Scorecard"));
		add(scoreCardPanel, BorderLayout.EAST);

		((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
	}

	private JPanel buildTopPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 6));

		JPanel headerRow = new JPanel(new BorderLayout());
		roundLabel = new JLabel();
		roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 16f));
		headerRow.add(roundLabel, BorderLayout.WEST);

		statusLabel = new JLabel("", SwingConstants.RIGHT);
		headerRow.add(statusLabel, BorderLayout.EAST);
		panel.add(headerRow, BorderLayout.NORTH);

		panel.add(buildScoreboardPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/** Builds one label per player showing their name and running grand total. */
	private JPanel buildScoreboardPanel() {
		scoreboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
		List<Player> players = game.getPlayers();
		playerScoreLabels = new JLabel[players.size()];
		for (int i = 0; i < players.size(); i++) {
			playerScoreLabels[i] = new JLabel();
			playerScoreLabels[i].setFont(playerScoreLabels[i].getFont().deriveFont(13f));
			scoreboardPanel.add(playerScoreLabels[i]);
		}
		return scoreboardPanel;
	}

	private JPanel buildDicePanel() {
		JPanel container = new JPanel(new BorderLayout(0, 12));

		diceRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
		Dice[] dice = game.getDice();
		diePanels = new DiePanel[dice.length];
		for (int i = 0; i < dice.length; i++) {
			final int index = i;
			diePanels[i] = new DiePanel(dice[i], () -> handleHoldToggle(index));
			diceRow.add(diePanels[i]);
		}
		container.add(diceRow, BorderLayout.CENTER);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		rollButton = new JButton("Roll Dice");
		rollButton.addActionListener(e -> handleRoll());
		controls.add(rollButton);
		container.add(controls, BorderLayout.SOUTH);

		JLabel hint = new JLabel("Click a die to hold/release it between rolls.", SwingConstants.CENTER);
		hint.setForeground(Color.GRAY);
		container.add(hint, BorderLayout.NORTH);

		return container;
	}

	private void handleRoll() {
		game.rollDice();
		refreshAll();
	}

	private void handleHoldToggle(int index) {
		game.toggleHold(index);
		refreshAll();
	}

	private void handleScore(ScoreCategory category) {
		boolean scored = game.scoreCategory(category);
		if (!scored) {
			return;
		}
		refreshAll();
		if (game.isGameOver()) {
			showGameOver();
		}
	}

	private void refreshAll() {
		for (DiePanel diePanel : diePanels) {
			diePanel.repaint();
		}
		scoreCardPanel.refresh();
		refreshScoreboard();

		if (game.isGameOver()) {
			roundLabel.setText("Game over");
			rollButton.setEnabled(false);
			scoreCardPanel.setBorder(BorderFactory.createTitledBorder("Final Scorecard"));
		} else {
			roundLabel.setText(String.format("Round %d / %d \u2014 %s",
					game.getRound(), Game.TOTAL_ROUNDS, game.getCurrentPlayer().getName()));
			rollButton.setEnabled(game.canRoll());
			scoreCardPanel.setBorder(BorderFactory.createTitledBorder(
					"Scorecard \u2014 " + game.getCurrentPlayer().getName()));
		}

		if (game.isGameOver()) {
			statusLabel.setText("");
		} else if (!game.hasRolledThisTurn()) {
			statusLabel.setText("Roll to begin your turn (" + game.getRollsLeft() + " rolls left)");
		} else {
			statusLabel.setText(game.getRollsLeft() + " rolls left \u2014 pick a category to score");
		}
	}

	/** Updates every player's running grand total, bolding whichever player's turn is active. */
	private void refreshScoreboard() {
		List<Player> players = game.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			boolean isCurrent = !game.isGameOver() && player == game.getCurrentPlayer();
			JLabel label = playerScoreLabels[i];
			label.setText(player.getName() + ": " + player.getScoreCard().grandTotal());
			label.setFont(label.getFont().deriveFont(isCurrent ? Font.BOLD : Font.PLAIN));
			label.setForeground(isCurrent ? new Color(0, 90, 170) : Color.DARK_GRAY);
		}
	}

	private void showGameOver() {
		List<Player> players = game.getPlayers();
		int bestTotal = players.stream().mapToInt(p -> p.getScoreCard().grandTotal()).max().orElse(0);
		List<Player> winners = players.stream()
				.filter(p -> p.getScoreCard().grandTotal() == bestTotal)
				.collect(Collectors.toList());

		StringBuilder message = new StringBuilder();
		for (Player p : players) {
			message.append(String.format("%s: %d%n", p.getName(), p.getScoreCard().grandTotal()));
		}
		message.append(System.lineSeparator());
		if (winners.size() > 1) {
			String names = winners.stream().map(Player::getName).collect(Collectors.joining(" and "));
			message.append(names).append(" tie for the win!");
		} else {
			message.append(winners.get(0).getName()).append(" wins!");
		}

		int choice = JOptionPane.showOptionDialog(this,
				message.toString(),
				"Game Over",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				new String[] { "New Game", "Close" },
				"New Game");
		if (choice == 0) {
			startNewGame();
		}
	}

	/** Starts a fresh game with the same players, in the same order, and rebuilds the dice and scorecard panels for it. */
	private void startNewGame() {
		List<String> names = new ArrayList<>();
		for (Player p : game.getPlayers()) {
			names.add(p.getName());
		}
		this.game = new Game(names);

		Dice[] dice = game.getDice();
		diceRow.removeAll();
		for (int i = 0; i < dice.length; i++) {
			final int index = i;
			diePanels[i] = new DiePanel(dice[i], () -> handleHoldToggle(index));
			diceRow.add(diePanels[i]);
		}
		diceRow.revalidate();

		remove(scoreCardPanel);
		scoreCardPanel = new ScoreCardPanel(game, this::handleScore);
		scoreCardPanel.setBorder(BorderFactory.createTitledBorder("Scorecard"));
		add(scoreCardPanel, BorderLayout.EAST);
		revalidate();

		refreshAll();
	}
}
