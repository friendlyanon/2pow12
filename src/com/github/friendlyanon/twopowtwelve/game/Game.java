package com.github.friendlyanon.twopowtwelve.game;

import com.github.friendlyanon.twopowtwelve.gui.Panels;
import com.github.friendlyanon.twopowtwelve.gui.Screen;
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Game
    extends JPanel
    implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
    //<editor-fold defaultstate="collapsed" desc="Variables">
    public static final Font font = new Font("Arial", Font.PLAIN, 28);
    private static final double FPS60 = 1e9 / 60;
    public static int WIDTH = 420;
    public static int HEIGHT = 420;
    public JFrame window;
    private boolean running;
    private BufferedImage image;
    private Screen screen;
    private static final Color background = new Color(0xFAF8EF);
    //</editor-fold>

    public Game() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        screen = Screen.getInstance();
        screen.setCurrentPanel(Panels.MAINMENU);
    }

    public void setDimensions(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        window.pack();
        window.setLocationRelativeTo(null);
    }

    //<editor-fold defaultstate="collapsed" desc="Render & Simulation">
    private void update() {
        screen.update();
        Keyboard.update();
    }

    private void render() {
        @Cleanup("dispose") val imgGraphics = (Graphics2D) image.getGraphics();
        imgGraphics.setRenderingHints(Rendering.hints);
        imgGraphics.setColor(background);
        imgGraphics.fillRect(0, 0, WIDTH, HEIGHT);
        screen.render(imgGraphics);

        @Cleanup("dispose") val graphics = (Graphics2D) getGraphics();
        imgGraphics.setRenderingHints(Rendering.hints);
        graphics.drawImage(image, 0, 0, null);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Game loop">
    @Override
    public void run() {
        var then = System.nanoTime();
        var unprocessed = 0.0;

        while (running) {
            var shouldRender = false;
            val now = System.nanoTime();
            unprocessed += (now - then) / FPS60;
            then = now;

            for (; unprocessed >= 1.0; --unprocessed) {
                update();
                shouldRender = true;
            }

            if (shouldRender) {
                render();
            }
            else {
                try {
                    Thread.sleep(1);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Start & Stop">
    @Synchronized
    public void start() {
        if (running) {
            return;
        }
        running = true;
        Audio.getInstance().playTitle();
        new Thread(this, "twopowtwelve").start();
    }

    @Synchronized
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        System.exit(0);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Delegates">
    @Override
    public void keyPressed(KeyEvent e) {
        Keyboard.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Keyboard.keyReleased(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        screen.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        screen.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        screen.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        screen.mouseMoved(e);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Unused overrides">
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
    //</editor-fold>
}
