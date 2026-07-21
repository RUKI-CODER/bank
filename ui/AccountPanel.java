package bank.ui;

import bank.dao.AccountDAO;
import bank.dao.impl.AccountDAOImpl;
import bank.model.Account;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

/**
 * Accounts Page – full CRUD + pagination (10 rows per page).
 */
public class AccountPanel extends JPanel {

    // ---- Data ----
    private JTable accountsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel footerStatusLabel;
    private JPanel paginationPanel;

    private List<Account> fullAccountList;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    private final AccountDAO accountDAO = new AccountDAOImpl();

    // Column header changed to "Customer ID"
    private final String[] columnNames = {"Account No", "Customer ID", "Account Type", "Balance", "Status", "Actions"};

    // ---- Colors ----
    private static final Color PRIMARY_BLUE      = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK_BLUE = new Color(0, 64, 128);
    private static final Color BG_LIGHT_GREY    = new Color(248, 249, 250);
    private static final Color TABLE_HEADER_BG  = new Color(240, 242, 245);
    private static final Color WHITE            = Color.WHITE;
    private static final Color TEXT_DARK        = new Color(33, 37, 41);
    private static final Color TEXT_MUTED       = new Color(108, 117, 125);
    private static final Color BORDER_COLOR     = new Color(222, 226, 230);
    private static final Color DELETE_RED       = new Color(220, 53, 69);
    private static final Color EDIT_BLUE        = new Color(0, 123, 255);

