package yahtzee;

import java.util.Arrays;

/**
 * Scoring engine for a hand of 5 dice. Evaluates the current dice values
 * against every one of the 13 Yahtzee categories and reports what each
 * would be worth if scored right now.
 *
 * Straight detection works by sorting the five face values and measuring
 * the longest run of consecutive distinct values; the run resets whenever
 * a gap appears, so two short, separated runs (such as {1,2,3,5,6}) are
 * never mistaken for one long straight.
 *
 * This class holds a reference to the Dice array, not a copy, so it
 * always reflects the live state of the dice during a turn and can be
 * used to preview scores before a roll is locked in.
 */
public class DiceHand {

	private final Dice[] dice;

	public DiceHand(Dice[] dice) {
		if (dice == null || dice.length != 5) {
			throw new IllegalArgumentException("DiceHand requires exactly 5 dice");
		}
		this.dice = dice;
	}

	/** Returns a 6-element array: counts[0] = how many dice show a 1, ... counts[5] = how many show a 6. */
	public int[] getValueCount() {
		int[] counts = new int[6];
		for (Dice d : dice) {
			counts[d.getValue() - 1]++;
		}
		return counts;
	}

	private int dieSum() {
		int sum = 0;
		for (Dice d : dice) {
			sum += d.getValue();
		}
		return sum;
	}

	/** Length of the longest run of consecutive distinct face values among the 5 dice. */
	private int longestRun() {
		int[] values = new int[5];
		for (int i = 0; i < 5; i++) {
			values[i] = dice[i].getValue();
		}
		Arrays.sort(values);

		int maxRun = 1;
		int currentRun = 1;
		for (int i = 1; i < values.length; i++) {
			int diff = values[i] - values[i - 1];
			if (diff == 1) {
				currentRun++;
				maxRun = Math.max(maxRun, currentRun);
			} else if (diff > 1) {
				currentRun = 1; // gap breaks the run
			}
			// diff == 0 (a duplicate) leaves the current run unchanged
		}
		return maxRun;
	}

	private boolean isFullHouse() {
		int[] counts = getValueCount();
		boolean hasThree = false;
		boolean hasTwo = false;
		for (int c : counts) {
			if (c == 3) hasThree = true;
			if (c == 2) hasTwo = true;
		}
		return hasThree && hasTwo;
	}

	/**
	 * Returns the potential score for every one of the 13 categories, given
	 * the current dice values - regardless of whether that category has
	 * already been used on a scorecard. Index i corresponds to
	 * ScoreCategory.values()[i].ordinal(). A category the dice don't
	 * qualify for simply scores 0, matching official rules (you're always
	 * allowed to "burn" a roll into any open category for zero).
	 */
	public int[] getScoreOptions() {
		int[] counts = getValueCount();
		int[] result = new int[ScoreCategory.values().length];

		for (int face = 1; face <= 6; face++) {
			result[face - 1] = counts[face - 1] * face;
		}

		int maxCount = 0;
		for (int c : counts) {
			maxCount = Math.max(maxCount, c);
		}
		int sum = dieSum();

		if (maxCount >= 3) {
			result[ScoreCategory.THREE_OF_KIND.ordinal()] = sum;
		}
		if (maxCount >= 4) {
			result[ScoreCategory.FOUR_OF_KIND.ordinal()] = sum;
		}
		if (maxCount == 5) {
			result[ScoreCategory.YAHTZEE.ordinal()] = 50;
		}
		if (isFullHouse()) {
			result[ScoreCategory.FULL_HOUSE.ordinal()] = 25;
		}

		int run = longestRun();
		if (run >= 4) {
			result[ScoreCategory.SMALL_STRAIGHT.ordinal()] = 30;
		}
		if (run == 5) {
			result[ScoreCategory.LARGE_STRAIGHT.ordinal()] = 40;
		}

		result[ScoreCategory.CHANCE.ordinal()] = sum;

		return result;
	}

	/** Convenience accessor for a single category's potential score. */
	public int getScore(ScoreCategory category) {
		return getScoreOptions()[category.ordinal()];
	}

	/** Returns {maxScore, categoryIndex} for the best-scoring category. */
	public int[] bestScore() {
		int[] options = getScoreOptions();
		int bestIndex = 0;
		int bestValue = options[0];
		for (int i = 1; i < options.length; i++) {
			if (options[i] > bestValue) {
				bestValue = options[i];
				bestIndex = i;
			}
		}
		return new int[] { bestValue, bestIndex };
	}

	/** True if both hands show the same multiset of face values. */
	public boolean equals(DiceHand other) {
		return Arrays.equals(this.getValueCount(), other.getValueCount());
	}

	@Override
	public String toString() {
		return String.format("Dice: {%d, %d, %d, %d, %d}",
				dice[0].getValue(), dice[1].getValue(), dice[2].getValue(), dice[3].getValue(), dice[4].getValue());
	}
}
