package bank.ui;

import bank.dao.TransactionDAO;
import bank.dao.impl.TransactionDAOImpl;
import bank.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionPanel extends JPanel {

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TransactionDAO transactionDAO;

    // Pagination state
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;   // 10 rows per page
    private int totalRecords = 0;
    private List<Transaction> currentDataList = new ArrayList<>();

    // Footer UI components
    private JLabel statusLabel;
    private JPanel paginationPanel;

    // Colors
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
    private static final Color DARK_GREEN       = new Color(0, 100, 0);
    private static final Color DARK_RED         = new Color(139, 0, 0);

    public TransactionPanel() {
        transactionDAO = new TransactionDAOImpl();
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        buildUI();
        loadTableData();
    }

    private void buildUI() {
        add(createHeaderBar(), BorderLayout.NORTH);
        JPanel mainContent = new JPanel(new BorderLayout(0, 15));
        mainContent.setBackground(BG_LIGHT_GREY);
        mainContent.add(createControlsPanel(), BorderLayout.NORTH);
        mainContent.add(createTablePanel(), BorderLayout.CENTER);
        mainContent.add(createFooterPanel(), BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);
    }

    // ─── Header ──────────────────────────────────────────────────────────────

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT_GREY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Transaction");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(PRIMARY_BLUE);
        addBtn.setForeground(WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setOpaque(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setUI(new RoundedButtonUI(PRIMARY_BLUE, PRIMARY_DARK_BLUE));

        addBtn.addActionListener(e -> {
            AddTransactionDialog dialog = new AddTransactionDialog(
                    SwingUtilities.getWindowAncestor(this),
                    null
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
                JOptionPane.showMessageDialog(this,
                        "New transaction added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    // ─── Search Bar ──────────────────────────────────────────────────────────

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

        String placeholder = "Search transaction...";
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

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
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
        return controls;
    }

    // ─── Table ───────────────────────────────────────────────────────────────

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] columns = {"ID", "Account No", "Type", "Amount", "Date", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Object getValueAt(int row, int column) {
                if (column == 6) return "";
                return super.getValueAt(row, column);
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTable.setRowHeight(50);
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));
        transactionTable.setBackground(WHITE);
        transactionTable.setSelectionBackground(new Color(230, 242, 255));

        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        // Column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        // Custom renderers
        transactionTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyCellRenderer());
        transactionTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        transactionTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        transactionTable.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        // Mouse listener for Edit/Delete clicks
        transactionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = transactionTable.rowAtPoint(e.getPoint());
                int col = transactionTable.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    int x = e.getX() - transactionTable.getCellRect(row, col, true).x;
                    int cellWidth = transactionTable.getColumnModel().getColumn(col).getWidth();
                    if (x < cellWidth / 2) {
                        showEditDialog(row);
                    } else {
                        showDeleteConfirmation(row);
                    }
                }
            }
        });

        // Alternating row colors
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                    label.setHorizontalAlignment(column == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ─── Cell Renderers ──────────────────────────────────────────────────────

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String status = (String) value;
            if ("Success".equalsIgnoreCase(status)) {
                label.setForeground(DARK_GREEN);
                label.setText("● Success");
            } else if ("Pending".equalsIgnoreCase(status)) {
                label.setForeground(new Color(241, 196, 15));
                label.setText("● Pending");
            } else if ("Failed".equalsIgnoreCase(status)) {
                label.setForeground(DARK_RED);
                label.setText("● Failed");
            } else {
                label.setForeground(TEXT_DARK);
            }
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? WHITE : new Color(250, 251, 252));
            } else {
                label.setBackground(table.getSelectionBackground());
            }
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            return label;
        }
    }

    private class CurrencyCellRenderer extends DefaultTableCellRenderer {
        private final NumberFormat currencyFormat;

        public CurrencyCellRenderer() {
            currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof BigDecimal) {
                String formatted = currencyFormat.format(((BigDecimal) value).doubleValue());
                return super.getTableCellRendererComponent(table, formatted, isSelected, hasFocus, row, column);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn, deleteBtn;

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
            editBtn.setToolTipText("Edit this transaction");
            editBtn.setPreferredSize(new Dimension(70, 30));
            editBtn.setOpaque(false);
            editBtn.setContentAreaFilled(false);
            editBtn.setUI(new RoundedButtonUI(EDIT_BLUE, new Color(0, 80, 200)));

            deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setBackground(DELETE_RED);
            deleteBtn.setForeground(WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setToolTipText("Delete this transaction");
            deleteBtn.setPreferredSize(new Dimension(70, 30));
            deleteBtn.setOpaque(false);
            deleteBtn.setContentAreaFilled(false);
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
        public Object getCellEditorValue() { return null; }
        @Override
        public boolean isCellEditable(EventObject anEvent) { return true; }
        @Override
        public boolean shouldSelectCell(EventObject anEvent) { return true; }
    }

    // ─── Footer (Pagination) ────────────────────────────────────────────────

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_MUTED);
        footer.add(statusLabel, BorderLayout.WEST);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setBackground(WHITE);
        footer.add(paginationPanel, BorderLayout.EAST);

        return footer;
    }

    // ─── UPDATED PAGINATION WITH ALWAYS-VISIBLE < AND > ────────────────────

    private void refreshFooter() {
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        final int totalPagesFinal = totalPages;

        // Update status label
        int start = (currentPage - 1) * PAGE_SIZE + 1;
        int end = Math.min(currentPage * PAGE_SIZE, totalRecords);
        if (totalRecords == 0) {
            statusLabel.setText("No entries");
        } else {
            statusLabel.setText(String.format("Showing %d to %d of %d entries", start, end, totalRecords));
        }

        paginationPanel.removeAll();

        // ── Previous page button (always visible) ──
        JButton prevBtn = new JButton("<");
        prevBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        prevBtn.setFocusPainted(false);
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        prevBtn.setBackground(WHITE);
        prevBtn.setForeground(TEXT_MUTED);
        prevBtn.setEnabled(currentPage > 1);
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) displayPage(currentPage - 1);
        });
        paginationPanel.add(prevBtn);

        // ── Page number block (max 3 pages) ──
        int blockStart = ((currentPage - 1) / 3) * 3 + 1;
        int blockEnd = Math.min(blockStart + 2, totalPagesFinal);

        for (int i = blockStart; i <= blockEnd; i++) {
            addPageButton(i);
        }

        // ── Next page button (always visible) ──
        JButton nextBtn = new JButton(">");
        nextBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        nextBtn.setBackground(WHITE);
        nextBtn.setForeground(TEXT_MUTED);
        nextBtn.setEnabled(currentPage < totalPagesFinal);
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPagesFinal) displayPage(currentPage + 1);
        });
        paginationPanel.add(nextBtn);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addPageButton(int page) {
        JButton btn = new JButton(String.valueOf(page));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        if (page == currentPage) {
            btn.setBackground(PRIMARY_BLUE);
            btn.setForeground(WHITE);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setUI(new RoundedButtonUI(PRIMARY_BLUE, PRIMARY_BLUE));
        } else {
            btn.setBackground(WHITE);
            btn.setForeground(TEXT_DARK);
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

    // ─── Rounded Button UI ──────────────────────────────────────────────────

    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color normalColor, pressedColor;

        public RoundedButtonUI(Color normal, Color pressed) {
            this.normalColor = normal;
            this.pressedColor = pressed;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AbstractButton b = (AbstractButton) c;
            Color bg = b.getModel().isPressed() ? pressedColor : normalColor;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 8, 8));
            g2.dispose();
            super.paint(g, c);
        }
    }

    // ─── Data Loading & Pagination ──────────────────────────────────────────

    private void loadTableData() {
        currentDataList = transactionDAO.findAll();
        totalRecords = currentDataList.size();
        currentPage = 1;
        displayPage(1);
        refreshFooter();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() || keyword.equals("Search transaction...")) {
            loadTableData();
        } else {
            currentDataList = transactionDAO.search(keyword);
            totalRecords = currentDataList.size();
            currentPage = 1;
            displayPage(1);
            refreshFooter();
        }
    }

    private void displayPage(int page) {
        if (page < 1) page = 1;
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;
        currentPage = page;

        tableModel.setRowCount(0);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, currentDataList.size());
        for (int i = start; i < end; i++) {
            Transaction t = currentDataList.get(i);
            tableModel.addRow(new Object[]{
                    t.getTransactionCode(),
                    t.getAccountNo(),
                    t.getType(),
                    t.getAmount(),
                    t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    t.getStatus(),
                    ""
            });
        }
        refreshFooter();
    }

    // ─── Edit & Delete ──────────────────────────────────────────────────────

    private void showEditDialog(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= currentDataList.size()) return;

        Transaction transaction = currentDataList.get(globalIndex);
        AddTransactionDialog dialog = new AddTransactionDialog(
                SwingUtilities.getWindowAncestor(this),
                transaction
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
            JOptionPane.showMessageDialog(this,
                    "Transaction " + transaction.getTransactionCode() + " updated successfully.",
                    "Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDeleteConfirmation(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= currentDataList.size()) return;

        Transaction t = currentDataList.get(globalIndex);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete transaction " + t.getTransactionCode() + "?",
                "Delete Transaction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            transactionDAO.delete(t.getTransactionCode());
            loadTableData();
            JOptionPane.showMessageDialog(this,
                    "Transaction " + t.getTransactionCode() + " deleted successfully.",
                    "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}