    // ======================================================================
    // CONSTRUCTOR
    // ======================================================================
    public AccountPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return BigDecimal.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        buildUI();
        refreshFullList(accountDAO.findAll());
    }

    // ======================================================================
    // UI BUILDER
    // ======================================================================
    private void buildUI() {
        add(createHeaderBar(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 15));
        mainContent.setBackground(BG_LIGHT_GREY);

        mainContent.add(createControlsPanel(), BorderLayout.NORTH);
        mainContent.add(createTablePanel(), BorderLayout.CENTER);
        mainContent.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
    }

    // ======================================================================
    // HEADER BAR
    // ======================================================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT_GREY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Accounts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Account");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(PRIMARY_BLUE);
        addBtn.setForeground(WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setUI(new RoundedButtonUI(PRIMARY_BLUE, PRIMARY_DARK_BLUE));
        addBtn.addActionListener(e -> showAddAccountDialog());

        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    // ======================================================================
    // CONTROLS PANEL (SEARCH)
    // ======================================================================
    private JPanel createControlsPanel() {
        JPanel controls = new JPanel(new BorderLayout());
        controls.setBackground(BG_LIGHT_GREY);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.setBackground(BG_LIGHT_GREY);

        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setPreferredSize(new Dimension(280, 40));

        String placeholder = "Search account...";
        searchField.setText(placeholder);
        searchField.setForeground(TEXT_MUTED);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(TEXT_MUTED);
                }
            }
        });

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        searchIcon.setForeground(TEXT_MUTED);
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(WHITE);
        searchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        searchWrapper.add(searchIcon, BorderLayout.WEST);
        searchWrapper.add(searchField, BorderLayout.CENTER);
        searchWrapper.setOpaque(true);

        searchPanel.add(searchWrapper);
        controls.add(searchPanel, BorderLayout.EAST);

        searchField.addActionListener(e -> performSearch());

        return controls;
    }

    // ======================================================================
    // TABLE PANEL (unchanged)
    // ======================================================================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        accountsTable = new JTable(tableModel);
        accountsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountsTable.setRowHeight(50);
        accountsTable.setShowGrid(false);
        accountsTable.setIntercellSpacing(new Dimension(0, 0));
        accountsTable.setBackground(WHITE);
        accountsTable.setSelectionBackground(new Color(230, 242, 255));

        JTableHeader header = accountsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        accountsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        accountsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        accountsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        accountsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        accountsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        accountsTable.getColumnModel().getColumn(5).setPreferredWidth(180);

        accountsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        accountsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    setText(currencyFormat.format(value));
                } else {
                    super.setValue(value);
                }
            }
        });

        accountsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        accountsTable.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor());

        accountsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = accountsTable.rowAtPoint(e.getPoint());
                int col = accountsTable.columnAtPoint(e.getPoint());
                if (col == 5 && row >= 0) {
                    int x = e.getX() - accountsTable.getCellRect(row, col, true).x;
                    int cellWidth = accountsTable.getColumnModel().getColumn(col).getWidth();
                    if (x < cellWidth / 2) {
                        showEditDialog(row);
                    } else {
                        showDeleteConfirmation(row);
                    }
                }
            }
        });

        accountsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? WHITE : new Color(250, 251, 252));
                }
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(column == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ======================================================================
    // STATUS RENDERER
    // ======================================================================
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String status = value.toString();
                c.setForeground("Active".equalsIgnoreCase(status) ? new Color(0, 128, 0) : Color.RED);
            }
            return c;
        }
    }

    // ======================================================================
    // ACTION BUTTONS (RENDERER & EDITOR)
    // ======================================================================
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn;
        private final JButton deleteBtn;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));
            setOpaque(true);
            setBackground(WHITE);

            editBtn = new JButton("Edit");
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            editBtn.setBackground(EDIT_BLUE);
            editBtn.setForeground(WHITE);
            editBtn.setFocusPainted(false);
            editBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editBtn.setToolTipText("Edit this account");
            editBtn.setPreferredSize(new Dimension(70, 30));
            editBtn.setUI(new RoundedButtonUI(EDIT_BLUE, new Color(0, 80, 200)));

            deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setBackground(DELETE_RED);
            deleteBtn.setForeground(WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setToolTipText("Delete this account");
            deleteBtn.setPreferredSize(new Dimension(70, 30));
            deleteBtn.setUI(new RoundedButtonUI(DELETE_RED, new Color(180, 30, 50)));

            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? new Color(230, 242, 255) : (row % 2 == 0 ? WHITE : new Color(250, 251, 252)));
            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final ActionButtonRenderer renderer;

        public ActionButtonEditor() {
            renderer = new ActionButtonRenderer();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }
    }

    // ======================================================================
    // FOOTER (PAGINATION)
    // ======================================================================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        footerStatusLabel = new JLabel("Showing 0 entries");
        footerStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerStatusLabel.setForeground(TEXT_MUTED);
        footer.add(footerStatusLabel, BorderLayout.WEST);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setBackground(WHITE);
        footer.add(paginationPanel, BorderLayout.EAST);

        return footer;
    }

    // ======================================================================
    // PAGINATION LOGIC
    // ======================================================================

    private void displayPage(int page) {
        if (fullAccountList == null) fullAccountList = List.of();
        int total = fullAccountList.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages == 0) totalPages = 1;

        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        currentPage = page;

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);

        tableModel.setRowCount(0);
        for (int i = start; i < end; i++) {
            Account a = fullAccountList.get(i);
            tableModel.addRow(new Object[]{
                    a.getAccountNo(),
                    a.getCustomerCode(),
                    a.getAccountType(),
                    a.getBalance(),
                    a.getStatus(),
                    ""
            });
        }

        if (total == 0) {
            footerStatusLabel.setText("Showing 0 entries");
        } else {
            footerStatusLabel.setText("Showing " + (start + 1) + " to " + end + " of " + total + " entries");
        }

        updatePaginationButtons(totalPages);
    }

    private void updatePaginationButtons(int totalPages) {
        paginationPanel.removeAll();

        if (totalPages <= 1) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        JButton prev = createNavButton("<", currentPage > 0);
        prev.addActionListener(e -> displayPage(currentPage - 1));
        paginationPanel.add(prev);

        int maxVisible = 7;
        int half = maxVisible / 2;
        int startPage, endPage;

        if (totalPages <= maxVisible) {
            startPage = 0;
            endPage = totalPages - 1;
        } else {
            if (currentPage <= half) {
                startPage = 0;
                endPage = maxVisible - 1;
            } else if (currentPage >= totalPages - half - 1) {
                startPage = totalPages - maxVisible;
                endPage = totalPages - 1;
            } else {
                startPage = currentPage - half;
                endPage = currentPage + half;
            }
        }

        if (startPage > 0) {
            addPageButton(0);
            if (startPage > 1) addEllipsis();
        }

        for (int i = startPage; i <= endPage; i++) {
            if (i >= 0 && i < totalPages) {
                addPageButton(i);
            }
        }

        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) addEllipsis();
            addPageButton(totalPages - 1);
        }

        JButton next = createNavButton(">", currentPage < totalPages - 1);
        next.addActionListener(e -> displayPage(currentPage + 1));
        paginationPanel.add(next);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addPageButton(int page) {
        JButton btn = new JButton(String.valueOf(page + 1));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setBackground(WHITE);
        btn.setForeground(TEXT_DARK);

        if (page == currentPage) {
            btn.setBackground(PRIMARY_BLUE);
            btn.setForeground(WHITE);
            btn.setUI(new RoundedButtonUI(PRIMARY_BLUE, PRIMARY_BLUE));
            btn.setOpaque(false);
        } else {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(230, 240, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(WHITE);
                }
            });
        }

        btn.addActionListener(e -> displayPage(page));
        paginationPanel.add(btn);
    }

    private void addEllipsis() {
        JLabel ellipsis = new JLabel("...");
        ellipsis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ellipsis.setForeground(TEXT_MUTED);
        ellipsis.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        paginationPanel.add(ellipsis);
    }

    private JButton createNavButton(String label, boolean enabled) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        btn.setBackground(WHITE);
        btn.setForeground(enabled ? TEXT_DARK : TEXT_MUTED);
        btn.setEnabled(enabled);
        return btn;
    }

    // ======================================================================
    // HELPER UI: Rounded Button UI
    // ======================================================================
    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color normalColor;
        private final Color pressedColor;

        public RoundedButtonUI(Color normal, Color pressed) {
            this.normalColor = normal;
            this.pressedColor = pressed;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            Color bg = model.isPressed() ? pressedColor : normalColor;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 8, 8));
            g2.dispose();
            super.paint(g, c);
        }
    }

    // ======================================================================
    // DATA OPERATIONS
    // ======================================================================

    private void refreshFullList(List<Account> newList) {
        fullAccountList = newList;
        currentPage = 0;
        displayPage(0);
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || query.equals("Search account...")) {
            refreshFullList(accountDAO.findAll());
        } else {
            refreshFullList(accountDAO.search(query));
        }
    }

    // ======================================================================
    // ADD ACCOUNT – now uses separate AddAccountDialog
    // ======================================================================
    private void showAddAccountDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddAccountDialog dialog = new AddAccountDialog(parent, accountDAO);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshFullList(accountDAO.findAll());
        }
    }

    // ======================================================================
    // EDIT ACCOUNT
    // ======================================================================
    private void showEditDialog(int row) {
        String accNo = (String) tableModel.getValueAt(row, 0);
        Account accountToEdit = fullAccountList.stream()
                .filter(a -> a.getAccountNo().equals(accNo))
                .findFirst()
                .orElse(null);
        if (accountToEdit == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Account", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(new JLabel("Account No:"));
        JLabel accNoLabel = new JLabel(accNo);
        accNoLabel.setFont(accNoLabel.getFont().deriveFont(Font.BOLD));
        form.add(accNoLabel);

        form.add(new JLabel("Customer ID:"));
        JTextField custCodeField = new JTextField(accountToEdit.getCustomerCode());
        form.add(custCodeField);

        form.add(new JLabel("Account Type:"));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Savings", "Checking"});
        typeCombo.setSelectedItem(accountToEdit.getAccountType());
        form.add(typeCombo);

        form.add(new JLabel("Balance:"));
        JTextField balanceField = new JTextField(accountToEdit.getBalance().toString());
        form.add(balanceField);

        form.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(accountToEdit.getStatus());
        form.add(statusCombo);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                String newCustCode = custCodeField.getText().trim();
                String newType = (String) typeCombo.getSelectedItem();
                BigDecimal newBalance = new BigDecimal(balanceField.getText().trim());
                String newStatus = (String) statusCombo.getSelectedItem();

                if (newCustCode.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Customer ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Account updated = new Account(accNo, newCustCode, newType, newBalance, newStatus);
                accountDAO.update(updated);
                dialog.dispose();
                refreshFullList(accountDAO.findAll());
                JOptionPane.showMessageDialog(this, "Account updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid balance format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ======================================================================
    // DELETE ACCOUNT
    // ======================================================================
    private void showDeleteConfirmation(int row) {
        String accNo = (String) tableModel.getValueAt(row, 0);
        String custCode = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete account " + accNo + " (" + custCode + ")?",
                "Delete Account",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            accountDAO.delete(accNo);
            refreshFullList(accountDAO.findAll());
            JOptionPane.showMessageDialog(this, "Account deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}