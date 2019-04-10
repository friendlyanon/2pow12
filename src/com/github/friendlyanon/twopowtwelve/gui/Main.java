package com.github.friendlyanon.twopowtwelve.gui;

import com.github.friendlyanon.twopowtwelve.game.Audio;
import com.github.friendlyanon.twopowtwelve.game.Game;

import javax.swing.*;

public class Main {
    public static final Game game;
    public static JFrame window;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {
        }
        game = new Game();
        Audio.getInstance().loadAudioSamples();
    }

    public static void main(String[] args) {
        window = new JFrame("2^12");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(game);
        window.setVisible(true);

        game.window = window;
        game.setDimensions(420, 420);
        game.start();
    }
}
