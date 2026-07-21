package bank.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A modern 'Add Customer' dialog that returns the entered data.
 * Use isSaved() and the getters after the dialog is closed.
 */
public class AddCustomerDialog extends JDialog {

    // Colors for a modern light theme
    private static final Color BACKGROUND_COLOR = new Color(0xF5F5F5);
    private static final Color BORDER_COLOR = new Color(0xE0E0E0);
    private static final Color PRIMARY_BLUE = new Color(0x007BFF);
    private static final Color BUTTON_TEXT_WHITE = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(0x333333);
    private static final Color PLACEHOLDER_COLOR = new Color(0xBBBBBB);

    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField dobField;
    private JTextField emailField;
    private JComboBox<String> genderCombo;

    private boolean saved = false;   // true if the user clicked Save

    // Getters for the entered data
    public String getFullName() { return fullNameField.getText(); }
    public String getPhone()    { return phoneField.getText(); }
    public String getAddress()  { return addressField.getText(); }
    public String getDob()      { return dobField.getText(); }
    public String getEmail()    { return emailField.getText(); }
    public String getGender()   { return (String) genderCombo.getSelectedItem(); }
    public boolean isSaved()    { return saved; }

    public AddCustomerDialog(JFrame parent) {
        super(parent, "Add Customer", true);
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

        JLabel titleLabel = new JLabel("Add Customer");
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
        gbc.gridx = 0;
        gbc.gridy = 0;

        String[] labels = {"Full Name", "Phone", "Address", "Date of Birth", "Email", "Gender"};
        String[] placeholders = {"Enter full name", "Enter phone number", "Enter address",
                "mm/dd/yyyy", "Enter email", null};
        JComponent[] inputs = new JComponent[6];

        fullNameField = createPlaceholderTextField(placeholders[0]);
        phoneField = createPlaceholderTextField(placeholders[1]);
        addressField = createPlaceholderTextField(placeholders[2]);
        dobField = createPlaceholderTextField(placeholders[3]);
        emailField = createPlaceholderTextField(placeholders[4]);

        genderCombo = new JComboBox<>(new String[]{"Select", "Male", "Female", "Other"});
        genderCombo.setBackground(Color.WHITE);

        inputs[0] = fullNameField;
        inputs[1] = phoneField;
        inputs[2] = addressField;
        inputs[3] = dobField;
        inputs[4] = emailField;
        inputs[5] = genderCombo;

        for (int i = 0; i < labels.length; i++) {
            JPanel fieldPanel = createFieldPanel(labels[i], inputs[i]);
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            form.add(fieldPanel, gbc);
        }

        return form;
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
}