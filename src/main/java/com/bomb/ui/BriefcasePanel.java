package com.bomb.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.Random;

// Background of the bomb window: a metal briefcase with a handle and brackets
public class BriefcasePanel extends JPanel {

    private boolean exploded;

    public BriefcasePanel() {
        setBackground(Theme.BG);
        // keep the content inside the case
        setBorder(new EmptyBorder(64, 70, 50, 70));
    }

    public void setExploded(boolean exploded) {
        this.exploded = exploded;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int x = 24, y = 36, cw = w - 48, ch = h - 48;

        // handle (drawn first so the case covers its bottom)
        g2.setColor(Theme.METAL);
        g2.setStroke(new BasicStroke(14));
        g2.drawRoundRect(w / 2 - 130, 8, 260, 70, 30, 30);

        // case
        g2.setColor(Theme.CASE);
        g2.fillRoundRect(x, y, cw, ch, 30, 30);
        g2.setColor(Theme.METAL);
        g2.setStroke(new BasicStroke(8));
        g2.drawRoundRect(x, y, cw, ch, 30, 30);

        // a few scratches (same every time)
        Random r = new Random(7);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(0x3D4A2F));
        for (int i = 0; i < 18; i++) {
            int sx = x + 20 + r.nextInt(cw - 100), sy = y + 20 + r.nextInt(ch - 40);
            g2.drawLine(sx, sy, sx + 20 + r.nextInt(50), sy + r.nextInt(9) - 4);
        }

        // corner brackets
        g2.setColor(Theme.METAL);
        int b = 60;
        g2.fillPolygon(new Polygon(new int[]{x, x + b, x}, new int[]{y, y, y + b}, 3));
        g2.fillPolygon(new Polygon(new int[]{x + cw, x + cw - b, x + cw}, new int[]{y, y, y + b}, 3));
        g2.fillPolygon(new Polygon(new int[]{x, x + b, x}, new int[]{y + ch, y + ch, y + ch - b}, 3));
        g2.fillPolygon(new Polygon(new int[]{x + cw, x + cw - b, x + cw}, new int[]{y + ch, y + ch, y + ch - b}, 3));

        // hinges and latches on the sides
        for (int hy : new int[]{y + ch / 4, y + ch * 3 / 4 - 50}) {
            g2.fillRect(x - 14, hy, 26, 60);
            g2.fillRect(x + cw - 12, hy, 26, 60);
        }

        // red flash when the bomb explodes
        if (exploded) {
            g2.setColor(new Color(180, 30, 20, 90));
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
