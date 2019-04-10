package com.github.friendlyanon.twopowtwelve.gui;

import com.github.friendlyanon.twopowtwelve.game.Audio;
import com.github.friendlyanon.twopowtwelve.game.Board;
import com.github.friendlyanon.twopowtwelve.game.Game;
import com.github.friendlyanon.twopowtwelve.game.Tile;
import lombok.Cleanup;
import lombok.val;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textHeight;
import static com.github.friendlyanon.twopowtwelve.util.TextDimensions.textWidth;

public enum Panels {
    MAINMENU(new MainMenu()),
    HELP(new Help()),
    SETUP(new Setup()),
    INGAME(new InGame());

    public final GamePanel panel;

    Panels(GamePanel panel) {
        this.panel = panel;
    }
}

class TextBox extends ActivityAdapter {
    Color color = Color.lightGray;
    Font font;
    String message;
    private Rectangle rect;

    public TextBox(Rectangle rect, String message) {
        if ((this.message = message).indexOf('\n') >= 0) {
            font = Game.font.deriveFont(28f);
        }
        else {
            font = Game.font.deriveFont(36f);
        }
        this.rect = rect;
    }

    public TextBox(Rectangle rect) {
        this(rect, "");
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(color);
        graphics.fill(rect);
        graphics.setColor(Color.black);
        graphics.setFont(font);
        val height = textHeight(message, graphics, font);
        if (message.indexOf('\n') >= 0) {
            val x = rect.x + 10;
            var currentY = rect.y + height + 5;
            for (val line : message.split("\n")) {
                graphics.drawString(line, x, currentY);
                currentY += height + 10;
            }
            return;
        }
        graphics.drawString(
            message,
            rect.x + (rect.width >> 1) - (textWidth(message, graphics) >> 1),
            rect.y + (rect.height >> 1) + (height >> 1)
        );
    }
}

class Title extends GamePanel {
    private static final Font titleFont = Game.font.deriveFont(100f);
    private static final String titleText = "2^12";

    @Override
    public void render(Graphics2D graphics) {
        super.render(graphics);
        graphics.setFont(titleFont);
        graphics.setColor(Color.black);
        graphics.drawString(
            titleText,
            (Game.WIDTH >> 1) - (textWidth(titleText, graphics) >> 1),
            120
        );
    }
}

class MainMenu extends Title {
    static final int buttonWidth = 220;
    static final int buttonHeight = 60;
    static final int buttonGap = 80;

    MainMenu() {
        objects = new Button[3];
        val x = (Game.WIDTH >> 1) - (buttonWidth >> 1);
        val play = new Button(new Rectangle(
            x, 165, buttonWidth, buttonHeight
        ), "Play!");
        val help = new Button(new Rectangle(
            x, play.rect.y + buttonGap, buttonWidth, buttonHeight
        ), "Help");
        val quit = new Button(new Rectangle(
            x, help.rect.y + buttonGap, buttonWidth, buttonHeight
        ), "Quit");
        play.action = (nil) -> Screen.getInstance().setCurrentPanel(Panels.SETUP);
        help.action = (nil) -> Screen.getInstance().setCurrentPanel(Panels.HELP);
        quit.action = (nil) -> System.exit(0);
        objects[0] = play;
        objects[1] = help;
        objects[2] = quit;
    }
}

class Help extends Title {
    Help() {
        val x = (Game.WIDTH >> 1) - (MainMenu.buttonWidth >> 1);
        val y = Game.HEIGHT - 10 - MainMenu.buttonHeight;
        val back = new Button(new Rectangle(
            x, y, MainMenu.buttonWidth, MainMenu.buttonHeight
        ), "Back");
        back.action = (nil) -> Screen.getInstance().setCurrentPanel(Panels.MAINMENU);
        objects = new Activity[] {
            new TextBox(
                new Rectangle(10, 155, 400, 185),
                "Control: WASD or arrow keys\n\nGoal: combine tiles until the\nexponent set as a goal is\nreached"
            ),
            back
        };
    }
}

