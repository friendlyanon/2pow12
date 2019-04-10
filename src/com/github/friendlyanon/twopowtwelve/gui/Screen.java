package com.github.friendlyanon.twopowtwelve.gui;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Screen {
    private static Screen screen;
    private GamePanel currentPanel;

    private Screen() {}

    public static Screen getInstance() {
        return screen == null ? (screen = new Screen()) : screen;
    }

    public void update() {
        currentPanel.update();
    }

    public void render(Graphics2D graphics) {
        currentPanel.render(graphics);
    }

    public void setCurrentPanel(Panels panel) {
        currentPanel = panel.panel;
    }

    public void mousePressed(MouseEvent e) {
        currentPanel.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        currentPanel.mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e) {
        currentPanel.mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        currentPanel.mouseDragged(e);
    }
}
