package myhyuny.game.minesweeper;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.lang.Integer.parseInt;
import static myhyuny.game.minesweeper.Application.isMac;
import static myhyuny.game.minesweeper.Minesweeper.MAX_HEIGHT;
import static myhyuny.game.minesweeper.Minesweeper.MAX_MINES;
import static myhyuny.game.minesweeper.Minesweeper.MAX_WIDTH;
import static myhyuny.game.minesweeper.Minesweeper.MIN_HEIGHT;
import static myhyuny.game.minesweeper.Minesweeper.MIN_MINES;
import static myhyuny.game.minesweeper.Minesweeper.MIN_WIDTH;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Hyunmin Kang
 */
final class NewGameDialog extends Frame {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 120;

    private final Panel panel = new Panel(new BorderLayout());
    private final TextField widthTextField = new TextField();
    private final TextField heightTextText = new TextField();
    private final TextField minesTextField = new TextField();

    public NewGameDialog() {
        init();
    }

    private void init() {
        setTitle("Minesweeper");
        setSize(WIDTH, HEIGHT);
        setResizable(false);

        Panel westPanel = new Panel(new GridLayout(3, 1));
        westPanel.add(new Label(" Width (" + MIN_WIDTH + '~' + MAX_WIDTH + "): "));
        westPanel.add(new Label(" Height (" + MIN_HEIGHT + '~' + MAX_HEIGHT + "): "));
        westPanel.add(new Label(" Mines (" + MIN_MINES + '~' + MAX_MINES + "): "));

        Panel centerPanel = new Panel(new GridLayout(3, 1));
        centerPanel.add(widthTextField);
        centerPanel.add(heightTextText);
        centerPanel.add(minesTextField);

        Button startButton = new Button("Start");
        Button cancelButton = new Button("Cancel");
        Button beginnerButton = new Button("Beginner");
        Button intermediateButton = new Button("Intermediate");
        Button advancedButton = new Button("Expert");
        startButton.setEnabled(false);

        Panel southPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(beginnerButton);
        southPanel.add(intermediateButton);
        southPanel.add(advancedButton);
        southPanel.add(new Panel());
        if (isMac) {
            southPanel.add(cancelButton);
            southPanel.add(startButton);
        } else {
            southPanel.add(startButton);
            southPanel.add(cancelButton);
        }

        panel.add(westPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        add(panel);

        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = e.getKeyChar();
                if (key != VK_BACK_SPACE && (key < VK_0 || VK_9 < key)) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                boolean enabled = false;
                try {
                    int width = parseInt(widthTextField.getText());
                    if (width < MIN_WIDTH || MAX_WIDTH < width) {
                        return;
                    }
                    int height = parseInt(heightTextText.getText());
                    if (height < MIN_HEIGHT || MAX_HEIGHT < height) {
                        return;
                    }
                    int mines = parseInt(minesTextField.getText());
                    if (mines < MIN_MINES || MAX_MINES < mines) {
                        return;
                    }
                    enabled = true;
                } catch (NumberFormatException ignore) {
                } finally {
                    startButton.setEnabled(enabled);
                }
                if (enabled && e.getKeyChar() == VK_ENTER) {
                    start();
                }
            }
        };

        widthTextField.addKeyListener(keyListener);
        heightTextText.addKeyListener(keyListener);
        minesTextField.addKeyListener(keyListener);

        beginnerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                start(MIN_WIDTH, Minesweeper.MIN_HEIGHT, Minesweeper.MIN_MINES);
            }
        });
        intermediateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                start(16, 16, 30);
            }
        });
        advancedButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                start(30, 16, 99);
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                exit();
            }
        });
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                start();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                int width = getWidth() - panel.getWidth();
                int height = getHeight() - panel.getHeight();
                setSize(width + WIDTH, height + HEIGHT);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        setVisible(true);
    }

    private void start() {
        int width = parseInt(widthTextField.getText());
        int height = parseInt(heightTextText.getText());
        int mines = parseInt(minesTextField.getText());
        start(width, height, mines);
    }

    private void start(int width, int height, int mines) {
        new Application(width, height, mines).start();
        setVisible(false);
        removeAll();
        dispose();
    }

    private void exit() {
        setVisible(false);
        removeAll();
        dispose();
        System.exit(0);
    }

}
