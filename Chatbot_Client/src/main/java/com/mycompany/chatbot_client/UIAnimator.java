package com.mycompany.chatbot_client;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Timer;

public class UIAnimator {
    public static void addSmoothHover(JComponent comp, Color defaultColor, Color hoverColor) {
        comp.setBackground(defaultColor);
        comp.addMouseListener(new MouseAdapter() {
            Timer fadeTimer;
            @Override
            public void mouseEntered(MouseEvent e) { animateColor(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { animateColor(defaultColor); }

            private void animateColor(Color targetColor) {
                if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
                fadeTimer = new Timer(15, event -> {
                    Color current = comp.getBackground();
                    int r = step(current.getRed(), targetColor.getRed());
                    int g = step(current.getGreen(), targetColor.getGreen());
                    int b = step(current.getBlue(), targetColor.getBlue());
                    comp.setBackground(new Color(r, g, b));
                    if (r == targetColor.getRed() && g == targetColor.getGreen() && b == targetColor.getBlue()) {
                        fadeTimer.stop();
                    }
                });
                fadeTimer.start();
            }
            private int step(int current, int target) {
                if (current < target) return Math.min(current + 12, target);
                if (current > target) return Math.max(current - 12, target);
                return current;
            }
        });
    }
}