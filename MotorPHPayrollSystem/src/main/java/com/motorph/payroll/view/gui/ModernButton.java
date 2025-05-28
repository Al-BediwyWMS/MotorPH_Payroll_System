package com.motorph.payroll.view.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color defaultColor = new Color(41, 128, 185);
    private Color hoverColor = new Color(52, 152, 219);
    private Color textColor = Color.WHITE;
    private boolean isRounded = true;
    
    public ModernButton(String text) {
        super(text);
        setupButton();
    }
    
    public ModernButton(String text, Color defaultColor, Color hoverColor) {
        super(text);
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        setupButton();
    }
    
    private void setupButton() {
        setFont(new Font("Montserrat", Font.BOLD, 14));
        setForeground(textColor);
        setBackground(defaultColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        
        // Add hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultColor);
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isPressed()) {
            g2.setColor(hoverColor.darker());
        } else if (getModel().isRollover()) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(defaultColor);
        }
        
        if (isRounded) {
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        } else {
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        
        g2.dispose();
        
        super.paintComponent(g);
    }
    
    public void setRounded(boolean rounded) {
        isRounded = rounded;
        repaint();
    }
    
    public void setButtonColors(Color defaultColor, Color hoverColor) {
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        repaint();
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        setForeground(textColor);
    }
}