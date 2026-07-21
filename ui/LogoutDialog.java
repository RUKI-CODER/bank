package bank.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LogoutDialog extends JDialog {

    private static final int CARD_SIZE = 420;
    private static final int BORDER_RADIUS = 24;

    private final Runnable onLogout;

    public LogoutDialog(JFrame parent, Runnable onLogout) {
        super(parent, "Logout", true);
        this.onLogout = onLogout;
        initUI();
    }

    public LogoutDialog(JFrame parent) {
        this(parent, () -> {
            JOptionPane.showMessageDialog(parent, "Logged out (default action).", "Info", JOptionPane.INFORMATION_MESSAGE);
            parent.dispose();
        });
    }

    private void initUI() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedCardPanel cardPanel = new RoundedCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(48, 40, 40, 40));
        cardPanel.setBackground(Color.WHITE);

        // --- Title (no icon, just text) ---
        JLabel titleLabel = new JLabel("Logout Confirmation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- Subtext ---
        JLabel subLabel = new JLabel("Are you sure you want to logout?");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(120, 120, 120));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(subLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 36)));

        // --- Button Panel (Yes / No) ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton yesButton = createButton("Yes", new Color(33, 150, 243));
        yesButton.addActionListener(e -> {
            dispose();
            if (onLogout != null) onLogout.run();
        });

        JButton noButton = createButton("No", new Color(160, 160, 160));
        noButton.addActionListener(e -> dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(16, 0)));
        buttonPanel.add(noButton);

        cardPanel.add(buttonPanel);

        // Transparent content pane
        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) { }
        });
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(0, 0, 0, 0));
        getContentPane().add(cardPanel);

        pack();
        setSize(CARD_SIZE, CARD_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 45));
        button.setMaximumSize(new Dimension(100, 45));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private static class RoundedCardPanel extends JPanel {
        private static final int SHADOW_SIZE = 16;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = BORDER_RADIUS;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(
                    SHADOW_SIZE / 2f, SHADOW_SIZE / 2f,
                    w - SHADOW_SIZE, h - SHADOW_SIZE,
                    r, r
            ));

            // White card
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, r, r));

            // Subtle border
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

            g2.dispose();
        }
    }
}