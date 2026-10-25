package com.bomb.ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// The slightly crooked yellow sticker with the serial number and batteries
public class SerialSticker extends JComponent {

    private final String serial;
    private final int batteries;

    public SerialSticker(String serial, int batteries) {
        this.serial = serial;
        this.batteries = batteries;
        setPreferredSize(new Dimension(300, 92));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = 270, h = 72;
        g2.translate((getWidth() - w) / 2, (getHeight() - h) / 2);
        g2.rotate(Math.toRadians(2), w / 2.0, h / 2.0);

        g2.setColor(new Color(0xE8DFA8));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(0xB8AD70));
        g2.drawRect(0, 0, w, h);

        g2.setColor(new Color(0x2B2B22));
        g2.setFont(Theme.mono(24));
        g2.drawString("SN  " + serial, 14, 32);
        g2.setFont(Theme.mono(16));
        g2.drawString("BATTERIES  " + batteries, 14, 58);

        // two bits of tape on the corners
        g2.setColor(new Color(210, 205, 180, 170));
        g2.fillRect(w - 50, -8, 44, 16);
        g2.fillRect(6, h - 8, 44, 16);
        g2.dispose();
    }
}
