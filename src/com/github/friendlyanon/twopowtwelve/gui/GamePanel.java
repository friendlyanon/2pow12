package com.github.friendlyanon.twopowtwelve.gui;

import lombok.val;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class GamePanel implements Activity {
    Activity[] objects;

    public void update() {
        for (val obj : objects) {
            obj.update();
        }
    }

    public void render(Graphics2D graphics) {
        for (val obj : objects) {
            obj.render(graphics);
        }
    }

    public void mousePressed(MouseEvent event) {
        for (val obj : objects) {
            obj.mousePressed(event);
        }
    }

    public void mouseReleased(MouseEvent event) {
        for (val obj : objects) {
            obj.mouseReleased(event);
        }
    }

    public void mouseMoved(MouseEvent event) {
        for (val obj : objects) {
            obj.mouseMoved(event);
        }
    }

    public void mouseDragged(MouseEvent event) {
        for (val obj : objects) {
            obj.mouseDragged(event);
        }
    }
}
