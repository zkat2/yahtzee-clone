package yahtzee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a full game of Yahtzee: round/turn progression, the 3-rolls-per-turn
 * rule, holding dice between rolls, and committing a score to the current
 * player's scorecard. Works with any number of players: a round consists of
 * each player taking exactly one turn in order, and the round counter only
 * advances once every player has played that round.
 */
public class Game {

	public static final int TOTAL_ROUNDS = 13;
	public static final int ROLLS_PER_TURN = 3;
	private static final int DICE_COUNT = 5;

	private final List<Player> players;
	private final Dice[] dice = new Dice[DICE_COUNT];

	private int currentPlayerIndex = 0;
	private int round = 1;
	private int rollsLeft = ROLLS_PER_TURN;
	private boolean hasRolledThisTurn = false;

	public Game(List<String> playerNames) {
		if (playerNames == null || playerNames.isEmpty()) {
			throw new IllegalArgumentException("Need at least one player");
		}
		players = new ArrayList<>();
		for (String name : playerNames) {
			players.add(new Player(name));
		}
		for (int i = 0; i < DICE_COUNT; i++) {
			dice[i] = new Dice();
		}
	}

	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public Player getCurrentPlayer() {
		return players.get(currentPlayerIndex);
	}

	public Dice[] getDice() {
		return dice;
	}

	public int getRound() {
		return round;
	}

	public int getRollsLeft() {
		return rollsLeft;
	}

	public boolean hasRolledThisTurn() {
		return hasRolledThisTurn;
	}

	public boolean isGameOver() {
		return round > TOTAL_ROUNDS;
	}

	public boolean canRoll() {
		return !isGameOver() && rollsLeft > 0;
	}

	/** Returns a live view of the current dice for scoring previews. */
	public DiceHand getCurrentHand() {
		return new DiceHand(dice);
	}

	/** Rolls every die that isn't held. Forces a fresh roll for held-off dice the first time a turn starts. */
	public void rollDice() {
		if (!canRoll()) {
			return;
		}
		for (Dice d : dice) {
			d.roll();
		}
		rollsLeft--;
		hasRolledThisTurn = true;
	}

	/** Toggles whether a die is held between rolls. Only allowed after the first roll of a turn, and while rolls remain. */
	public boolean toggleHold(int index) {
		if (isGameOver() || !hasRolledThisTurn || rollsLeft == 0) {
			return false;
		}
		dice[index].toggleHeld();
		return true;
	}

	/**
	 * Commits the current dice hand's score to the given category for the
	 * current player, then advances to the next player/round. Returns false
	 * (no-op) if the category is already used, the game is over, or the
	 * player hasn't rolled yet this turn.
	 */
	public boolean scoreCategory(ScoreCategory category) {
		if (isGameOver() || !hasRolledThisTurn) {
			return false;
		}
		ScoreCard card = getCurrentPlayer().getScoreCard();
		if (card.isUsed(category)) {
			return false;
		}

		DiceHand hand = getCurrentHand();

		// Bonus Yahtzee: a second (or later) five-of-a-kind after the first
		// Yahtzee has already been scored as a genuine 50 is worth +100,
		// on top of whatever category this roll ultimately gets placed in.
		boolean isYahtzeeRoll = hand.getScore(ScoreCategory.YAHTZEE) == 50;
		boolean alreadyScoredYahtzee = card.isUsed(ScoreCategory.YAHTZEE)
				&& card.getScore(ScoreCategory.YAHTZEE) == 50;
		if (isYahtzeeRoll && alreadyScoredYahtzee) {
			card.addYahtzeeBonus();
		}

		card.setScore(category, hand.getScore(category));
		advanceTurn();
		return true;
	}

	private void advanceTurn() {
		currentPlayerIndex++;
		if (currentPlayerIndex >= players.size()) {
			currentPlayerIndex = 0;
			round++;
		}
		rollsLeft = ROLLS_PER_TURN;
		hasRolledThisTurn = false;
		for (Dice d : dice) {
			d.setHeld(false);
		}
	}
}
