package yahtzee;

/** A player taking part in a game: a display name paired with that player's own scorecard. */
public class Player {

	private final String name;
	private final ScoreCard scoreCard = new ScoreCard();

	public Player(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	@Override
	public String toString() {
		return name;
	}
}
