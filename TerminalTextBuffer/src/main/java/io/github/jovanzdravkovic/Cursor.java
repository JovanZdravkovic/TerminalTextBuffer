package io.github.jovanzdravkovic;

public class Cursor {
    private int position;
    private int terminalWidth;
    private int terminalHeight;

    public Cursor(int position, int terminalWidth, int terminalHeight) {
        this.position = position;
        this.terminalWidth = terminalWidth;
        this.terminalHeight = terminalHeight;
    }

    public Cursor(int terminalWidth, int terminalHeight) {
        this(0, terminalWidth, terminalHeight);
    }

    public int getRow() {
        return this.position / terminalWidth;
    }

    public int getColumn() {
        return this.position % terminalWidth;
    }

    public void setPosition(int row, int column) {
        if(row < 0 || row >= terminalHeight || column < 0 || row >= terminalWidth) {
            throw new IllegalArgumentException("Row or column has invalid value");
        }
        this.position = row * terminalWidth + column;
    }

    public boolean moveUp(int steps) {
        try {
            setPosition(this.getRow() - steps, this.getColumn());
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean moveDown(int steps) {
        try {
            setPosition(this.getRow() + steps, this.getColumn());
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean moveLeft(int steps) {
        try {
            setPosition(this.getRow(), this.getColumn() - steps);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean moveRight(int steps) {
        try {
            setPosition(this.getRow(), this.getColumn() + steps);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
