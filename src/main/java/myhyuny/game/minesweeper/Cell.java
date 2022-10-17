package myhyuny.game.minesweeper;

/**
 * @author Hyunmin Kang
 */
public class Cell {

    public static final byte FLAG_NONE = 0;
    public static final byte FLAG_UP = 1;
    public static final byte FLAG_QUESTION = 2;

    private byte nearby = 0;
    private byte flag = 0;
    private boolean opened = false;
    private boolean mine = false;

    public byte getNearby() {
        return nearby;
    }

    public boolean isNearby() {
        return 0 < nearby && nearby < 9;
    }

    public boolean isEmpty() {
        return nearby < 1;
    }

    protected void incrementNearby() {
        nearby++;
    }

    public boolean isMine() {
        return mine;
    }

    protected void setMine() {
        this.mine = true;
    }

    public byte getFlag() {
        return flag;
    }

    protected void changeFlag() {
        flag = (byte) ((flag + 1) % 3);
    }

    public boolean isOpened() {
        return opened;
    }

    protected void setOpened() {
        this.opened = true;
    }

}
