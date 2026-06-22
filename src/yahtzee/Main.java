package yahtzee;

import yahtzee.ui.GameWindow;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point. Before the game window opens, this class asks
 * how many people are playing and what each of their names is, so the
 * same game supports anywhere from a single solo player up to a full
 * table of players sharing the keyboard and mouse in turn.
 */
public class Main {

	private static final int MIN_PLAYERS = 1;
	private static final int MAX_PLAYERS = 6;
	private static final int DEFAULT_PLAYERS = 2;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
				// Falls back to Swing's default look and feel when the
				// platform look and feel isn't available.
			}

			Integer playerCount = promptForPlayerCount();
			if (playerCount == null) {
				return; // user closed the dialog without confirming
			}

			List<String> names = promptForPlayerNames(playerCount);
			if (names == null) {
				return; // user closed the dialog without confirming
			}

			GameWindow window = new GameWindow(names);
			window.setVisible(true);
		});
	}

	/**
	 * Shows a small dialog with a spinner so the user can pick how many
	 * people are playing this game, anywhere from {@value #MIN_PLAYERS}
	 * to {@value #MAX_PLAYERS}. Returns null if the dialog is dismissed
	 * without confirming.
	 */
	private static Integer promptForPlayerCount() {
		SpinnerNumberModel model = new SpinnerNumberModel(DEFAULT_PLAYERS, MIN_PLAYERS, MAX_PLAYERS, 1);
		JSpinner spinner = new JSpinner(model);

		JPanel panel = new JPanel();
		panel.add(new JLabel("Number of players:"));
		panel.add(spinner);

		int choice = JOptionPane.showConfirmDialog(null, panel, "Yahtzee",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		return (Integer) spinner.getValue();
	}

	/**
	 * Shows a single dialog containing one labeled name field per player,
	 * pre-filled with a default name. Blank fields fall back to their
	 * default name. Returns null if the dialog is dismissed without
	 * confirming.
	 */
	private static List<String> promptForPlayerNames(int playerCount) {
		JTextField[] fields = new JTextField[playerCount];
		JPanel panel = new JPanel(new GridLayout(playerCount, 2, 6, 6));
		for (int i = 0; i < playerCount; i++) {
			String defaultName = "Player " + (i + 1);
			panel.add(new JLabel(defaultName + " name:"));
			fields[i] = new JTextField(defaultName);
			panel.add(fields[i]);
		}

		int choice = JOptionPane.showConfirmDialog(null, panel, "Yahtzee",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}

		List<String> names = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) {
			String text = fields[i].getText().trim();
			names.add(text.isEmpty() ? "Player " + (i + 1) : text);
		}
		return names;
	}
}
