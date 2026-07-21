package bank.ui;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import bank.dao.CustomerDAO;
import bank.dao.impl.CustomerDAOImpl;
import bank.model.Customer;

/**
 * CustomersPanel – a self-contained UI component for managing customer records.
 * Pagination logic matches EmployeePanel exactly.
 */
public class CustomersPanel extends JPanel {

    // ---- Data ----
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;

    // ---- DAO ----
    private CustomerDAO customerDAO;

    // ---- Pagination (now 1-based, like EmployeePanel) ----
    private List<Customer> allCustomers = new ArrayList<>();
    private int currentPage = 1;          // 1‑based
    private static final int PAGE_SIZE = 10;
    private int totalRecords = 0;
    private JPanel paginationPanel;
    private JPanel footerPanel;

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
    // CONSTRUCTORS
    // ======================================================================
    public CustomersPanel() {
        this(new CustomerDAOImpl());
    }

    public CustomersPanel(CustomerDAO dao) {
        this.customerDAO = dao;
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        buildUI();
        loadTableData();
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

        JLabel title = new JLabel("Customers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Customer");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(PRIMARY_BLUE);
        addBtn.setForeground(WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setUI(new RoundedButtonUI(PRIMARY_BLUE, PRIMARY_DARK_BLUE));

        addBtn.addActionListener(e -> {
            AddCustomerDialog dialog = new AddCustomerDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this)
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                String name    = dialog.getFullName();
                String phone   = dialog.getPhone();
                String address = dialog.getAddress();

                String newId = String.format("CUS%03d", totalRecords + 1);

                Customer c = new Customer();
                c.setCustomerCode(newId);
                c.setName(name);
                c.setPhone(phone);
                c.setAddress(address);
                customerDAO.insert(c);

                loadTableData();
                JOptionPane.showMessageDialog(this,
                        "New customer added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

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

        String placeholder = "Search customer...";
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

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { performSearch(); }
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

    // ======================================================================
    // TABLE PANEL
    // ======================================================================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] columns = {"ID", "Name", "Phone", "Address", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        customerTable.setRowHeight(50);
        customerTable.setShowGrid(false);
        customerTable.setIntercellSpacing(new Dimension(0, 0));
        customerTable.setBackground(WHITE);
        customerTable.setSelectionBackground(new Color(230, 242, 255));

        JTableHeader header = customerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        customerTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(180);

        customerTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());

        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = customerTable.rowAtPoint(e.getPoint());
                int col = customerTable.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    int x = e.getX() - customerTable.getCellRect(row, col, true).x;
                    int cellWidth = customerTable.getColumnModel().getColumn(col).getWidth();
                    if (x < cellWidth / 2) {
                        showEditDialog(row);
                    } else {
                        showDeleteConfirmation(row);
                    }
                }
            }
        });

        customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                    label.setHorizontalAlignment(column == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ======================================================================
    // ACTION BUTTON RENDERER (visual only)
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
            editBtn.setToolTipText("Edit this customer");
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
            deleteBtn.setToolTipText("Delete this customer");
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

    // ======================================================================
    // FOOTER (PAGINATION) – matches EmployeePanel layout
    // ======================================================================
    private JPanel createFooterPanel() {
        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        statusLabel = new JLabel("No entries");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_MUTED);
        footerPanel.add(statusLabel, BorderLayout.WEST);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setBackground(WHITE);
        paginationPanel.setMinimumSize(new Dimension(300, 40));
        paginationPanel.setPreferredSize(new Dimension(400, 40));
        footerPanel.add(paginationPanel, BorderLayout.EAST);

        return footerPanel;
    }

    // ======================================================================
    // PAGINATION LOGIC – exact copy from EmployeePanel
    // ======================================================================

    private void loadTableData() {
        allCustomers = customerDAO.findAll();
        totalRecords = allCustomers.size();
        currentPage = 1;
        displayPage(1);
        refreshFooter();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() || keyword.equals("Search customer...")) {
            loadTableData();
            return;
        }
        allCustomers = customerDAO.search(keyword);
        totalRecords = allCustomers.size();
        currentPage = 1;
        displayPage(1);
        refreshFooter();
    }

    private void displayPage(int page) {
        if (page < 1) page = 1;
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;
        currentPage = page;

        tableModel.setRowCount(0);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allCustomers.size());
        for (int i = start; i < end; i++) {
            Customer c = allCustomers.get(i);
            tableModel.addRow(new Object[]{
                    c.getCustomerCode(),
                    c.getName(),
                    c.getPhone(),
                    c.getAddress()
            });
        }
        refreshFooter(); // update status and buttons
    }

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

        // Previous button
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

        // Page numbers – exactly like EmployeePanel
        if (totalPagesFinal <= 5) {
            for (int i = 1; i <= totalPagesFinal; i++) {
                addPageButton(i);
            }
        } else {
            addPageButton(1);
            if (currentPage > 3) addEllipsis();
            int startPage = Math.max(2, currentPage - 1);
            int endPage = Math.min(totalPagesFinal - 1, currentPage + 1);
            for (int i = startPage; i <= endPage; i++) {
                addPageButton(i);
            }
            if (currentPage < totalPagesFinal - 2) addEllipsis();
            if (totalPagesFinal > 1) addPageButton(totalPagesFinal);
        }

        // Next button
        JButton nextBtn = new JButton(">");
        nextBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        nextBtn.setBackground(WHITE);
        nextBtn.setForeground(TEXT_MUTED);
        nextBtn.setEnabled(currentPage < totalPagesFinal);
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPagesFinal) {
                displayPage(currentPage + 1);
            }
        });
        paginationPanel.add(nextBtn);

        paginationPanel.revalidate();
        paginationPanel.repaint();
        footerPanel.revalidate();
        footerPanel.repaint();
    }

    // FIXED: page buttons now properly visible
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
            btn.setOpaque(true);                 // ensure background is painted
            // DO NOT set UI to null – let the default L&F handle painting
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
    // EDIT & DELETE LOGIC (unchanged)
    // ======================================================================

    private void showEditDialog(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= allCustomers.size()) return;

        Customer selected = allCustomers.get(globalIndex);
        final Customer selectedFinal = selected;

        String id      = selectedFinal.getCustomerCode();
        String name    = selectedFinal.getName();
        String phone   = selectedFinal.getPhone();
        String address = selectedFinal.getAddress();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Customer", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(new JLabel("ID:"));
        JLabel idLabel = new JLabel(id);
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD));
        form.add(idLabel);

        form.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(name);
        form.add(nameField);

        form.add(new JLabel("Phone:"));
        JTextField phoneField = new JTextField(phone);
        form.add(phoneField);

        form.add(new JLabel("Address:"));
        JTextField addressField = new JTextField(address);
        form.add(addressField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            selectedFinal.setName(nameField.getText().trim());
            selectedFinal.setPhone(phoneField.getText().trim());
            selectedFinal.setAddress(addressField.getText().trim());
            customerDAO.update(selectedFinal);
            loadTableData(); // reload and reset to page 1
            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                    "Customer " + id + " updated successfully.",
                    "Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showDeleteConfirmation(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= allCustomers.size()) return;

        Customer c = allCustomers.get(globalIndex);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete customer " + c.getName() + " (" + c.getCustomerCode() + ")?",
                "Delete Customer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            customerDAO.delete(c.getCustomerCode());
            loadTableData();
            JOptionPane.showMessageDialog(this,
                    "Customer " + c.getCustomerCode() + " deleted successfully.",
                    "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}