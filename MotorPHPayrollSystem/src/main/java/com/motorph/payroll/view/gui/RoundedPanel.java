package com.motorph.payroll.view.gui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius = 15;
    
    public RoundedPanel(LayoutManager layout, Color backgroundColor) {
        super(layout);
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }
    
    public RoundedPanel(LayoutManager layout, Color backgroundColor, int radius) {
        super(layout);
        this.backgroundColor = backgroundColor;
        this.cornerRadius = radius;
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Paint background
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        
        // Paint border
        graphics.setColor(backgroundColor.darker());
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}