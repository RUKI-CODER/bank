package bank.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A modern 'Add Employee' dialog that returns the entered data.
 * Use isSaved() and the getters after the dialog is closed.
 */
public class AddEmployeeDialog extends JDialog {

    // Colors (mirroring AddCustomerDialog)
    private static final Color BACKGROUND_COLOR = new Color(0xF5F5F5);
    private static final Color BORDER_COLOR = new Color(0xE0E0E0);
    private static final Color PRIMARY_BLUE = new Color(0x007BFF);
    private static final Color BUTTON_TEXT_WHITE = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(0x333333);
    private static final Color PLACEHOLDER_COLOR = new Color(0xBBBBBB);

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JComboBox<String> statusCombo;

    private boolean saved = false;
    private boolean passwordVisible = false;

    // Getters
    public String getFullName() { return fullNameField.getText(); }
    public String getUsername() { return usernameField.getText(); }
    public String getPassword() {
        // If the placeholder is shown, return empty string
        String placeholder = "Enter password";
        if (new String(passwordField.getPassword()).equals(placeholder)) {
            return "";
        }
        return new String(passwordField.getPassword());
    }
    public String getPhone()    { return phoneField.getText(); }
    public String getStatus()   { return (String) statusCombo.getSelectedItem(); }
    public boolean isSaved()    { return saved; }

    public AddEmployeeDialog(JFrame parent) {
        super(parent, "Add Employee", true);
        initUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        setContentPane(mainPanel);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Add Employee");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(LABEL_COLOR);
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 16));
        closeButton.setForeground(Color.GRAY);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;

        // Row 1: Full Name (left), Username (right)
        fullNameField = createPlaceholderTextField("Enter full name");
        JPanel namePanel = createFieldPanel("Full Name", fullNameField);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        form.add(namePanel, gbc);

        usernameField = createPlaceholderTextField("Enter username");
        JPanel usernamePanel = createFieldPanel("Username", usernameField);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        form.add(usernamePanel, gbc);

        // Row 2: Password (left), Phone (right)
        // Create password field with eye toggle
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        passwordField.setBackground(Color.WHITE);

        // Set placeholder on password field
        String placeholder = "Enter password";
        passwordField.setEchoChar((char) 0);
        passwordField.setText(placeholder);
        passwordField.setForeground(PLACEHOLDER_COLOR);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String text = new String(passwordField.getPassword());
                if (text.equals(placeholder)) {
                    passwordField.setText("");
                    // If passwordVisible is false, mask it; otherwise keep it visible
                    if (!passwordVisible) {
                        passwordField.setEchoChar('*');
                    }
                    passwordField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String text = new String(passwordField.getPassword());
                if (text.isEmpty()) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText(placeholder);
                    passwordField.setForeground(PLACEHOLDER_COLOR);
                    passwordVisible = false; // reset state when placeholder appears
                }
            }
        });

        // Eye toggle button
        JLabel eyeLabel = new JLabel("👁");
        eyeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        eyeLabel.setForeground(TEXT_MUTED);
        eyeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        eyeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                togglePasswordVisibility();
            }
        });

        JPanel passwordWrapper = new JPanel(new BorderLayout());
        passwordWrapper.setBackground(Color.WHITE);
        passwordWrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        passwordWrapper.add(passwordField, BorderLayout.CENTER);
        passwordWrapper.add(eyeLabel, BorderLayout.EAST);

        JPanel passwordPanel = createFieldPanel("Password", passwordWrapper);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        form.add(passwordPanel, gbc);

        // Phone field
        phoneField = createPlaceholderTextField("Enter phone number");
        JPanel phonePanel = createFieldPanel("Phone", phoneField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        form.add(phonePanel, gbc);

        // Row 3: Status (spanning both columns)
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setBackground(Color.WHITE);
        JPanel statusPanel = createFieldPanel("Status", statusCombo);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(statusPanel, gbc);

        return form;
    }

    private void togglePasswordVisibility() {
        String text = new String(passwordField.getPassword());
        String placeholder = "Enter password";
        if (text.equals(placeholder)) {
            // Do nothing if placeholder is shown
            return;
        }
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('*');
        }
        // Keep the text as is
        passwordField.requestFocusInWindow();
    }

    private JPanel createFieldPanel(String labelText, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(LABEL_COLOR);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setBackground(Color.WHITE);

        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });

        return field;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setBackground(new Color(0xE9ECEF));
        cancelButton.setForeground(LABEL_COLOR);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        saveButton.setBackground(PRIMARY_BLUE);
        saveButton.setForeground(BUTTON_TEXT_WHITE);
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_BLUE, 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            saved = true;
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    // Reuse the muted color from EmployeePanel for consistency
    private static final Color TEXT_MUTED = new Color(108, 117, 125);
}