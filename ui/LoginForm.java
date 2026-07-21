package bank.ui;

import bank.dao.AdminDAO;
import bank.dao.impl.AdminDAOImpl;
import bank.model.Admin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginForm extends JFrame {
    private JTextField jtn;
    private JPasswordField jtp;
    private JLabel eyeToggle;
    private JButton log;

    private static final Color NAVY = new Color(30, 45, 74);
    private static final Color ACCENT_BLUE = new Color(74, 130, 216);
    private static final Color FIELD_BG = new Color(243, 245, 248);
    private static final Color LABEL_GRAY = new Color(90, 100, 115);

    public LoginForm() {
        setTitle("Bank Management System - Login");
        setSize(750, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidePanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);

        // Pre-fill with demo credentials
        jtn.setText("admin");
        jtp.setText("admin123");

        setVisible(true);
    }

    // Left dark navy panel with icon + bank name
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(260, 460));
        side.setBackground(NAVY);
        side.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83C\uDFDB");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bankName = new JLabel("BANK");
        bankName.setFont(new Font("SansSerif", Font.BOLD, 22));
        bankName.setForeground(Color.WHITE);
        bankName.setAlignmentX(Component.CENTER_ALIGNMENT);
        bankName.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel sub1 = new JLabel("MANAGEMENT");
        sub1.setFont(new Font("SansSerif", Font.BOLD, 16));
        sub1.setForeground(Color.WHITE);
        sub1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub2 = new JLabel("SYSTEM");
        sub2.setFont(new Font("SansSerif", Font.BOLD, 16));
        sub2.setForeground(Color.WHITE);
        sub2.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(bankName);
        inner.add(sub1);
        inner.add(sub2);

        side.add(inner);
        return side;
    }

    // Right white panel with the form
    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(null);

        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);
        title.setBounds(60, 50, 250, 35);
        form.add(title);

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLbl.setForeground(LABEL_GRAY);
        userLbl.setBounds(60, 110, 200, 20);
        form.add(userLbl);

        jtn = new JTextField();
        jtn.setBackground(FIELD_BG);
        jtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        jtn.setBounds(60, 133, 340, 38);
        form.add(jtn);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passLbl.setForeground(LABEL_GRAY);
        passLbl.setBounds(60, 185, 200, 20);
        form.add(passLbl);

        jtp = new JPasswordField();
        jtp.setEchoChar('•');
        jtp.setBackground(FIELD_BG);
        jtp.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        jtp.setBounds(60, 208, 340, 38);
        form.add(jtp);

        // Password visibility toggle (eye icon)
        eyeToggle = new JLabel("\uD83D\uDC41");
        eyeToggle.setBounds(365, 218, 25, 20);
        eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeToggle.addMouseListener(new MouseAdapter() {
            private boolean visible = false;

            public void mouseClicked(MouseEvent e) {
                visible = !visible;
                jtp.setEchoChar(visible ? (char) 0 : '•');
            }
        });
        form.add(eyeToggle);

        log = new JButton("Login");
        log.setBackground(ACCENT_BLUE);
        log.setForeground(Color.WHITE);
        log.setFocusPainted(false);
        log.setFont(new Font("SansSerif", Font.BOLD, 14));
        log.setBounds(60, 275, 340, 42);
        log.addActionListener(e -> attemptLogin());
        form.add(log);

        return form;
    }

    /**
     * Authenticates the user against the 'admin' table using the AdminDAO.
     */
    private void attemptLogin() {
        String username = jtn.getText().trim();
        String password = new String(jtp.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Declare outside try so it can be used after authentication
        AdminDAO adminDAO = null;
        try {
            adminDAO = new AdminDAOImpl();
            Admin admin = adminDAO.findByUsernameAndPassword(username, password);

            if (admin != null) {
                // Login successful – pass admin and DAO to the Dashboard
                dispose();
                new DashboardForm(admin, adminDAO);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                jtp.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}