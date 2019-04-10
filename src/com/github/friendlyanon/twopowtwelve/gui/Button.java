package com.github.friendlyanon.twopowtwelve.gui;

import com.github.friendlyanon.twopowtwelve.game.Game;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textHeight;
import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textWidth;

public class Button extends ActivityAdapter {
    //<editor-fold defaultstate="collapsed" desc="Variables">
    public final Rectangle rect;
    ActionListener action;
    Font font = Game.font.deriveFont(36f);
    private State state = State.RELEASED;
    private String text;
    //</editor-fold>

    public Button(Rectangle rect, String text) {
        this.text = text;
        this.rect = rect;
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(state.color);
        graphics.fill(rect);
        graphics.setColor(Color.white);
        graphics.setFont(font);
        graphics.drawString(
            text,
            rect.x + (rect.width >> 1) - (textWidth(text, graphics) >> 1),
            rect.y + (rect.height >> 1) + (textHeight(text, graphics, font) >> 1)
        );
    }

    //<editor-fold defaultstate="collapsed" desc="Delegates">
    public void mousePressed(MouseEvent e) {
        if (!rect.contains(e.getPoint())) {
            return;
        }
        state = State.PRESSED;
    }

    public void mouseReleased(MouseEvent e) {
        if (!rect.contains(e.getPoint())) {
            return;
        }
        action.actionPerformed(null);
        state = State.RELEASED;
    }

    public void mouseDragged(MouseEvent e) {
        if (rect.contains(e.getPoint())) {
            state = State.PRESSED;
        }
        else {
            state = State.RELEASED;
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (rect.contains(e.getPoint())) {
            state = State.HOVER;
        }
        else {
            state = State.RELEASED;
        }
    }
    //</editor-fold>

    private enum State {
        RELEASED(Color.black),
        HOVER(Color.red),
        PRESSED(Color.green);

        public final Color color;

        State(Color color) {
            this.color = color;
        }
    }
}
