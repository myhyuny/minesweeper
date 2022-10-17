package myhyuny.game.minesweeper;

import static java.awt.EventQueue.invokeLater;
import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.KeyEvent.KEY_RELEASED;
import static java.awt.event.KeyEvent.VK_META;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Hyunmin Kang
 */
public final class Application extends Panel { // Applet {

    public static final int CELL_SIZE = 24;

    private static final byte BUTTON_NONE = 0x0;
    private static final byte BUTTON_LEFT = 0x1;
    private static final byte BUTTON_MIDDLE = 0x1 << 1;
    private static final byte BUTTON_RIGHT = 0x1 << 2;

    private static final Color[] NUMBER_COLORS = {
        Color.BLUE, new Color(0x008000), Color.RED, new Color(0x000080), new Color(0x800000),
        new Color(0x008080), Color.BLACK, Color.GRAY
    };

    private static final BevelBorder RAISED_BUTTON = new BevelBorder(BevelBorder.RAISED);
    private static final BevelBorder LOWERED_BUTTON = new BevelBorder(BevelBorder.LOWERED);

    private static final ScheduledExecutorService schedule = newSingleThreadScheduledExecutor();

    private final Application self = this;

    private final int width;
    private final int height;
    private final int mines;

    private final Label minesLabel = new Label();
    private final Label timerLabel = new Label("0");
    private final Label messageLabel = new Label("https://github.com/myhyuny/minesweeper");
    private JButton[][] buttons;
    private Panel centerPanel;
    private Frame frame;

    private ScheduledFuture<?> timer = null;

    private Minesweeper minesweeper;

    public Application(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
    }

    public Application() {
        this(0, 0, 0);
    }

    private boolean metaKey = false;

    public void init() {
        minesweeper = new Minesweeper(width, height, mines);

        Button restartButton = new Button("Restart");
        buttons = new JButton[minesweeper.getHeight()][minesweeper.getWidth()];

        minesLabel.setText(String.valueOf(minesweeper.getMines()));
        minesLabel.setAlignment(Label.CENTER);
        timerLabel.setAlignment(Label.CENTER);

        Panel panel = new Panel(new GridLayout(1, 2));
        Panel northPanel = new Panel(new BorderLayout());
        panel.add(minesLabel);
        panel.add(restartButton);
        panel.add(timerLabel);
        northPanel.add(new Panel(), BorderLayout.SOUTH);
        northPanel.add(new Panel(), BorderLayout.NORTH);
        northPanel.add(panel, BorderLayout.CENTER);

        MouseAdapter buttonMouseHandler = new MouseAdapter() {
            private byte pressed = BUTTON_NONE;

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1:
                        pressed |= BUTTON_LEFT;
                        break;
                    case MouseEvent.BUTTON2:
                        pressed |= BUTTON_MIDDLE;
                        break;
                    case MouseEvent.BUTTON3:
                        pressed |= BUTTON_RIGHT;
                        break;
                }
                if (metaKey) {
                    pressed |= BUTTON_RIGHT;
                }

                switch (pressed) {
                    case BUTTON_MIDDLE:
                    case BUTTON_LEFT | BUTTON_RIGHT:
                        CellButton b = (CellButton) e.getComponent();
                        minesweeper.press(b.rows, b.cols, (y, x) -> buttons[y][x].setBorder(LOWERED_BUTTON));
                        break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                CellButton b = (CellButton) e.getComponent();
                if (minesweeper.isStart()) {
                    switch (pressed) {
                        case BUTTON_LEFT:
                            minesweeper.select(b.rows, b.cols);
                            break;
                        case BUTTON_RIGHT:
                            minesweeper.changeFlag(b.rows, b.cols);
                            break;
                        case BUTTON_MIDDLE:
                        case BUTTON_LEFT | BUTTON_RIGHT:
                            minesweeper.exploreMine(b.rows, b.cols, (y, x) -> buttons[y][x].setBorder(RAISED_BUTTON));
                            break;
                    }
                } else if (pressed == BUTTON_LEFT) {
                    minesweeper.start(b.rows, b.cols);
                }
                pressed = BUTTON_NONE;
            }
        };

        centerPanel = new Panel(new GridLayout(minesweeper.getHeight(), minesweeper.getWidth()));
        for (int y = 0; y < minesweeper.getHeight(); y++) {
            for (int x = 0; x < minesweeper.getWidth(); x++) {
                CellButton button = new CellButton(y, x);
                button.addMouseListener(buttonMouseHandler);
                button.setBorder(new BevelBorder(BevelBorder.RAISED));
                buttons[y][x] = button;
                centerPanel.add(button);
            }
        }

        centerPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("keyPressed");
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("keyReleased");
            }
        });

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        restartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (minesweeper.isStart() || minesweeper.isOver()) {
                    minesweeper.restart();
                } else if (frame != null) {
                    new NewGameDialog();
                    frame.setVisible(false);
                    frame.removeAll();
                    frame.dispose();
                }
            }
        });


        if (isMac()) {
            getToolkit().addAWTEventListener(event -> {
                KeyEvent e = (KeyEvent) event;
                if (e.getKeyCode() != VK_META) {
                    return;
                }
                switch (e.getID()) {
                    case KEY_PRESSED:
                        metaKey = true;
                        break;
                    case KEY_RELEASED:
                        metaKey = false;
                        break;
                }
            }, AWTEvent.KEY_EVENT_MASK);
        }

        minesweeper
            .setStartedListener(() -> {
                messageLabel.setText("");
                long startTime = currentTimeMillis();
                timer = schedule.scheduleAtFixedRate(() -> invokeLater(() ->
                    timerLabel.setText(Long.toString((currentTimeMillis() - startTime) / 1000L))
                ), 0L, 1000L, TimeUnit.MILLISECONDS);
            })
            .setStoppedListener(() -> {
                if (timer != null) {
                    timer.cancel(false);
                }
            })
            .setOverListener(clear -> messageLabel.setText(clear ? "Clear!" : "Game over"))
            .setRestartListener(() -> {
                minesLabel.setText(Integer.toString(minesweeper.getMines()));
                timerLabel.setText("0");
                messageLabel.setText("New game");
                for (int y = 0; y < minesweeper.getHeight(); y++) {
                    for (int x = 0; x < minesweeper.getWidth(); x++) {
                        JButton button = buttons[y][x];
                        button.setBorder(RAISED_BUTTON);
                        button.setForeground(Color.BLACK);
                        button.setText("");
                    }
                }
            })
            .setOpenedListener((rows, cols, cell) -> {
                JButton button = buttons[rows][cols];
                button.setBorder(LOWERED_BUTTON);
                if (cell.isMine()) {
                    button.setText("X");
                } else if (cell.isNearby()) {
                    button.setText(String.valueOf(cell.getNearby()));
                    button.setForeground(NUMBER_COLORS[cell.getNearby() - 1]);
                }
            })
            .setExplodedListener((rows, cols) -> {
                JButton button = buttons[rows][cols];
                button.setBorder(LOWERED_BUTTON);
                button.setForeground(Color.RED);
                button.setText("*");
            })
            .setFlagListener((rows, cols, flag) -> {
                JButton button = buttons[rows][cols];
                switch (flag) {
                    case Cell.FLAG_NONE:
                        button.setForeground(Color.RED);
                        button.setText("▶");
                        break;
                    case Cell.FLAG_UP:
                        button.setForeground(Color.BLACK);
                        button.setText("?");
                        break;
                    case Cell.FLAG_QUESTION:
                        button.setText("");
                        break;
                }
                minesLabel.setText(Integer.toString(minesweeper.getFlag()));
            });
    }

    public void exit() {
        schedule.shutdown();
        frame.setVisible(false);
        frame.removeAll();
        frame.dispose();
        System.exit(0);
    }

    public void start() {
        frame = new Frame("Minesweeper");
        frame.add(this);
        init();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            private int frameWidth;
            private int frameHeight;
            private boolean initialized = false;

            @Override
            public void componentShown(ComponentEvent e) {
                Component c = e.getComponent();
                c.setSize(0, 0);

                invokeLater(() -> {
                    frameWidth = c.getWidth() - self.getWidth();
                    frameHeight = c.getHeight() - centerPanel.getHeight();

                    resized(e, Application.CELL_SIZE);

                    initialized = true;
                });
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (!initialized) {
                    return;
                }
                int width = centerPanel.getWidth() / minesweeper.getWidth();
                int height = centerPanel.getHeight() / minesweeper.getHeight();
                resized(e, max(Application.CELL_SIZE, min(width, height)));
            }

            private void resized(ComponentEvent e, int cellSize) {
                int width = frameWidth + cellSize * minesweeper.getWidth();
                int height = frameHeight + cellSize * minesweeper.getHeight();
                e.getComponent().setSize(width, height);
            }
        });

        frame.setVisible(true);
    }

    static boolean isMac() {
        return System.getProperty("os.name").contains("Mac");
    }

    public static void main(String[] args) {
        new Application().start();
    }

}
