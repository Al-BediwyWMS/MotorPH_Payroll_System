package com.motorph.payroll.view.gui.components;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class DashboardCard extends RoundedPanel {
    public DashboardCard(String title, String value, Color color) {
        super(new BorderLayout(), new Color(255, 255, 255, 240), 10);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setPreferredSize(new Dimension(200, 120));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 100, 100));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        
        add(contentPanel, BorderLayout.CENTER);
    }
}