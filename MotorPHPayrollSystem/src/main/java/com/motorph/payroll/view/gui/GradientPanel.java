package com.motorph.payroll.view.gui.components;

import java.awt.*;
import javax.swing.*;

public class GradientPanel extends JPanel {
    private Color color1 = new Color(180, 144, 202); // Light purple
    private Color color2 = new Color(245, 194, 175); // Light orange/peach
    
    public GradientPanel() {
        setOpaque(false);
    }
    
    public GradientPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int w = getWidth();
        int h = getHeight();
        
        GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
    
    public void setGradientColors(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        repaint();
    }
}