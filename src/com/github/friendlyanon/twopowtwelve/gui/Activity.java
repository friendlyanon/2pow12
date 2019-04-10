package com.github.friendlyanon.twopowtwelve.gui;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Activity {
    void update();

    void render(Graphics2D graphics);

    void mousePressed(MouseEvent event);

    void mouseReleased(MouseEvent event);

    void mouseMoved(MouseEvent event);

    void mouseDragged(MouseEvent event);
}
