package bank.ui;

import javax.swing.SwingUtilities;

public class BankApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}     