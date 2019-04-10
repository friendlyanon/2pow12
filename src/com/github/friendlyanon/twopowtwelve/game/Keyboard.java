package com.github.friendlyanon.twopowtwelve.game;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

@UtilityClass
public class Keyboard {
    public boolean[] pressed = new boolean[4];
    public boolean[] previous = new boolean[4];

    public void update() {
        System.arraycopy(pressed, 0, previous, 0, 4);
    }

    private int mapKeyCode(int code) {
        switch (code) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                return 0;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                return 1;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                return 2;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                return 3;
            default:
                return -1;
        }
    }

    public void keyPressed(KeyEvent e) {
        val idx = mapKeyCode(e.getKeyCode());
        if (idx >= 0) {
            pressed[idx] = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        val idx = mapKeyCode(e.getKeyCode());
        if (idx >= 0) {
            pressed[idx] = false;
        }
    }

    public boolean typed(int code) {
        val idx = mapKeyCode(code);
        return idx >= 0 && pressed[idx] && !previous[idx];
    }

    public void check(IntConsumer fn) {
        for (var i = 0; i < 4; ++i) {
            if (pressed[i] && !previous[i]) {
                fn.accept(i);
            }
        }
    }
}
