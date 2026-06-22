package yahtzee;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

	private Game singlePlayerGame() {
		return new Game(Collections.singletonList("Tester"));
	}

	private Game twoPlayerGame() {
		return new Game(List.of("Alice", "Bob"));
	}

	@Test
	void newGameStartsAtRoundOneWithThreeRollsAvailable() {
		Game game = singlePlayerGame();
		assertEquals(1, game.getRound());
		assertEquals(3, game.getRollsLeft());
		assertFalse(game.hasRolledThisTurn());
		assertTrue(game.canRoll());
	}

	@Test
	void rollingThreeTimesExhaustsRollsForTheTurn() {
		Game game = singlePlayerGame();
		game.rollDice();
		game.rollDice();
		game.rollDice();
		assertEquals(0, game.getRollsLeft());
		assertFalse(game.canRoll());
		game.rollDice(); // should be a no-op
		assertEquals(0, game.getRollsLeft());
	}

	@Test
	void cannotHoldDiceBeforeFirstRoll() {
		Game game = singlePlayerGame();
		boolean held = game.toggleHold(0);
		assertFalse(held);
		assertFalse(game.getDice()[0].isHeld());
	}

	@Test
	void canHoldDiceAfterFirstRoll() {
		Game game = singlePlayerGame();
		game.rollDice();
		boolean held = game.toggleHold(0);
		assertTrue(held);
		assertTrue(game.getDice()[0].isHeld());
	}

	@Test
	void heldDiceDoNotChangeValueOnNextRoll() {
		Game game = singlePlayerGame();
		game.rollDice();
		game.toggleHold(0);
		int heldValue = game.getDice()[0].getValue();
		game.rollDice();
		assertEquals(heldValue, game.getDice()[0].getValue());
	}

	@Test
	void scoringAdvancesTheRoundForASinglePlayer() {
		Game game = singlePlayerGame();
		game.rollDice();
		boolean scored = game.scoreCategory(ScoreCategory.CHANCE);
		assertTrue(scored);
		assertEquals(2, game.getRound());
		assertEquals(3, game.getRollsLeft());
		assertFalse(game.hasRolledThisTurn());
	}

	@Test
	void cannotScoreTheSameCategoryTwice() {
		Game game = singlePlayerGame();
		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE);

		// Chance is now used for round 1; simulate round 2 trying to reuse it
		game.rollDice();
		boolean scoredAgain = game.scoreCategory(ScoreCategory.CHANCE);
		assertFalse(scoredAgain);
	}

	@Test
	void cannotScoreBeforeRollingThisTurn() {
		Game game = singlePlayerGame();
		boolean scored = game.scoreCategory(ScoreCategory.CHANCE);
		assertFalse(scored);
	}

	@Test
	void gameEndsAfterThirteenRounds() {
		Game game = singlePlayerGame();
		List<ScoreCategory> categories = List.of(ScoreCategory.values());
		for (int round = 0; round < Game.TOTAL_ROUNDS; round++) {
			assertFalse(game.isGameOver());
			game.rollDice();
			game.scoreCategory(categories.get(round));
		}
		assertTrue(game.isGameOver());
		assertFalse(game.canRoll());
	}

	@Test
	void turnPassesToTheSecondPlayerAfterTheFirstScores() {
		Game game = twoPlayerGame();
		assertEquals("Alice", game.getCurrentPlayer().getName());

		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE);

		assertEquals("Bob", game.getCurrentPlayer().getName());
		assertEquals(1, game.getRound());
		assertEquals(3, game.getRollsLeft());
		assertFalse(game.hasRolledThisTurn());
	}

	@Test
	void roundAdvancesOnlyAfterEveryPlayerHasTakenATurn() {
		Game game = twoPlayerGame();

		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE);
		assertEquals(1, game.getRound()); // Bob still has to play round 1

		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE);
		assertEquals(2, game.getRound()); // both players have played round 1
		assertEquals("Alice", game.getCurrentPlayer().getName());
	}

	@Test
	void eachPlayerKeepsTheirOwnScorecard() {
		Game game = twoPlayerGame();
		List<Player> players = game.getPlayers();

		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE); // Alice scores

		game.rollDice();
		game.scoreCategory(ScoreCategory.CHANCE); // Bob scores

		assertTrue(players.get(0).getScoreCard().isUsed(ScoreCategory.CHANCE));
		assertTrue(players.get(1).getScoreCard().isUsed(ScoreCategory.CHANCE));
	}
}
