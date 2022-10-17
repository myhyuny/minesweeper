package myhyuny.game.minesweeper;

/**
 * @author Hyunmin Kang
 */
@FunctionalInterface
public interface LocationConsumer {
    void accept(int cols, int rows);
}
