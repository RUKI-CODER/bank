package bank.ui;

import bank.model.Transaction;
import bank.dao.TransactionDAO;
import bank.dao.impl.TransactionDAOImpl;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Dialog for adding or editing a transaction.
 * When constructed with a Transaction object → Edit mode.
 * Otherwise → Add mode.
 */
public class AddTransactionDialog extends JDialog {

    private JTextField accountField;
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JLabel statusLabel;
    private JLabel dateLabel;
    private boolean saved = false;

    private Transaction existingTransaction; // null for add mode
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

    public AddTransactionDialog(Window parent, Transaction existing) {
        super(parent, existing == null ? "Add Transaction" : "Edit Transaction", ModalityType.APPLICATION_MODAL);
        this.existingTransaction = existing;
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Account No
        form.add(new JLabel("Account No:"), gbc);
        gbc.gridx = 1;
        accountField = new JTextField(20);
        accountField.setBorder(new LineBorder(new Color(200, 200, 210), 1, true));
        accountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) accountField.setText(existing.getAccountNo());
        form.add(accountField, gbc);

        // Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"Deposit", "Withdraw", "Transfer"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setBackground(Color.WHITE);
        if (existing != null) typeCombo.setSelectedItem(existing.getType());
        form.add(typeCombo, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(20);
        amountField.setBorder(new LineBorder(new Color(200, 200, 210), 1, true));
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) amountField.setText(existing.getAmount().toString());
        amountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateStatus();
            }
        });
        form.add(amountField, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        String dateStr = existing != null ?
                existing.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(80, 80, 100));
        form.add(dateLabel, gbc);

        // Status (auto-computed, read-only)
        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        form.add(statusLabel, gbc);

        // If editing, set initial status based on existing transaction
        if (existing != null) {
            updateStatusFromAmount(existing.getAmount().doubleValue());
        } else {
            updateStatus(); // uses current amount field
        }

        add(form, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 20));

        JButton saveBtn = new JButton(existing == null ? "Save" : "Update");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(0, 102, 204));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(new Color(0, 85, 170));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(new Color(0, 102, 204));
            }
        });
        saveBtn.addActionListener(e -> saveTransaction());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(0, 102, 204));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(0, 85, 170));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(new Color(0, 102, 204));
            }
        });
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveBtn);
    }

    private void updateStatus() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            statusLabel.setText("Success");
            statusLabel.setForeground(new Color(46, 160, 67));
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            updateStatusFromAmount(amount);
        } catch (NumberFormatException ex) {
            // ignore
        }
    }

    private void updateStatusFromAmount(double amount) {
        if (amount > 0) {
            statusLabel.setText("Success");
            statusLabel.setForeground(new Color(46, 160, 67));
        } else if (amount < 0) {
            statusLabel.setText("Failed");
            statusLabel.setForeground(new Color(192, 57, 43));
        } else {
            statusLabel.setText("Pending");
            statusLabel.setForeground(new Color(241, 196, 15));
        }
    }

    private void saveTransaction() {
        String account = accountField.getText().trim();
        if (account.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account No is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            accountField.requestFocus();
            return;
        }

        String amountStr = amountField.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            amountField.requestFocus();
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            amountField.requestFocus();
            return;
        }

        // Determine status based on amount
        String status;
        if (amount.compareTo(BigDecimal.ZERO) > 0) status = "Success";
        else if (amount.compareTo(BigDecimal.ZERO) < 0) status = "Failed";
        else status = "Pending";

        String type = (String) typeCombo.getSelectedItem();
        LocalDateTime date = LocalDateTime.parse(dateLabel.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Transaction t;
        if (existingTransaction == null) {
            // Add mode: generate new ID
            // In a real app, you'd get the next sequence from DB; here we simulate
            String newId = "TRX" + System.currentTimeMillis(); // simple unique
            t = new Transaction();
            t.setTransactionCode(newId);
        } else {
            t = existingTransaction;
        }
        t.setAccountNo(account);
        t.setType(type);
        t.setAmount(amount);
        t.setDate(date);
        t.setStatus(status);

        if (existingTransaction == null) {
            transactionDAO.insert(t);
        } else {
            transactionDAO.update(t);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}