package yahtzee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the DiceHand scoring engine, covering straight detection,
 * n-of-a-kind and Yahtzee detection, and full house detection.
 */
class DiceHandTest {

	private DiceHand handOf(int... values) {
		Dice[] dice = new Dice[5];
		for (int i = 0; i < 5; i++) {
			dice[i] = new Dice(values[i]);
		}
		return new DiceHand(dice);
	}

	@Test
	void upperSectionScoresCountTimesFaceValue() {
		DiceHand hand = handOf(3, 3, 3, 5, 6);
		int[] options = hand.getScoreOptions();
		assertEquals(9, options[ScoreCategory.THREES.ordinal()]); // 3 dice x 3
		assertEquals(5, options[ScoreCategory.FIVES.ordinal()]);
		assertEquals(6, options[ScoreCategory.SIXES.ordinal()]);
		assertEquals(0, options[ScoreCategory.ONES.ordinal()]);
	}

	@Test
	void threeOfAKindScoresSumOfAllDice() {
		DiceHand hand = handOf(3, 3, 3, 5, 6);
		assertEquals(20, hand.getScore(ScoreCategory.THREE_OF_KIND));
		assertEquals(0, hand.getScore(ScoreCategory.FOUR_OF_KIND));
	}

	@Test
	void fourOfAKindAlsoCountsAsThreeOfAKind() {
		DiceHand hand = handOf(4, 4, 4, 4, 2);
		assertEquals(18, hand.getScore(ScoreCategory.FOUR_OF_KIND));
		assertEquals(18, hand.getScore(ScoreCategory.THREE_OF_KIND));
	}

	@Test
	void yahtzeeScoresFifty() {
		DiceHand hand = handOf(6, 6, 6, 6, 6);
		assertEquals(50, hand.getScore(ScoreCategory.YAHTZEE));
		assertEquals(30, hand.getScore(ScoreCategory.SIXES));
	}

	@Test
	void fullHouseDetectsThreePlusTwo() {
		DiceHand hand = handOf(2, 2, 5, 5, 5);
		assertEquals(25, hand.getScore(ScoreCategory.FULL_HOUSE));
	}

	@Test
	void fullHouseRejectsFourOfAKind() {
		DiceHand hand = handOf(2, 2, 2, 2, 5);
		assertEquals(0, hand.getScore(ScoreCategory.FULL_HOUSE));
	}

	@Test
	void fullHouseRejectsYahtzee() {
		DiceHand hand = handOf(5, 5, 5, 5, 5);
		assertEquals(0, hand.getScore(ScoreCategory.FULL_HOUSE));
	}

	@Test
	void largeStraightScoresForty() {
		DiceHand hand = handOf(2, 3, 4, 5, 6);
		assertEquals(40, hand.getScore(ScoreCategory.LARGE_STRAIGHT));
		assertEquals(30, hand.getScore(ScoreCategory.SMALL_STRAIGHT));
	}

	@Test
	void smallStraightScoresThirtyForFourConsecutiveWithOneDuplicate() {
		DiceHand hand = handOf(1, 2, 3, 4, 4);
		assertEquals(30, hand.getScore(ScoreCategory.SMALL_STRAIGHT));
		assertEquals(0, hand.getScore(ScoreCategory.LARGE_STRAIGHT));
	}

	/**
	 * Two separate short runs ({1,2,3} and {5,6}) must not be combined into
	 * one longer run; the longest consecutive run here is only 3, so
	 * neither a small nor a large straight should be detected.
	 */
	@Test
	void twoSeparateShortRunsDoNotFalselyAddUpToAStraight() {
		DiceHand hand = handOf(1, 2, 3, 5, 6);
		assertEquals(0, hand.getScore(ScoreCategory.SMALL_STRAIGHT));
		assertEquals(0, hand.getScore(ScoreCategory.LARGE_STRAIGHT));
	}

	@Test
	void chanceScoresSumOfAllDice() {
		DiceHand hand = handOf(1, 2, 3, 4, 5);
		assertEquals(15, hand.getScore(ScoreCategory.CHANCE));
	}

	@Test
	void anyCategoryCanBeScoredAsZeroWhenItDoesNotQualify() {
		DiceHand hand = handOf(1, 1, 2, 3, 4);
		assertEquals(0, hand.getScore(ScoreCategory.YAHTZEE));
		assertEquals(0, hand.getScore(ScoreCategory.FULL_HOUSE));
	}

	@Test
	void bestScorePicksTheHighestOption() {
		DiceHand hand = handOf(5, 5, 5, 5, 5);
		int[] best = hand.bestScore();
		assertEquals(50, best[0]);
		assertEquals(ScoreCategory.YAHTZEE.ordinal(), best[1]);
	}

	@Test
	void equalsComparesValueCountsNotOrder() {
		DiceHand a = handOf(1, 2, 3, 4, 5);
		DiceHand b = handOf(5, 4, 3, 2, 1);
		assertTrue(a.equals(b));

		DiceHand c = handOf(1, 1, 3, 4, 5);
		assertFalse(a.equals(c));
	}
}
