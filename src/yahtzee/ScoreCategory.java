package yahtzee;

/**
 * The 13 scoring categories on a Yahtzee scorecard, in standard order.
 * Ordinal position (0-12) doubles as the index into the score-options
 * array produced by {@link DiceHand#getScoreOptions()}, so keep the
 * declaration order in sync with that method.
 */
public enum ScoreCategory {

	ONES("Ones"),
	TWOS("Twos"),
	THREES("Threes"),
	FOURS("Fours"),
	FIVES("Fives"),
	SIXES("Sixes"),
	THREE_OF_KIND("3 of a Kind"),
	FOUR_OF_KIND("4 of a Kind"),
	FULL_HOUSE("Full House"),
	SMALL_STRAIGHT("Small Straight"),
	LARGE_STRAIGHT("Large Straight"),
	YAHTZEE("Yahtzee"),
	CHANCE("Chance");

	private final String label;

	ScoreCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** Upper section = the six "Ones" through "Sixes" categories. */
	public boolean isUpper() {
		return ordinal() < 6;
	}
}
