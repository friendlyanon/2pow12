package com.github.friendlyanon.twopowtwelve.game;

import com.github.friendlyanon.twopowtwelve.gui.ActivityAdapter;
import com.github.friendlyanon.twopowtwelve.util.FastList;
import lombok.Cleanup;
import lombok.val;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class Board extends ActivityAdapter {
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final int GAP = 10;
    public static int ROWS;
    public static int COLS;
    public static int WIDTH;
    public static int HEIGHT;
    private static int startCount;
    private static int randomCount;
    private final TileHolder[] board;
    private final TileHolder[] sorted;
    private final FastList<TileHolder> occupied;
    private final FastList<TileHolder> vacant;
    private final FastList<Tile> orphan;
    private final BufferedImage background;
    private final BufferedImage image;
    public BoardState state;
    public int goal;
    private int x;
    private int y;
    private boolean tilesMoved;
    private boolean reachedGoal;
    //</editor-fold>

    public Board(int x, int y, int goal) {
        this.x = x;
        this.y = y;
        if (goal == 0) {
            reachedGoal = true;
        }
        else {
            this.goal = goal;
        }
        val length = ROWS * COLS;
        vacant = new FastList<>(length);
        occupied = new FastList<>(length);
        orphan = new FastList<>(length);
        sorted = new TileHolder[length];
        board = new TileHolder[length];
        for (int row = 0, i = 0; row < ROWS; ++row) {
            for (var col = 0; col < COLS; ++col, ++i) {
                vacant.add(sorted[i] = board[i] = new TileHolder(row, col));
            }
        }
        background = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        createBackground();
        start();
        state = BoardState.PLAYING;
    }

    public static void setStatics(int dimensions) {
        randomCount = (startCount = (int) (Math.pow(dimensions / 4.0, 1.8) * 2)) >> 1;
        ROWS = COLS = dimensions;
        WIDTH = (COLS + 1) * GAP + COLS * Tile.WIDTH;
        HEIGHT = (ROWS + 1) * GAP + ROWS * Tile.HEIGHT;
    }

    //<editor-fold defaultstate="collapsed" desc="Init">
    private void createBackground() {
        @Cleanup("dispose") val graphics = (Graphics2D) background.getGraphics();
        graphics.setRenderingHints(Rendering.hints);
        graphics.setColor(new Color(0xBBADA0));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        graphics.setColor(new Color(0xCDC1B4));
        for (var row = 0; row < ROWS; ++row) {
            for (var col = 0; col < COLS; ++col) {
                val x = GAP + GAP * col + Tile.WIDTH * col;
                val y = GAP + GAP * row + Tile.HEIGHT * row;
                graphics.fillRoundRect(
                    x, y, Tile.WIDTH, Tile.HEIGHT, Tile.RADIUS, Tile.RADIUS
                );
            }
        }
    }

    private void start() {
        for (var i = 0; i < startCount; ++i) {
            spawnRandom();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Render & Simulation">

    //<editor-fold defaultstate="collapsed" desc="Moving Tiles">
    private void spawnRandom() {
        for (var i = 0; i < randomCount; ++i) {
            val size = vacant.size();
            if (size == 0) {
                return;
            }
            val rand = new Random();
            val position = rand.nextInt(size);
            val current = vacant.remove(position);
            current.tile = new Tile(
                rand.nextInt(10) < 9 ? 1 : 2,
                getTileX(current.col),
                getTileY(current.row)
            );
            occupied.add(current);
        }
        if (occupied.size() == board.length) {
            state = BoardState.DEAD;
        }
    }

    private void moveTiles(int i) {
        if (!tryMove(i)) {
            return;
        }

        tilesMoved = true;
        occupied.clear();
        vacant.clear();
        for (val holder : board) {
            if (holder.tile == null) {
                vacant.add(holder);
            }
            else {
                holder.tile.canCombine = true;
                occupied.add(holder);
            }
        }
    }

    private int sort(int direction) {
        val swap = (direction & 2) != 0;
        val upDown = (direction & 1) != 0;
        Arrays.sort(sorted, (x, y) -> {
            if (swap) {
                val z = x;
                x = y;
                y = z;
            }
            return upDown
                ? ((y.col << 16) | x.row) - ((x.col << 16) | y.row)
                : ((y.row << 16) | x.col) - ((x.row << 16) | y.col);
        });
        return upDown ? ROWS : COLS;
    }

    private boolean tryMove(int direction) {
        var didMove = false;
        var counter = 0;
        val limit = sort(direction);
        val length = sorted.length;
        for (var i = 0; i < length; ++i, ++counter) {
            if (counter == limit) {
                counter = 0;
            }
            else if (counter == 0) {
                continue;
            }
            val curr = sorted[i].tile;
            if (curr == null) {
                continue;
            }
            for (int j = i - 1, cur = counter - 1; cur >= 0; --j, --cur) {
                val holder = sorted[j];
                val prev = holder.tile;
                if (prev == null) {
                    didMove = true;
                    val tile = holder.tile = sorted[j + 1].tile;
                    tile.destination = (holder.row << 16) | holder.col;
                    sorted[j + 1].tile = null;
                }
                else if (prev.canCombine && curr.exponent == prev.exponent) {
                    prev.increment();
                    if (!reachedGoal && prev.exponent == goal) {
                        state = BoardState.WON;
                    }
                    didMove = true;
                    val h = sorted[j + 1];
                    orphan.add(h.tile);
                    h.tile.destination = (holder.row << 16) | holder.col;
                    h.tile = null;
                }
                else {
                    break;
                }
            }
        }
        return didMove;
    }

    private int getDistance(int val) {
        return -(val < 0 ? Math.max(-Tile.SPEED, val) : Math.min(Tile.SPEED, val));
    }

    private boolean animateTile(Tile tile) {
        val x = tile.x - getTileX(tile.destination & 0xFFFF);
        val zeroX = x == 0;
        if (!zeroX) {
            tile.x += getDistance(x);
        }

        val y = tile.y - getTileY(tile.destination >>> 16);
        val zeroY = y == 0;
        if (!zeroY) {
            tile.y += getDistance(y);
        }

        if (zeroX && zeroY) {
            tile.destination = -1;
            return true;
        }
        return false;
    }
    //</editor-fold>

    public void update() {
        if (state == BoardState.PLAYING) {
            Keyboard.check(this::moveTiles);
            if (tilesMoved) {
                spawnRandom();
                tilesMoved = false;
            }
        }
        for (val tile : orphan) {
            tile.update();
            if (tile.destination != -1 && animateTile(tile)) {
                orphan.remove(tile);
            }
        }
        for (val holder : occupied) {
            val tile = holder.tile;
            if (tile.destination == -1) {
                continue;
            }
            animateTile(tile);
        }
        for (val holder : occupied) {
            holder.tile.update();
        }
    }

    public void render(Graphics2D graphics) {
        @Cleanup("dispose") val imgGraphics = (Graphics2D) image.getGraphics();
        imgGraphics.setRenderingHints(Rendering.hints);
        imgGraphics.drawImage(background, 0, 0, null);

        for (val tile : orphan) {
            tile.render(imgGraphics);
        }
        for (val holder : occupied) {
            holder.tile.render(imgGraphics);
        }

        graphics.drawImage(image, x, y, null);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Utils">
    private int getTileX(int col) {
        return GAP + col * Tile.WIDTH + col * GAP;
    }

    private int getTileY(int row) {
        return GAP + row * Tile.HEIGHT + row * GAP;
    }

    public enum BoardState {
        PLAYING,
        WON,
        DEAD
    }

    private static class TileHolder {
        Tile tile;
        int row;
        int col;

        TileHolder(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    //</editor-fold>
}
