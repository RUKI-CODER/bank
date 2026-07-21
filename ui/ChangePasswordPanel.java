package bank.ui;

import bank.dao.AdminDAO;
import bank.model.Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChangePasswordPanel extends JPanel {

    private JPasswordField currentPass, newPass, confirmPass;
    private JButton changeBtn;

    // References to the logged-in admin and DAO
    private Admin currentAdmin;
    private AdminDAO adminDAO;

    private static final Color NAVY = new Color(30, 45, 74);
    private static final Color ACCENT_BLUE = new Color(74, 130, 216);
    private static final Color FIELD_BG = new Color(243, 245, 248);
    private static final Color LABEL_GRAY = new Color(90, 100, 115);
    private static final Color BORDER_GRAY = new Color(220, 224, 230);

    // Constructor now requires admin and DAO
    public ChangePasswordPanel(Admin admin, AdminDAO adminDAO) {
        this.currentAdmin = admin;
        this.adminDAO = adminDAO;

        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        JPanel formPanel = buildFormPanel();
        formPanel.setPreferredSize(new Dimension(430, 460));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(formPanel, gbc);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(null);

        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);  // fixed: was NAVY.BLUE
        title.setBounds(30, 25, 300, 30);
        form.add(title);

        // ... (all other UI components remain the same)

        // Labels and fields as in your original code...
        JLabel currentLbl = new JLabel("Current Password");
        currentLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        currentLbl.setForeground(LABEL_GRAY);
        currentLbl.setBounds(30, 75, 250, 20);
        form.add(currentLbl);

        currentPass = new JPasswordField();
        currentPass.setEchoChar('•');
        stylePasswordField(currentPass);
        currentPass.setBounds(30, 98, 340, 38);
        form.add(currentPass);
        addEyeToggle(form, currentPass, 335, 108);

        JLabel newLbl = new JLabel("New Password");
        newLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        newLbl.setForeground(LABEL_GRAY);
        newLbl.setBounds(30, 150, 250, 20);
        form.add(newLbl);

        newPass = new JPasswordField();
        newPass.setEchoChar('•');
        stylePasswordField(newPass);
        newPass.setBounds(30, 173, 340, 38);
        form.add(newPass);
        addEyeToggle(form, newPass, 335, 183);

        JLabel confirmLbl = new JLabel("Confirm New Password");
        confirmLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        confirmLbl.setForeground(LABEL_GRAY);
        confirmLbl.setBounds(30, 225, 250, 20);
        form.add(confirmLbl);

        confirmPass = new JPasswordField();
        confirmPass.setEchoChar('•');
        stylePasswordField(confirmPass);
        confirmPass.setBounds(30, 248, 340, 38);
        form.add(confirmPass);
        addEyeToggle(form, confirmPass, 335, 258);

        changeBtn = new JButton("Change Password");
        changeBtn.setBackground(ACCENT_BLUE);
        changeBtn.setForeground(Color.WHITE);
        changeBtn.setFocusPainted(false);
        changeBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        changeBtn.setBounds(30, 310, 340, 42);
        changeBtn.addActionListener(e -> handleChangePassword());
        form.add(changeBtn);

        return form;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setBackground(FIELD_BG);
        field.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1, true));
    }

    private void addEyeToggle(JPanel form, JPasswordField field, int x, int y) {
        JLabel eye = new JLabel("\uD83D\uDC41");
        eye.setBounds(x, y, 25, 20);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addMouseListener(new MouseAdapter() {
            private boolean visible = false;

            public void mouseClicked(MouseEvent e) {
                visible = !visible;
                field.setEchoChar(visible ? (char) 0 : '•');
            }
        });
        form.add(eye);
    }

    private void handleChangePassword() {
        String current = new String(currentPass.getPassword());
        String newP = new String(newPass.getPassword());
        String confirm = new String(confirmPass.getPassword());

        // 1. Basic validation
        if (current.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newP.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Verify current password against the database
        Admin verified = adminDAO.findByUsernameAndPassword(
                currentAdmin.getUsername(), current
        );

        if (verified == null) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                    "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Update the password in the database
        //    Since we have the admin object, we can set the new password and call update()
        currentAdmin.setPassword(newP);   // beware: plain text – use hashing in real project
        adminDAO.update(currentAdmin);

        // 4. Success
        JOptionPane.showMessageDialog(this, "Password changed successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // 5. Clear fields
        currentPass.setText("");
        newPass.setText("");
        confirmPass.setText("");
    }
}