class Setup extends Title {
    private static final SetupDialog dialog = new SetupDialog();
    private int dimensions = 4;
    private int goal = 12;
    private boolean changed = true;

    Setup() {
        objects = new Activity[6];
        val modal = new Button(new Rectangle(7, 350, 200, 60), "Change");
        val start = new Button(new Rectangle(213, 350, 200, 60), "Start");
        modal.action = (nil) -> dialog.run(this);
        start.action = (nil) -> startHandler();
        objects[0] = modal;
        objects[1] = start;
        objects[2] = new TextBox(new Rectangle(10, 140, 195, 50), "Grid size:");
        objects[3] = new TextBox(new Rectangle(10, 210, 400, 50), "Win condition:");
        objects[4] = new TextBox(new Rectangle(210, 140, 195, 50));
        objects[5] = new TextBox(new Rectangle(10, 265, 400, 50));
    }

    private void startHandler() {
        Tile.SPEED = 11 * (int) (Math.pow(dimensions / 4.0, 0.9) * 2);
        Board.setStatics(dimensions);
        Main.game.setDimensions(Board.WIDTH + 20, Board.HEIGHT + 70);
        val exitButton = new Button(
            new Rectangle(Game.WIDTH - 110, 0, 100, 50),
            "Exit"
        );
        exitButton.action = (nil) -> {
            Panels.INGAME.panel.objects = new Activity[0];
            Screen.getInstance().setCurrentPanel(Panels.MAINMENU);
            Audio.getInstance().playTitle();
            Main.game.setDimensions(420, 420);
        };

        Panels.INGAME.panel.objects = new Activity[] {
            new Board(
                (Game.WIDTH >> 1) - (Board.WIDTH >> 1),
                Game.HEIGHT - 10 - Board.HEIGHT,
                goal
            ),
            exitButton,
            InGame.audioButton,
            InGame.goalText
        };
        InGame.goalText.message = goal == 0
            ? "Free Play"
            : "Goal: 2^" + goal;
        InGame.goalText.font = Game.font.deriveFont(20f);
        Screen.getInstance().setCurrentPanel(Panels.INGAME);
        Audio.getInstance().playIngame();
    }

    @Override
    public void update() {
        super.update();
        if (!changed) {
            return;
        }
        ((TextBox) objects[4]).message = String.valueOf(dimensions) + '×' + dimensions;
        ((TextBox) objects[5]).message = goal == 0
            ? "Free Play"
            : "Reaching exponent " + goal;
        changed = false;
    }

    private static class SetupDialog {
        private JDialog dialog;
        private Setup instance;

