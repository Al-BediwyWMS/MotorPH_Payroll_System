package com.motorph.payroll.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class ImageHelper {
    
    /**
     * Loads an image from resources or file system, maintaining aspect ratio
     * @param resourcePath Path to the image in resources
     * @param fallbackPath Fallback path on the file system
     * @param maxWidth Maximum width for the image
     * @param maxHeight Maximum height for the image
     * @return Scaled ImageIcon that maintains aspect ratio
     */
    public static ImageIcon loadImage(String resourcePath, String fallbackPath, int maxWidth, int maxHeight) {
        try {
            // Try to load from resources
            URL imageUrl = ImageHelper.class.getResource(resourcePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                return scaleImageIcon(icon, maxWidth, maxHeight, true);
            } else {
                throw new Exception("Resource not found");
            }
        } catch (Exception e1) {
            try {
                // Try to load from file
                File imageFile = new File(fallbackPath);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    return scaleImageIcon(icon, maxWidth, maxHeight, true);
                }
            } catch (Exception e2) {
                System.err.println("Could not load image from file: " + e2.getMessage());
            }
        }
        
        // Return null if image couldn't be loaded
        return null;
    }
    
    /**
     * Scales an ImageIcon to fit within maxWidth and maxHeight while maintaining aspect ratio
     * @param icon The original ImageIcon
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @param preserveAspectRatio Whether to preserve aspect ratio
     * @return Scaled ImageIcon
     */
    public static ImageIcon scaleImageIcon(ImageIcon icon, int maxWidth, int maxHeight, boolean preserveAspectRatio) {
        Image img = icon.getImage();
        int originalWidth = img.getWidth(null);
        int originalHeight = img.getHeight(null);
        
        if (originalWidth <= 0 || originalHeight <= 0) {
            return icon; // Can't determine dimensions, return original
        }
        
        int newWidth, newHeight;
        
        if (preserveAspectRatio) {
            // Calculate the scaling factor to maintain aspect ratio
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        } else {
            newWidth = maxWidth;
            newHeight = maxHeight;
        }
        
        // Scale the image
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    
    /**
     * Creates a text-based logo image
     * @param text The text to display
     * @param width Image width
     * @param height Image height
     * @param foreground Text color
     * @param background Background color
     * @param fontName Font name
     * @param fontSize Font size
     * @param fontStyle Font style
     * @return ImageIcon containing the text logo
     */
    public static ImageIcon createTextLogo(String text, int width, int height, 
                                          Color foreground, Color background, 
                                          String fontName, int fontSize, int fontStyle) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background
        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);
        
        // Draw text
        g2d.setColor(foreground);
        g2d.setFont(new Font(fontName, fontStyle, fontSize));
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Center the text
        g2d.drawString(text, (width - textWidth) / 2, height/2 + fm.getAscent()/2);
        
        g2d.dispose();
        
        return new ImageIcon(img);
    }
    
    /**
     * Creates a simple icon with text (like emoji)
     * @param text The text or emoji to display
     * @param size Icon size
     * @param color Text color
     * @return ImageIcon containing the text/emoji
     */
    public static ImageIcon createSimpleIcon(String text, int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Dialog", Font.BOLD, size));
        FontMetrics fm = g2d.getFontMetrics();
        
        g2d.setColor(color);
        g2d.drawString(text, 0, fm.getAscent());
        
        g2d.dispose();
        
        return new ImageIcon(img);
    }
    
    /**
     * Creates a default profile image with initials
     * @param initials Initials to display
     * @param size Image size
     * @param bgColor Background color
     * @param textColor Text color
     * @return ImageIcon with initials in a circle
     */
    public static ImageIcon createInitialsProfileImage(String initials, int size, Color bgColor, Color textColor) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a colored circle
        g2d.setColor(bgColor);
        g2d.fillOval(0, 0, size - 1, size - 1);
        
        // Draw the initials
        g2d.setColor(textColor);
        g2d.setFont(new Font("Montserrat", Font.BOLD, size / 3));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getHeight();
        
        g2d.drawString(initials, 
            (size - textWidth) / 2, 
            (size - textHeight) / 2 + fm.getAscent());
        
        g2d.dispose();
        
        return new ImageIcon(img);
    }
}