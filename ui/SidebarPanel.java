package bank.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {

    private static final Color NAVY_DARK = new Color(13, 33, 74);
    private static final Color NAVY_ACTIVE = new Color(24, 96, 210);
    private static final Color NAVY_HOVER = new Color(20, 55, 110);
    private static final Color TEXT_LIGHT = new Color(210, 220, 235);

    private final List<NavButton> navButtons = new ArrayList<>();
    private final List<NavigationListener> listeners = new ArrayList<>();
    private NavButton activeButton;

    public SidebarPanel() {
        setPreferredSize(new Dimension(230, 0));
        setBackground(NAVY_DARK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(buildLogo());
        add(Box.createVerticalStrut(25));
        add(Box.createVerticalStrut(15));

        // Navigation items
        add(navItem("Dashboard"));
        add(navItem("Employees"));
        add(navItem("Customers"));
        add(navItem("Accounts"));
        add(navItem("Transactions"));
        add(navItem("Change Password"));

        // Push everything above the logout item
        add(Box.createVerticalGlue());

        // Logout with bold text, no icon
        add(buildLogoutItem());

        setActive(navButtons.get(0));
    }

    public void addNavigationListener(NavigationListener listener) {
        listeners.add(listener);
    }

    private void fireNavigation(String page) {
        for (NavigationListener listener : listeners) {
            listener.onNavigate(page);
        }
    }

    private JPanel buildLogo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JLabel title = new JLabel("BANK");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setForeground(TEXT_LIGHT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);

        return panel;
    }

    private NavButton navItem(String text) {
        NavButton button = new NavButton(text);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(button);
                fireNavigation(text);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(NAVY_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(NAVY_DARK);
                }
            }
        });

        navButtons.add(button);
        return button;
    }

    private void setActive(NavButton button) {
        if (activeButton != null) {
            activeButton.setBackground(NAVY_DARK);
            activeButton.setSelected(false);
        }
        activeButton = button;
        activeButton.setBackground(NAVY_ACTIVE);
        activeButton.setSelected(true);
    }

    /**
     * Builds the "Logout" item with bold white text (no icon).
     * Clicking opens the LogoutDialog with Yes/No confirmation.
     * On "Yes", closes the dashboard and re-opens the LoginForm.
     */
    private JPanel buildLogoutItem() {
        NavButton logoutButton = new NavButton("Logout");

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(SidebarPanel.this);
                if (parent != null) {
                    LogoutDialog dialog = new LogoutDialog(parent, () -> {
                        // Dispose the dashboard
                        parent.dispose();
                        // Show the login screen again (using the renamed class)
                        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
                    });
                    dialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(SidebarPanel.this,
                            "Could not find parent window.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(NAVY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(NAVY_DARK);
            }
        });

        // Make it always bold and white (like an active item)
        logoutButton.setSelected(true);

        return logoutButton;
    }

    // ======================== NAV BUTTON ========================
    private static class NavButton extends JPanel {
        private final JLabel textLabel;
        private boolean selected;

        NavButton(String text) {
            setLayout(new BorderLayout());
            setBackground(NAVY_DARK);
            setBorder(new EmptyBorder(26, 22, 20, 10));
            setMaximumSize(new Dimension(230, 75));

            textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textLabel.setForeground(TEXT_LIGHT);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            wrapper.setOpaque(false);
            wrapper.add(textLabel);

            add(wrapper, BorderLayout.WEST);
        }

        void setSelected(boolean value) {
            this.selected = value;
            textLabel.setForeground(value ? Color.WHITE : TEXT_LIGHT);
            textLabel.setFont(textLabel.getFont().deriveFont(value ? Font.BOLD : Font.PLAIN));
        }
    }

    public interface NavigationListener {
        void onNavigate(String page);
    }
}