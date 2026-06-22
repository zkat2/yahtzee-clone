package yahtzee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreCardTest {

	@Test
	void newScoreCardHasNoUsedCategoriesAndZeroTotal() {
		ScoreCard card = new ScoreCard();
		for (ScoreCategory cat : ScoreCategory.values()) {
			assertFalse(card.isUsed(cat));
		}
		assertEquals(0, card.grandTotal());
		assertFalse(card.isComplete());
	}

	@Test
	void upperBonusAwardedAtThreshold() {
		ScoreCard card = new ScoreCard();
		card.setScore(ScoreCategory.FOURS, 4 * 4);   // 16
		card.setScore(ScoreCategory.FIVES, 5 * 5);   // 25
		card.setScore(ScoreCategory.SIXES, 6 * 4);   // 24 -> upper subtotal 65, bonus qualifies (>=63)
		assertEquals(65, card.upperSubtotal());
		assertEquals(35, card.upperBonus());
		assertEquals(100, card.upperTotal());
	}

	@Test
	void noUpperBonusBelowThreshold() {
		ScoreCard card = new ScoreCard();
		card.setScore(ScoreCategory.ONES, 1);
		card.setScore(ScoreCategory.TWOS, 2);
		assertEquals(3, card.upperSubtotal());
		assertEquals(0, card.upperBonus());
	}

	@Test
	void yahtzeeBonusAddsHundredToLowerSubtotal() {
		ScoreCard card = new ScoreCard();
		card.setScore(ScoreCategory.YAHTZEE, 50);
		card.setScore(ScoreCategory.CHANCE, 20);
		card.addYahtzeeBonus();
		assertEquals(100, card.getYahtzeeBonus());
		assertEquals(50 + 20 + 100, card.lowerSubtotal());
	}

	@Test
	void isCompleteOnlyWhenAllThirteenCategoriesUsed() {
		ScoreCard card = new ScoreCard();
		for (ScoreCategory cat : ScoreCategory.values()) {
			assertFalse(card.isComplete());
			card.setScore(cat, 0);
		}
		assertTrue(card.isComplete());
		assertEquals(ScoreCategory.values().length, card.categoriesUsed());
	}

	@Test
	void grandTotalSumsUpperAndLowerIncludingBonuses() {
		ScoreCard card = new ScoreCard();
		card.setScore(ScoreCategory.ONES, 3);
		card.setScore(ScoreCategory.TWOS, 6);
		card.setScore(ScoreCategory.THREES, 9);
		card.setScore(ScoreCategory.FOURS, 12);
		card.setScore(ScoreCategory.FIVES, 15);
		card.setScore(ScoreCategory.SIXES, 18); // upper subtotal 63 -> bonus 35
		card.setScore(ScoreCategory.CHANCE, 20);

		assertEquals(63, card.upperSubtotal());
		assertEquals(35, card.upperBonus());
		assertEquals(98, card.upperTotal());
		assertEquals(20, card.lowerSubtotal());
		assertEquals(118, card.grandTotal());
	}
}
