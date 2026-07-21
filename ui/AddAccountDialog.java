package bank.ui;

import bank.dao.AccountDAO;
import bank.dao.CustomerDAO;          // assume you have this DAO
import bank.dao.impl.CustomerDAOImpl; // assume implementation exists
import bank.model.Customer;
import bank.model.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Modal dialog for adding a new account.
 * Account number is auto-generated; customer is selected by name.
 */
public class AddAccountDialog extends JDialog {
    private JComboBox<String> customerCombo;
    private JComboBox<String> typeCombo;
    private JTextField balanceField;
    private JComboBox<String> statusCombo;
    private boolean saved = false;

    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private Map<String, String> customerNameToCode; // name -> customer_code

    public AddAccountDialog(JFrame parent, AccountDAO accountDAO) {
        super(parent, "Add Account", true);
        this.accountDAO = accountDAO;
        setSize(500, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Load customers from database
        loadCustomers();

        // ---------- Header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(15, 20, 10, 20));
        header.setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Add Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorder(null);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        header.add(closeBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ---------- Form Panel ----------
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Customer (name)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel customerLabel = new JLabel("Customer");
        customerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(customerLabel, gbc);

        gbc.gridy = 1;
        customerCombo = new JComboBox<>();
        customerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Populate with customer names
        for (String name : customerNameToCode.keySet()) {
            customerCombo.addItem(name);
        }
        if (customerCombo.getItemCount() > 0) {
            customerCombo.setSelectedIndex(0);
        }
        formPanel.add(customerCombo, gbc);

        // Row 2: Account Type
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel typeLabel = new JLabel("Account Type");
        formPanel.add(typeLabel, gbc);

        gbc.gridy = 3;
        typeCombo = new JComboBox<>(new String[]{"Savings", "Checking"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(typeCombo, gbc);

        // Row 4: Initial Balance & Status (2 cols)
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel balanceLabel = new JLabel("Initial Balance");
        formPanel.add(balanceLabel, gbc);

        gbc.gridx = 1;
        JLabel statusLabel = new JLabel("Status");
        formPanel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        balanceField = new JTextField("0.00");
        balanceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceField.setForeground(Color.BLACK);
        formPanel.add(balanceField, gbc);

        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(statusCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ---------- Footer ----------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 20, 15, 20));
        footer.setBackground(new Color(240, 242, 245));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(0, 102, 204));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        saveBtn.addActionListener(e -> {
            if (validateForm()) {
                // Generate next account number
                String newAccNo = generateNextAccountNumber();
                // Get selected customer code
                String selectedName = (String) customerCombo.getSelectedItem();
                String customerCode = customerNameToCode.get(selectedName);
                String type = (String) typeCombo.getSelectedItem();
                BigDecimal balance = new BigDecimal(balanceField.getText().trim());
                String status = (String) statusCombo.getSelectedItem();

                Account newAccount = new Account(newAccNo, customerCode, type, balance, status);
                accountDAO.insert(newAccount);
                saved = true;
                JOptionPane.showMessageDialog(this, "Account " + newAccNo + " added successfully!");
                dispose();
            }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);

        // ESC to close
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ---- Load customers from DB ----
    private void loadCustomers() {
        customerNameToCode = new HashMap<>();
        List<Customer> customers = customerDAO.findAll();
        for (Customer c : customers) {
            customerNameToCode.put(c.getName(), c.getCustomerCode());
        }
        // If no customers, add a placeholder
        if (customerNameToCode.isEmpty()) {
            customerNameToCode.put("No customers found", "NONE");
        }
    }

    // ---- Generate next account number ----
    private String generateNextAccountNumber() {
        // Query the max account_no from the database
        String maxAccNo = accountDAO.findMaxAccountNumber(); // we need to add this method
        // For now, let's implement it directly using a helper or add method to AccountDAO
        // I'll add a default implementation here (using a simple query)
        // Alternatively, we can call a method we'll add to AccountDAO.
        // Let's assume we have a method: accountDAO.getNextAccountNumber()
        // For simplicity, we'll implement it inline:
        String prefix = "ACC";
        int nextNumber = 1;
        try {
            // Use the DAO method if available
            String last = accountDAO.findMaxAccountNumber(); // returns "ACC125" or null
            if (last != null && last.startsWith(prefix)) {
                String numPart = last.substring(prefix.length());
                nextNumber = Integer.parseInt(numPart) + 1;
            }
        } catch (Exception ex) {
            // fallback
        }
        // Pad to 3 digits (e.g., 1 -> 001)
        return prefix + String.format("%03d", nextNumber);
    }

    // ---- Validation (unchanged) ----
    private boolean validateForm() {
        if (customerCombo.getSelectedIndex() < 0 || customerCombo.getSelectedItem().equals("No customers found")) {
            JOptionPane.showMessageDialog(this, "Please select a valid customer.");
            return false;
        }
        if (typeCombo.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account type.");
            return false;
        }
        String balanceText = balanceField.getText().trim();
        if (balanceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an initial balance.");
            return false;
        }
        try {
            double bal = Double.parseDouble(balanceText);
            if (bal < 0) {
                JOptionPane.showMessageDialog(this, "Balance cannot be negative.");
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric balance.");
            return false;
        }
        return true;
    }

    public boolean isSaved() {
        return saved;
    }
}