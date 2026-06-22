package yahtzee.ui;

import yahtzee.Game;
import yahtzee.ScoreCard;
import yahtzee.ScoreCategory;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Renders the current player's scorecard: one row per category showing
 * either the locked-in score (if used) or a live preview of what the
 * current dice would score there, with a button to commit it.
 */
public class ScoreCardPanel extends JPanel {

	private final Game game;
	private final Consumer<ScoreCategory> onScore;

	private final Map<ScoreCategory, JLabel> valueLabels = new EnumMap<>(ScoreCategory.class);
	private final Map<ScoreCategory, JButton> scoreButtons = new EnumMap<>(ScoreCategory.class);

	private JLabel upperSubtotalLabel;
	private JLabel upperBonusLabel;
	private JLabel lowerSubtotalLabel;
	private JLabel yahtzeeBonusLabel;
	private JLabel grandTotalLabel;

	public ScoreCardPanel(Game game, Consumer<ScoreCategory> onScore) {
		this.game = game;
		this.onScore = onScore;
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(8, 8, 8, 8));
		buildRows();
	}

	private void buildRows() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 6, 2, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		row = addHeaderRow(gbc, row, "Upper Section");
		for (ScoreCategory cat : ScoreCategory.values()) {
			if (cat.isUpper()) {
				row = addCategoryRow(gbc, row, cat);
			}
		}
		upperSubtotalLabel = addSummaryRow(gbc, row++, "Upper Subtotal");
		upperBonusLabel = addSummaryRow(gbc, row++, "Upper Bonus (63+ \u2192 35)");

		row = addHeaderRow(gbc, row, "Lower Section");
		for (ScoreCategory cat : ScoreCategory.values()) {
			if (!cat.isUpper()) {
				row = addCategoryRow(gbc, row, cat);
			}
		}
		yahtzeeBonusLabel = addSummaryRow(gbc, row++, "Bonus Yahtzees (\u00d7100)");
		lowerSubtotalLabel = addSummaryRow(gbc, row++, "Lower Subtotal");

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 3;
		add(new JSeparator(), gbc);
		row++;

		grandTotalLabel = addSummaryRow(gbc, row++, "GRAND TOTAL");
		grandTotalLabel.setFont(grandTotalLabel.getFont().deriveFont(Font.BOLD, 15f));
	}

	private int addHeaderRow(GridBagConstraints gbc, int row, String text) {
		JLabel header = new JLabel(text);
		header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 3;
		add(header, gbc);
		gbc.gridwidth = 1;
		return row + 1;
	}

	private int addCategoryRow(GridBagConstraints gbc, int row, ScoreCategory cat) {
		JLabel nameLabel = new JLabel(cat.getLabel());
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 1.0;
		add(nameLabel, gbc);

		JLabel valueLabel = new JLabel("0", SwingConstants.RIGHT);
		gbc.gridx = 1;
		gbc.weightx = 0;
		add(valueLabel, gbc);
		valueLabels.put(cat, valueLabel);

		JButton scoreButton = new JButton("Score");
		scoreButton.addActionListener(e -> onScore.accept(cat));
		gbc.gridx = 2;
		add(scoreButton, gbc);
		scoreButtons.put(cat, scoreButton);

		return row + 1;
	}

	private JLabel addSummaryRow(GridBagConstraints gbc, int row, String label) {
		JLabel nameLabel = new JLabel(label);
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		add(nameLabel, gbc);
		gbc.gridwidth = 1;

		JLabel valueLabel = new JLabel("0", SwingConstants.RIGHT);
		gbc.gridx = 2;
		add(valueLabel, gbc);
		return valueLabel;
	}

	/** Call after every roll, hold-toggle, or score commit to refresh all displayed numbers. */
	public void refresh() {
		ScoreCard card = game.getCurrentPlayer().getScoreCard();
		boolean canPreview = game.hasRolledThisTurn() && !game.isGameOver();

		for (ScoreCategory cat : ScoreCategory.values()) {
			JLabel valueLabel = valueLabels.get(cat);
			JButton button = scoreButtons.get(cat);
			boolean used = card.isUsed(cat);

			if (used) {
				valueLabel.setText(Integer.toString(card.getScore(cat)));
				valueLabel.setForeground(new Color(40, 110, 40));
				button.setEnabled(false);
				button.setText("Used");
			} else {
				int preview = canPreview ? game.getCurrentHand().getScore(cat) : 0;
				valueLabel.setText(canPreview ? Integer.toString(preview) : "-");
				valueLabel.setForeground(Color.DARK_GRAY);
				button.setEnabled(canPreview);
				button.setText("Score");
			}
		}

		upperSubtotalLabel.setText(Integer.toString(card.upperSubtotal()));
		upperBonusLabel.setText(Integer.toString(card.upperBonus()));
		yahtzeeBonusLabel.setText(Integer.toString(card.getYahtzeeBonus()));
		lowerSubtotalLabel.setText(Integer.toString(card.lowerSubtotal()));
		grandTotalLabel.setText(Integer.toString(card.grandTotal()));
	}
}
