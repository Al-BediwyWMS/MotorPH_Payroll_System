package com.motorph.payroll.view.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class NotificationPopup extends JPopupMenu {
    private List<String> notifications = new ArrayList<>();
    private List<String> timestamps = new ArrayList<>();
    private JPanel contentPanel;
    
    public NotificationPopup() {
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        setBackground(Color.WHITE);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setForeground(Color.WHITE);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearNotifications());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);
        
        // Empty state
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel emptyLabel = new JLabel("No notifications");
        emptyLabel.setFont(new Font("Montserrat", Font.ITALIC, 12));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        
        // Add components to popup
        add(headerPanel);
        contentPanel.add(emptyPanel);
        add(contentPanel);
        
        // Add some default notifications
        addNotification("Welcome to MotorPH Payroll System!", "Just now");
        addNotification("Your attendance has been recorded.", "10 minutes ago");
        addNotification("New payslip is available.", "Yesterday");
        
        // Set size
        setPreferredSize(new Dimension(300, 350));
    }
    
    public void addNotification(String message, String timestamp) {
        notifications.add(message);
        timestamps.add(timestamp);
        
        // Clear content panel and rebuild
        contentPanel.removeAll();
        
        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
            
            JLabel emptyLabel = new JLabel("No notifications");
            emptyLabel.setFont(new Font("Montserrat", Font.ITALIC, 12));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            contentPanel.add(emptyPanel);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                JPanel notifPanel = createNotificationPanel(notifications.get(i), timestamps.get(i), i);
                contentPanel.add(notifPanel);
                
                // Add separator except for the last item
                if (i < notifications.size() - 1) {
                    JSeparator separator = new JSeparator();
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    contentPanel.add(separator);
                }
            }
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createNotificationPanel(String message, String timestamp, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel messageLabel = new JLabel("<html><body width='250'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Montserrat", Font.PLAIN, 12));
        
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Montserrat", Font.ITALIC, 10));
        timeLabel.setForeground(Color.GRAY);
        
        // Close button (X)
        JLabel closeLabel = new JLabel("×");
        closeLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        closeLabel.setForeground(Color.GRAY);
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeNotification(index);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setForeground(Color.RED);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(Color.GRAY);
            }
        });
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(messageLabel, BorderLayout.CENTER);
        topPanel.add(closeLabel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void removeNotification(int index) {
        if (index >= 0 && index < notifications.size()) {
            notifications.remove(index);
            timestamps.remove(index);
            
            // Rebuild notifications
            contentPanel.removeAll();
            
            if (notifications.isEmpty()) {
                JPanel emptyPanel = new JPanel(new BorderLayout());
                emptyPanel.setBackground(Color.WHITE);
                emptyPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
                
                JLabel emptyLabel = new JLabel("No notifications");
                emptyLabel.setFont(new Font("Montserrat", Font.ITALIC, 12));
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                emptyPanel.add(emptyLabel, BorderLayout.CENTER);
                contentPanel.add(emptyPanel);
            } else {
                for (int i = 0; i < notifications.size(); i++) {
                    JPanel notifPanel = createNotificationPanel(notifications.get(i), timestamps.get(i), i);
                    contentPanel.add(notifPanel);
                    
                    // Add separator except for the last item
                    if (i < notifications.size() - 1) {
                        JSeparator separator = new JSeparator();
                        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                        contentPanel.add(separator);
                    }
                }
            }
            
            revalidate();
            repaint();
        }
    }
    
    private void clearNotifications() {
        notifications.clear();
        timestamps.clear();
        
        contentPanel.removeAll();
        
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel emptyLabel = new JLabel("No notifications");
        emptyLabel.setFont(new Font("Montserrat", Font.ITALIC, 12));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        contentPanel.add(emptyPanel);
        
        revalidate();
        repaint();
    }
    
    public int getNotificationCount() {
        return notifications.size();
    }
}