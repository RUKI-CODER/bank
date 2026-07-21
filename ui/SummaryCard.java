package bank.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SummaryCard extends RoundedPanel {

    public SummaryCard(String title, String value, String iconText, Color color) {
        super(18, color);
        setPreferredSize(new Dimension(240, 110));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblIcon = new JLabel(iconText);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(255, 255, 255, 220));
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblTitle);

        add(top, BorderLayout.NORTH);
        add(textPanel, BorderLayout.SOUTH);
    }
}