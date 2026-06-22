package yahtzee;

import java.util.EnumMap;
import java.util.Map;

/**
 * One player's scorecard: which of the 13 categories have been used, and
 * the running totals, including the upper-section bonus and the bonus
 * awarded for rolling more than one Yahtzee in a game.
 */
public class ScoreCard {

	private static final int UPPER_BONUS_THRESHOLD = 63;
	private static final int UPPER_BONUS_VALUE = 35;
	private static final int YAHTZEE_BONUS_VALUE = 100;

	private final Map<ScoreCategory, Integer> scores = new EnumMap<>(ScoreCategory.class);
	private int yahtzeeBonus = 0;

	public boolean isUsed(ScoreCategory category) {
		return scores.containsKey(category);
	}

	/** Records a score for a category. Overwrites are not prevented here on purpose - Game enforces the "once per category" rule. */
	public void setScore(ScoreCategory category, int value) {
		scores.put(category, value);
	}

	public int getScore(ScoreCategory category) {
		return scores.getOrDefault(category, 0);
	}

	/** Adds a 100-point bonus for rolling an additional Yahtzee after the first one has already been scored as 50. */
	public void addYahtzeeBonus() {
		yahtzeeBonus += YAHTZEE_BONUS_VALUE;
	}

	public int getYahtzeeBonus() {
		return yahtzeeBonus;
	}

	public int upperSubtotal() {
		int sum = 0;
		for (ScoreCategory c : ScoreCategory.values()) {
			if (c.isUpper() && isUsed(c)) {
				sum += getScore(c);
			}
		}
		return sum;
	}

	public int upperBonus() {
		return upperSubtotal() >= UPPER_BONUS_THRESHOLD ? UPPER_BONUS_VALUE : 0;
	}

	public int upperTotal() {
		return upperSubtotal() + upperBonus();
	}

	public int lowerSubtotal() {
		int sum = 0;
		for (ScoreCategory c : ScoreCategory.values()) {
			if (!c.isUpper() && isUsed(c)) {
				sum += getScore(c);
			}
		}
		return sum + yahtzeeBonus;
	}

	public int grandTotal() {
		return upperTotal() + lowerSubtotal();
	}

	public boolean isComplete() {
		return scores.size() == ScoreCategory.values().length;
	}

	public int categoriesUsed() {
		return scores.size();
	}
}
