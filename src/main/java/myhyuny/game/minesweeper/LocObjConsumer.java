package myhyuny.game.minesweeper;

/**
 * @author Hyunmin Kang
 */
@FunctionalInterface
public interface LocObjConsumer<T> {
    void accept(int cols, int rows, T t);
}
