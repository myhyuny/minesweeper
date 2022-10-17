package myhyuny.game.minesweeper;

import javax.swing.JButton;

/**
 * @author Hyunmin Kang
 */
final class CellButton extends JButton {

    public final int rows;
    public final int cols;

    CellButton(int rows, int cols) {
        super();
        this.rows = rows;
        this.cols = cols;
    }

}
