package bank.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel with rounded corners and an optional subtle drop-shadow,
 * used to give cards / sidebar a modern flat-design look.
 */
public class RoundedPanel extends JPanel {

    private final int radius;
    private final Color bgColor;

    public RoundedPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.bgColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}