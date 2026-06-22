package yahtzee;

import java.util.Random;

/**
 * Represents a single six-sided die: a face value from 1 to 6, and a
 * "held" flag that lets a player keep the die's current value across
 * rolls within a turn instead of having every die re-roll each time.
 */
public class Dice {

	private static final Random RNG = new Random();

	private int value;
	private boolean held;

	public Dice() {
		this.value = 1;
		this.held = false;
	}

	public Dice(int value) {
		this.value = value;
		this.held = false;
	}

	/** Rolls this die to a new random value (1-6), unless it is held. */
	public void roll() {
		if (!held) {
			value = RNG.nextInt(6) + 1;
		}
	}

	/** Rolls this die to a new random value (1-6) even if it is currently held. */
	public void forceRoll() {
		value = RNG.nextInt(6) + 1;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isHeld() {
		return held;
	}

	public void setHeld(boolean held) {
		this.held = held;
	}

	public void toggleHeld() {
		held = !held;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
