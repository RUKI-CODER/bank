package bank.ui;

import bank.dao.AccountDAO;
import bank.dao.CustomerDAO;
import bank.dao.EmployeeDAO;
import bank.dao.TransactionDAO;
import bank.dao.impl.AccountDAOImpl;
import bank.dao.impl.CustomerDAOImpl;
import bank.dao.impl.EmployeeDAOImpl;
import bank.dao.impl.TransactionDAOImpl;
import bank.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class DashboardPanel extends JPanel {

    private static final Color BG          = new Color(244, 246, 250);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 110, 130);
    private static final Color CARD_WHITE  = Color.WHITE;
    private static final Color GREEN_SUCC  = new Color(22, 163, 74);
    private static final Color GREEN_SUCC_BG = new Color(220, 245, 230);
    private static final Color ORANGE_PEND = new Color(202, 138, 4);
    private static final Color ORANGE_PEND_BG = new Color(254, 243, 199);

    // DAO instances
    private EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private AccountDAO accountDAO = new AccountDAOImpl();
    private TransactionDAO transactionDAO = new TransactionDAOImpl();

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 30, 30, 30));
        center.add(buildCards(), BorderLayout.NORTH);
        center.add(buildTransactionsCard(), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 20, 30));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Welcome back, here's what's happening today.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(subtitle);

        JLabel admin = new JLabel(" Role:  Admin  ");
        admin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        admin.setForeground(TEXT_DARK);

        header.add(titleBox, BorderLayout.WEST);
        header.add(admin, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 20, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Use findAll().size() instead of count()
        long totalEmployees   = safeSize(() -> employeeDAO.findAll());
        long totalCustomers   = safeSize(() -> customerDAO.findAll());
        long totalAccounts    = safeSize(() -> accountDAO.findAll());
        long totalTransactions = safeSize(() -> transactionDAO.findAll());

        cards.add(new SummaryCard("Total Employees", String.valueOf(totalEmployees), "👥", new Color(52, 120, 246)));
        cards.add(new SummaryCard("Total Customers", String.valueOf(totalCustomers), "🧑‍💼", new Color(34, 197, 94)));
        cards.add(new SummaryCard("Total Accounts", String.valueOf(totalAccounts), "💳", new Color(147, 90, 224)));
        cards.add(new SummaryCard("Total Transactions", String.valueOf(totalTransactions), "💸", new Color(249, 115, 22)));

        return cards;
    }

    /**
     * Safely returns the size of a list obtained from a DAO method.
     * If the list is null or an exception occurs, returns 0.
     */
    private long safeSize(Supplier<List<?>> supplier) {
        try {
            List<?> list = supplier.get();
            return list == null ? 0 : list.size();
        } catch (Exception e) {
            e.printStackTrace(); // log the error
            return 0;
        }
    }

    // ---------- The rest (buildTransactionsCard, renderers, etc.) is unchanged ----------
    private JPanel buildTransactionsCard() {
        RoundedPanel wrap = new RoundedPanel(16, CARD_WHITE);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel cardTitle = new JLabel("Recent Transactions");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cardTitle.setForeground(TEXT_DARK);
        cardTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        wrap.add(cardTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Account No", "Type", "Amount", "Date", "Status"};

        List<Transaction> transactions = transactionDAO.findAll();
        int limit = Math.min(transactions.size(), 10);
        Object[][] data = new Object[limit][6];
        for (int i = 0; i < limit; i++) {
            Transaction t = transactions.get(i);
            data[i][0] = t.getTransactionCode();
            data[i][1] = t.getAccountNo();
            data[i][2] = t.getType();
            data[i][3] = currencyFormat.format(t.getAmount());
            data[i][4] = t.getDate().format(dateFormatter);
            data[i][5] = t.getStatus();
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(new Color(235, 241, 253));
        table.setFillsViewportHeight(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setBackground(new Color(248, 249, 252));
        tableHeader.setPreferredSize(new Dimension(0, 38));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 232, 238)));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 252));
                }
                return c;
            }
        };
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 1) {
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
        }
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private static class StatusBadgeRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        StatusBadgeRenderer() {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = String.valueOf(value);
            boolean success = "Success".equalsIgnoreCase(status);

            setText(status);
            setForeground(success ? GREEN_SUCC : ORANGE_PEND);
            setBackground(success ? GREEN_SUCC_BG : ORANGE_PEND_BG);
            setBorder(new EmptyBorder(6, 14, 6, 14));
            setOpaque(false);

            return new BadgeWrapper(this, getBackground());
        }
    }

    private static class BadgeWrapper extends JPanel {
        BadgeWrapper(JLabel label, Color bg) {
            setLayout(new GridBagLayout());
            setOpaque(false);
            RoundedLabelPanel pill = new RoundedLabelPanel(bg);
            pill.setLayout(new BorderLayout());
            pill.add(label, BorderLayout.CENTER);
            add(pill);
        }
    }

    private static class RoundedLabelPanel extends JPanel {
        private final Color bg;

        RoundedLabelPanel(Color bg) {
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SummaryCard extends JPanel {
        SummaryCard(String label, String value, String icon, Color color) {
            setLayout(new BorderLayout(10, 5));
            setBackground(CARD_WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true),
                    new EmptyBorder(18, 20, 18, 20)
            ));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valueLabel.setForeground(TEXT_DARK);

            JLabel labelLabel = new JLabel(label);
            labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            labelLabel.setForeground(TEXT_MUTED);

            JPanel left = new JPanel(new BorderLayout());
            left.setOpaque(false);
            left.add(iconLabel, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);
            right.add(valueLabel, BorderLayout.NORTH);
            right.add(labelLabel, BorderLayout.SOUTH);

            add(left, BorderLayout.WEST);
            add(right, BorderLayout.CENTER);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}