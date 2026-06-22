package yahtzee.ui;

import yahtzee.Dice;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Draws a single die face with pips, and highlights it when held.
 * Clicking toggles "held" via the supplied callback
 */
public class DiePanel extends JComponent {

	private static final int SIZE = 64;
	private static final Color FACE_COLOR = new Color(252, 252, 250);
	private static final Color HELD_FACE_COLOR = new Color(255, 224, 130);
	private static final Color BORDER_COLOR = new Color(70, 70, 70);
	private static final Color HELD_BORDER_COLOR = new Color(196, 130, 0);
	private static final Color PIP_COLOR = new Color(35, 35, 35);

	private final Dice die;

	public DiePanel(Dice die, Runnable onClick) {
		this.die = die;
		setPreferredSize(new Dimension(SIZE, SIZE));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClick.run();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		boolean held = die.isHeld();

		g2.setColor(held ? HELD_FACE_COLOR : FACE_COLOR);
		g2.fillRoundRect(3, 3, w - 6, h - 6, 14, 14);

		g2.setColor(held ? HELD_BORDER_COLOR : BORDER_COLOR);
		g2.setStroke(new BasicStroke(held ? 3f : 1.5f));
		g2.drawRoundRect(3, 3, w - 7, h - 7, 14, 14);

		g2.setColor(PIP_COLOR);
		drawPips(g2, die.getValue(), w, h);

		g2.dispose();
	}

	private void drawPips(Graphics2D g2, int value, int w, int h) {
		int pip = Math.max(6, w / 7);
		int cx = w / 2;
		int cy = h / 2;
		int off = w / 4;

		// 3x3 grid positions relative to center
		int left = cx - off;
		int right = cx + off;
		int top = cy - off;
		int bottom = cy + off;

		switch (value) {
			case 1:
				fillPip(g2, cx, cy, pip);
				break;
			case 2:
				fillPip(g2, left, top, pip);
				fillPip(g2, right, bottom, pip);
				break;
			case 3:
				fillPip(g2, left, top, pip);
				fillPip(g2, cx, cy, pip);
				fillPip(g2, right, bottom, pip);
				break;
			case 4:
				fillPip(g2, left, top, pip);
				fillPip(g2, right, top, pip);
				fillPip(g2, left, bottom, pip);
				fillPip(g2, right, bottom, pip);
				break;
			case 5:
				fillPip(g2, left, top, pip);
				fillPip(g2, right, top, pip);
				fillPip(g2, cx, cy, pip);
				fillPip(g2, left, bottom, pip);
				fillPip(g2, right, bottom, pip);
				break;
			case 6:
				fillPip(g2, left, top, pip);
				fillPip(g2, right, top, pip);
				fillPip(g2, left, cy, pip);
				fillPip(g2, right, cy, pip);
				fillPip(g2, left, bottom, pip);
				fillPip(g2, right, bottom, pip);
				break;
			default:
				// no pips for an unrolled/invalid value
		}
	}

	private void fillPip(Graphics2D g2, int centerX, int centerY, int diameter) {
		g2.fillOval(centerX - diameter / 2, centerY - diameter / 2, diameter, diameter);
	}
}
