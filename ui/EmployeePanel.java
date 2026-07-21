package bank.ui;

import bank.dao.EmployeeDAO;
import bank.dao.impl.EmployeeDAOImpl;
import bank.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;

public class EmployeePanel extends JPanel {

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private EmployeeDAO employeeDAO;

    // Pagination state
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private int totalRecords = 0;
    private List<Employee> currentDataList = new ArrayList<>();

    // Footer UI components
    private JLabel statusLabel;
    private JPanel paginationPanel;

    // Colors (unchanged)
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

    public EmployeePanel() {
        employeeDAO = new EmployeeDAOImpl();
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

        JLabel title = new JLabel("Employees");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Employee");
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
            AddEmployeeDialog dialog = new AddEmployeeDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this)
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                Employee newEmp = new Employee();
                newEmp.setEmployeeCode(String.format("EMP%03d", tableModel.getRowCount() + 1));
                newEmp.setName(dialog.getFullName());
                newEmp.setUsername(dialog.getUsername());
                newEmp.setPassword(dialog.getPassword());
                newEmp.setPhone(dialog.getPhone());
                newEmp.setStatus(dialog.getStatus());
                employeeDAO.insert(newEmp);
                loadTableData();
                JOptionPane.showMessageDialog(this,
                        "New employee added successfully!",
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

        String placeholder = "Search employee...";
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

        String[] columns = {"ID", "Name", "Username", "Phone", "Password", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Object getValueAt(int row, int column) {
                if (column == 6) return "";
                return super.getValueAt(row, column);
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setRowHeight(50);
        employeeTable.setShowGrid(false);
        employeeTable.setIntercellSpacing(new Dimension(0, 0));
        employeeTable.setBackground(WHITE);
        employeeTable.setSelectionBackground(new Color(230, 242, 255));

        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        employeeTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        employeeTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        employeeTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        employeeTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        employeeTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        employeeTable.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = employeeTable.rowAtPoint(e.getPoint());
                int col = employeeTable.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    int x = e.getX() - employeeTable.getCellRect(row, col, true).x;
                    int cellWidth = employeeTable.getColumnModel().getColumn(col).getWidth();
                    if (x < cellWidth / 2) {
                        showEditDialog(row);
                    } else {
                        showDeleteConfirmation(row);
                    }
                }
            }
        });

        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(employeeTable);
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
            if ("Active".equalsIgnoreCase(status)) label.setForeground(DARK_GREEN);
            else if ("Inactive".equalsIgnoreCase(status)) label.setForeground(DARK_RED);
            else label.setForeground(TEXT_DARK);
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? WHITE : new Color(250, 251, 252));
            } else {
                label.setBackground(table.getSelectionBackground());
            }
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            return label;
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
            editBtn.setToolTipText("Edit this employee");
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
            deleteBtn.setToolTipText("Delete this employee");
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

    /**
     * Refreshes the pagination controls and status label.
     * Uses a final copy of totalPages to satisfy lambda requirements.
     */
    private void refreshFooter() {
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        final int totalPagesFinal = totalPages;   // final for lambdas

        // Update status
        int start = (currentPage - 1) * PAGE_SIZE + 1;
        int end = Math.min(currentPage * PAGE_SIZE, totalRecords);
        if (totalRecords == 0) {
            statusLabel.setText("No entries");
        } else {
            statusLabel.setText(String.format("Showing %d to %d of %d entries", start, end, totalRecords));
        }

        paginationPanel.removeAll();

        // Previous
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

        // Page numbers
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

        // Next (uses totalPagesFinal)
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

        // page is effectively final here
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
        currentDataList = employeeDAO.findAll();
        totalRecords = currentDataList.size();
        currentPage = 1;
        displayPage(1);
        refreshFooter();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() || keyword.equals("Search employee...")) {
            loadTableData();
        } else {
            currentDataList = employeeDAO.search(keyword);
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
            Employee e = currentDataList.get(i);
            tableModel.addRow(new Object[]{
                    e.getEmployeeCode(),
                    e.getName(),
                    e.getUsername(),
                    e.getPhone(),
                    e.getPassword(),
                    e.getStatus(),
                    ""
            });
        }
        refreshFooter(); // update button highlights and status
    }

    // ─── Edit & Delete ──────────────────────────────────────────────────────

    private void showEditDialog(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= currentDataList.size()) return;

        Employee emp = currentDataList.get(globalIndex);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Employee", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(440, 400);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(new JLabel("ID:"));
        JLabel idLabel = new JLabel(emp.getEmployeeCode());
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD));
        form.add(idLabel);

        form.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(emp.getName());
        form.add(nameField);

        form.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(emp.getUsername());
        form.add(usernameField);

        form.add(new JLabel("Phone:"));
        JTextField phoneField = new JTextField(emp.getPhone());
        form.add(phoneField);

        form.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(emp.getPassword());
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        passwordField.setBackground(Color.WHITE);
        passwordField.setEchoChar('*');

        JLabel eyeLabel = new JLabel("👁");
        eyeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        eyeLabel.setForeground(TEXT_MUTED);
        eyeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        boolean[] passwordVisible = {false};
        eyeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passwordVisible[0] = !passwordVisible[0];
                passwordField.setEchoChar(passwordVisible[0] ? (char) 0 : '*');
                passwordField.requestFocusInWindow();
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
        form.add(passwordWrapper);

        form.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(emp.getStatus());
        form.add(statusCombo);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            Employee updated = new Employee();
            updated.setEmployeeCode(emp.getEmployeeCode());
            updated.setName(nameField.getText().trim());
            updated.setUsername(usernameField.getText().trim());
            updated.setPhone(phoneField.getText().trim());
            updated.setPassword(new String(passwordField.getPassword()));
            updated.setStatus((String) statusCombo.getSelectedItem());
            employeeDAO.update(updated);
            loadTableData(); // reload and reset to page 1
            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                    "Employee " + emp.getEmployeeCode() + " updated successfully.",
                    "Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showDeleteConfirmation(int row) {
        int globalIndex = (currentPage - 1) * PAGE_SIZE + row;
        if (globalIndex >= currentDataList.size()) return;

        Employee emp = currentDataList.get(globalIndex);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete employee " + emp.getName() + " (" + emp.getEmployeeCode() + ")?",
                "Delete Employee",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            employeeDAO.delete(emp.getEmployeeCode());
            loadTableData();
            JOptionPane.showMessageDialog(this,
                    "Employee " + emp.getEmployeeCode() + " deleted successfully.",
                    "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}