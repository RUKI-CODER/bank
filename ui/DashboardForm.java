package bank.ui;

import bank.dao.AdminDAO;
import bank.model.Admin;

import javax.swing.*;
import java.awt.*;

public class DashboardForm extends JFrame
        implements SidebarPanel.NavigationListener {

    private CardLayout cardLayout;
    private JPanel centerContainer;

    // Store references to pass to the ChangePasswordPanel
    private Admin currentAdmin;
    private AdminDAO adminDAO;

    // Constructor now requires the logged-in admin and DAO
    public DashboardForm(Admin currentAdmin, AdminDAO adminDAO) {
        this.currentAdmin = currentAdmin;
        this.adminDAO = adminDAO;

        setTitle("Bank Management System");
        setSize(1400, 800);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        SidebarPanel sidebar = new SidebarPanel();
        sidebar.addNavigationListener(this);
        add(sidebar, BorderLayout.WEST);

        centerContainer = new JPanel();
        cardLayout = new CardLayout();
        centerContainer.setLayout(cardLayout);

        // Add panels – card names must match the sidebar text exactly
        centerContainer.add(new DashboardPanel(), "Dashboard");
        centerContainer.add(new EmployeePanel(), "Employees");
        centerContainer.add(new CustomersPanel(), "Customers");
        centerContainer.add(new AccountPanel(), "Accounts");
        centerContainer.add(new TransactionPanel(), "Transactions");

        // Pass the admin and DAO to ChangePasswordPanel
        centerContainer.add(new ChangePasswordPanel(currentAdmin, adminDAO), "Change Password");

        add(centerContainer, BorderLayout.CENTER);

        // Show Dashboard by default
        cardLayout.show(centerContainer, "Dashboard");

        setVisible(true);
    }

    @Override
    public void onNavigate(String target) {
        cardLayout.show(centerContainer, target);
    }
}