        SetupDialog() {
            dialog = new JDialog(Main.window, "Setup", true);
            val form = new SetupForm();
            dialog.setContentPane(form.panel);
            dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            dialog.setResizable(false);

            val freePlay = form.freePlayRadioButton;
            val winCond = form.exponentReachedRadioButton;
            val spinner = form.spinnerExponent;
            val ok = form.OKButton;
            val combo = form.comboBoxGrid;

            freePlay.addChangeListener(x -> {
                if (!freePlay.isSelected()) {
                    return;
                }
                winCond.setSelected(false);
                spinner.setEnabled(false);
            });

            winCond.addChangeListener(x -> {
                if (!winCond.isSelected()) {
                    return;
                }
                freePlay.setSelected(false);
                spinner.setEnabled(true);
            });

            ok.addActionListener(x -> {
                dialog.setVisible(false);
                if (freePlay.isSelected()) {
                    if (instance.goal != 0) {
                        instance.goal = 0;
                        instance.changed = true;
                    }
                }
                else {
                    val value = ((Number) spinner.getValue()).intValue();
                    if (instance.goal != value) {
                        instance.goal = value;
                        instance.changed = true;
                    }
                }

                val idx = combo.getSelectedIndex() + 4;
                if (instance.dimensions != idx) {
                    instance.dimensions = idx;
                    instance.changed = true;
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(null);
        }

        void run(Setup instance) {
            this.instance = instance;
            dialog.setVisible(true);
        }
    }
}

class InGame extends GamePanel {
    static final Activity audioButton;
    static TextBox goalText = new TextBox(new Rectangle(70, 0, 120, 50));
    private static boolean isWithOverlay;

    static {
        Activity button;
        try {
            val loader = Main.class.getClassLoader();
            @SuppressWarnings("ConstantConditions")
            val mute = ImageIO.read(loader.getResource("icons/icons8-no-audio-50.png"));
            @SuppressWarnings("ConstantConditions")
            val unmute = ImageIO.read(loader.getResource("icons/icons8-voice-50.png"));
            button = new ActivityAdapter() {
                private BufferedImage image = unmute;

                @Override
                public void render(Graphics2D graphics) {
                    graphics.drawImage(image, 10, 0, 50, 50, null);
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    val x = event.getX();
                    if (x < 10 || 60 < x) {
                        return;
                    }
                    val y = event.getY();
                    if (50 < y) {
                        return;
                    }
                    val isMuted = image == mute;
                    image = isMuted ? unmute : mute;
                    if (!isMuted) {
                        Audio.getInstance().muteIngame();
                    }
                    else {
                        Audio.getInstance().playIngame();
                    }
                }
            };
        }
        catch (Exception ignored) {
            button = new ActivityAdapter() {};
        }
        audioButton = button;
    }

    @Override
    public void update() {
        if (objects.length == 0) {
            return;
        }
        super.update();
        if (isWithOverlay) return;
        val board = (Board) objects[0];
        Activity overlay;
        switch (board.state) {
            case WON:
                overlay = new WonOverlay(this);
                break;
            case DEAD:
                overlay = new DeadOverlay();
                break;
            default:
                return;
        }
        val withOverlay = new Activity[objects.length + 1];
        System.arraycopy(objects, 0, withOverlay, 0, objects.length);
        withOverlay[objects.length] = overlay;
        objects = withOverlay;
        isWithOverlay = true;
    }

    private abstract static class OverlayBase extends GamePanel {
        protected static final Color transparent = new Color(0, true);
        private static final Color overlayBackground = new Color(0xAAFFFFFF, true);
        protected final int x = 10;
        protected final int y = Game.HEIGHT - 10 - Board.HEIGHT;
        private final BufferedImage image = new BufferedImage(Board.WIDTH, Board.HEIGHT, BufferedImage.TYPE_INT_ARGB);

        {
            @Cleanup("dispose")
            val graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(overlayBackground);
            graphics.fillRect(0, 0, Board.WIDTH, Board.HEIGHT);
        }

        @Override
        public void render(Graphics2D graphics) {
            graphics.drawImage(image, x, y, null);
            super.render(graphics);
        }
    }

    private static class WonOverlay extends OverlayBase {
        public WonOverlay(InGame instance) {
            val continueButton = new Button(
                new Rectangle(
                    x + (Board.WIDTH >> 1) - (290 >> 1), y + Board.HEIGHT - 60,
                    290, 60
                ),
                "Continue playing"
            );
            continueButton.action = (nil) -> {
                val objs = instance.objects;
                val length = objs.length - 1;
                val withoutOverlay = new Activity[length];
                System.arraycopy(objs, 0, withoutOverlay, 0, length);
                instance.objects = withoutOverlay;
                val board = (Board) objs[0];
                board.goal = 0;
                board.state = Board.BoardState.PLAYING;
                goalText.message = "Free Play";
                isWithOverlay = false;
            };
            val messageBox = new TextBox(
                new Rectangle(x, y, Board.WIDTH, Board.HEIGHT),
                "You won! :)"
            );
            messageBox.font = Game.font.deriveFont(48f);
            messageBox.color = transparent;
            objects = new Activity[] { messageBox, continueButton };
        }
    }

    private static class DeadOverlay extends OverlayBase {
        public DeadOverlay() {
            val messageBox = new TextBox(
                new Rectangle(x, y, Board.WIDTH, Board.HEIGHT),
                "You lost! :("
            );
            messageBox.font = Game.font.deriveFont(48f);
            messageBox.color = transparent;
            objects = new Activity[] { messageBox };
        }
    }
}
