package myhyuny.game.minesweeper;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author Hyunmin Kang
 */
public class Minesweeper {

    public static final int MIN_WIDTH = 9;
    public static final int MAX_WIDTH = 30;

    public static final int MIN_HEIGHT = 9;
    public static final int MAX_HEIGHT = 24;

    public static final int MIN_MINES = 10;
    public static final int MAX_MINES = 667;

    private final int mines;
    private final int width;
    private final int height;

    private Cell[][] cells;
    private int closes = 0;
    private int flag;
    private boolean start = false;
    private boolean over = false;

    private Runnable startedListener = () -> {};

    private Runnable stoppedListener = () -> {};

    private Consumer<Boolean> overListener = (c) -> {};

    private Runnable restartListener = () -> {};

    private LocObjConsumer<Cell> openedListener = (c, r, e) -> {};

    private LocObjConsumer<Byte> flagListener = (c, r, f) -> {};

    private LocationConsumer explodedListener = (c, r) -> {};

    public Minesweeper(int width, int height, int mines) {
        this.width = min(max(width, MIN_WIDTH), MAX_WIDTH);
        this.height = min(max(height, MIN_HEIGHT), MAX_HEIGHT);
        int area = this.width * this.height;
        this.mines = min(max(mines, MIN_MINES), maxMines(area));
        this.flag = this.mines;
    }

    public static int maxMines(int mines) {
        return min((int) round(mines / (639d / 601d) - 9), MAX_MINES);
    }

    public void select(int rows, int cols) {
        Cell cell = cells[rows][cols];
        if (cell.isOpened()) {
            return;
        }
        if (!over && cell.getFlag() != Cell.FLAG_UP) {
            open(rows, cols);
            if (cell.isEmpty()) {
                selectEmpty(rows, cols);
            } else if (cell.isMine()) {
                selectMine();
            }
        }
        if (closes == mines) {
            over(true);
        }
    }

    private void open(int rows, int cols) {
        Cell cell = cells[rows][cols];
        if (cell.isOpened()) {
            return;
        }

        closes--;
        cell.setOpened();
        openedListener.accept(rows, cols, cell);
    }

    private void selectEmpty(int rows, int cols) {

        class Coord {
            final int y, x;

            Coord(int y, int x) {
                this.y = y;
                this.x = x;
            }
        }

        Deque<Coord> stack = new ArrayDeque<>();
        stack.push(new Coord(rows, cols));

        next:
        while (stack.size() > 0) {
            Coord coord = stack.pop();

            for (int y = coord.y - 1; y < coord.y + 2; y++) {
                if (y < 0 || height <= y) {
                    continue;
                }

                for (int x = coord.x - 1; x < coord.x + 2; x++) {
                    if (x < 0 || width <= x) {
                        continue;
                    }

                    Cell cell = cells[y][x];
                    if (cell.isOpened()) {
                        continue;
                    }

                    if (cell.isEmpty()) {
                        stack.add(coord);
                        stack.add(new Coord(y, x));
                        open(y, x);
                        continue next;
                    }

                    open(y, x);
                }
            }
        }
    }

    private void selectMine() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells[y][x].isMine()) {
                    explodedListener.accept(y, x);
                }
            }
        }
        over(false);
    }

    public void changeFlag(int rows, int cols) {
        Cell cell = cells[rows][cols];
        if (over || cell.isOpened()) {
            return;
        }

        switch (cell.getFlag()) {
            case Cell.FLAG_NONE:
                flag--;
                break;
            case Cell.FLAG_UP:
                flag++;
                break;
        }

        flagListener.accept(rows, cols, cell.getFlag());
        cell.changeFlag();
    }

    public void press(int rows, int cols, LocationConsumer handler) {
        loop(rows, cols, (y, x) -> {
            if (start && !over && cells[y][x].getFlag() != Cell.FLAG_UP) {
                handler.accept(y, x);
            }
        });
    }

    private void loop(int rows, int cols, LocationConsumer consumer) {
        for (int y = rows - 1; y < rows + 2; y++) {
            for (int x = cols - 1; x < cols + 2; x++) {
                if (0 <= y && y < height && 0 <= x && x < width) {
                    consumer.accept(y, x);
                }
            }
        }
    }

    public void exploreMine(int rows, int cols, LocationConsumer handler) {
        Cell cell = cells[rows][cols];
        if (cell.isOpened() && cell.isNearby()) {
            AtomicInteger flags = new AtomicInteger();

            loop(rows, cols, (y, x) -> {
                if (cells[y][x].getFlag() == Cell.FLAG_UP) {
                    flags.incrementAndGet();
                }
            });

            if (cell.getNearby() == flags.get()) {
                loop(rows, cols, (y, x) -> {
                    if (cells[y][x].getFlag() != Cell.FLAG_UP) {
                        select(y, x);
                    }
                });
            }
        }

        loop(rows, cols, (y, x) -> {
            if (!start || !cells[y][x].isOpened()) {
                handler.accept(y, x);
            }
        });
    }

    private void buried(int rows, int cols) {
        cells = new Cell[height][width];
        closes = height * width;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell();
            }
        }

        int max = width * height;
        List<Integer> locations = IntStream.range(0, max--).boxed().collect(toList());
        locations.remove(rows * width + cols);

        for (int i = 0; i < mines; i++) {
            int location = locations.remove(ThreadLocalRandom.current().nextInt(max--));
            int r = location / width, c = location % width;
            cells[r][c].setMine();

            for (int y = r - 1; y < r + 2; y++) {
                for (int x = c - 1; x < c + 2; x++) {
                    if (0 <= y && y < height && 0 <= x && x < width) {
                        cells[y][x].incrementNearby();
                    }
                }
            }
        }
    }

    public void start(int rows, int cols) {
        buried(rows, cols);
        select(rows, cols);
        start = true;
        startedListener.run();
    }

    public void stop() {
        over = true;
        stoppedListener.run();
    }

    private void over(boolean clear) {
        stop();
        overListener.accept(clear);
    }

    public void restart() {
        stop();
        over = false;
        start = false;
        flag = mines;
        restartListener.run();
    }

    public int getMines() {
        return mines;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isOver() {
        return over;
    }

    public int getFlag() {
        return flag;
    }

    public Minesweeper setStartedListener(Runnable listener) {
        startedListener = listener;
        return this;
    }

    public Minesweeper setStoppedListener(Runnable listener) {
        stoppedListener = listener;
        return this;
    }

    public Minesweeper setOverListener(Consumer<Boolean> listener) {
        overListener = listener;
        return this;
    }

    public Minesweeper setRestartListener(Runnable listener) {
        restartListener = listener;
        return this;
    }

    public Minesweeper setOpenedListener(LocObjConsumer<Cell> listener) {
        openedListener = listener;
        return this;
    }

    public Minesweeper setExplodedListener(LocationConsumer listener) {
        explodedListener = listener;
        return this;
    }

    public Minesweeper setFlagListener(LocObjConsumer<Byte> listener) {
        flagListener = listener;
        return this;
    }

}

