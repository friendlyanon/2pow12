package com.github.friendlyanon.twopowtwelve.game;

import lombok.Cleanup;
import lombok.val;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textHeight;
import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textWidth;

public class Tile {
    //<editor-fold defaultstate="collapsed" desc="Variables">
    public static int SPEED;
    public static final int RADIUS = 12;

    private static final int dimension = 60;
    public static final int WIDTH = dimension;
    public static final int HEIGHT = dimension;

    private static final char[] characters =
        { 8304, 185, 178, 179, 8308, 8309, 8310, 8311, 8312, 8313 };

    private static final Color transparent = new Color(0, 0, 0, 0);
    private static final Color[] colors = {
        new Color(0xeee4da),
        new Color(0xede0c8),
        new Color(0xf2b179),
        new Color(0xf59563),
        new Color(0xf67c5f),
        new Color(0xf65e3b),
        new Color(0xedcf72),
        new Color(0xedcc61),
        new Color(0xedc850),
        new Color(0xedc53f),
        new Color(0xedc22e)
    };

    public int exponent;
    public int destination = -1;
    public boolean canCombine = true;
    public int x;
    public int y;
    private BufferedImage image;
    private Color color = Color.black;
    private BufferedImage spawnImg;
    private double spawnScale = 0.1;
    private BufferedImage combineImg;
    private double combineScale;
    private AnimationState state = AnimationState.SPAWNING;
    //</editor-fold>

    public Tile(int exponent, int x, int y) {
        this.exponent = exponent;
        this.x = x;
        this.y = y;
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        spawnImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        combineImg = new BufferedImage(
            WIDTH << 1,
            HEIGHT << 1,
            BufferedImage.TYPE_INT_ARGB
        );
        drawImage();
    }

    //<editor-fold defaultstate="collapsed" desc="Render & Simulation">
    private void drawImage() {
        @Cleanup("dispose") val graphics = setupGraphics(image);

        val backgroundColor = colors[exponent % colors.length];
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, WIDTH, HEIGHT, RADIUS, RADIUS);

        graphics.setColor(color);
        val font = exponent <= 100 ? Game.font.deriveFont(32f) : Game.font;
        val str = exponentToString();
        graphics.setFont(font);
        graphics.drawString(
            str,
            (WIDTH >> 1) - (textWidth(str, graphics) >> 1),
            (HEIGHT >> 1) + (textHeight(str, graphics, font) >> 1)
        );
    }

    public void increment() {
        canCombine = false;
        ++exponent;
        drawImage();
        combineScale = 1.2;
        state = AnimationState.COMBINING;
    }

    private void applyScaling(BufferedImage img, double scale) {
        val tf = new AffineTransform();
        tf.translate(
            (WIDTH >> 1) - scale * (WIDTH >> 1),
            (HEIGHT >> 1) - scale * (HEIGHT >> 1)
        );
        tf.scale(scale, scale);
        @Cleanup("dispose") val graphics = setupGraphics(img);
        graphics.drawImage(image, tf, null);
    }

    public void update() {
        switch (state) {
            case NORMAL:
                return;
            case SPAWNING:
                applyScaling(spawnImg, spawnScale);
                if ((spawnScale += 0.1) >= 1.0) {
                    state = AnimationState.NORMAL;
                }
                break;
            case COMBINING:
                applyScaling(combineImg, combineScale);
                if ((combineScale -= 0.1) <= 1.0) {
                    state = AnimationState.NORMAL;
                }
                break;
        }
    }

    public void render(Graphics2D graphics) {
        switch (state) {
            case NORMAL:
                spawnImg = null;
                graphics.drawImage(image, x, y, null);
                break;
            case SPAWNING:
                graphics.drawImage(spawnImg, x, y, null);
                break;
            case COMBINING:
                graphics.drawImage(
                    combineImg,
                    (int) (x + (WIDTH >> 1) - combineScale * (WIDTH >> 1)),
                    (int) (y + (HEIGHT >> 1) - combineScale * (HEIGHT >> 1)),
                    null
                );
                break;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Utils">
    private int digits10() {
        var v = exponent;
        var i = 1;
        for (; v >= 10; ++i) v /= 10;
        return i;
    }

    private String exponentToString() {
        var value = exponent;
        val result = digits10();
        val buffer = new char[result + 1];
        for (var pos = result; value >= 10; --pos) {
            val q = value / 10;
            val r = value % 10;
            buffer[pos] = characters[r];
            value = q;
        }
        buffer[1] = characters[value];
        buffer[0] = '2';
        return new String(buffer);
    }

    private Graphics2D setupGraphics(BufferedImage img) {
        val graphics = (Graphics2D) img.getGraphics();
        graphics.setRenderingHints(Rendering.hints);
        graphics.setColor(transparent);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        return graphics;
    }

    private enum AnimationState {
        SPAWNING,
        NORMAL,
        COMBINING
    }
    //</editor-fold>
}
