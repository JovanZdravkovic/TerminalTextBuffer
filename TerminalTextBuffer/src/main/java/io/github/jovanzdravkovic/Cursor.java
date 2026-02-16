package io.github.jovanzdravkovic;

public class Cursor {
    private int position;
    private int terminalWidth;
    private int terminalHeight;

    public Cursor(int position, int terminalHeight, int terminalWidth) {
        this.position = position;
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
    }

    public Cursor(int terminalHeight, int terminalWidth) {
        this(0,  terminalHeight, terminalWidth);
    }

    public void resizeTerminal(int terminalHeight, int terminalWidth) {
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        int maximumPossiblePosition = terminalHeight * terminalWidth - 1;
        this.position = Math.min(this.position, maximumPossiblePosition);
    }

    public int getRow() {
        return this.position / terminalWidth;
    }

    public int getColumn() {
        return this.position % terminalWidth;
    }

    private void addToPosition(int steps) {
        this.position += steps;
        this.position = Math.max(this.position, 0);
        this.position = Math.min(this.position, terminalHeight * terminalWidth - 1);
    }

    public void setPosition(int row, int column) {
        if(row < 0) {
            row = 0;
        }
        if(row >= terminalHeight) {
            row = terminalHeight - 1;
        }
        if(column < 0) {
            column = 0;
        }
        if(column >= terminalWidth) {
            column = terminalWidth - 1;
        }
        this.position = row * terminalWidth + column;
    }

    public int getPosition() {
        return this.position;
    }

    public void moveUp(int steps) {
        setPosition(this.getRow() - steps, this.getColumn());
    }

    public void moveDown(int steps) {
        setPosition(this.getRow() + steps, this.getColumn());
    }

    public void moveLeft(int steps) {
        addToPosition(-steps);
    }

    public void moveRight(int steps) {
        addToPosition(steps);
    }

    public void moveToTheBeginning() {
        setPosition(this.getRow(), 0);
    }

    public boolean isLastCell() {
        return this.position == (terminalHeight * terminalWidth - 1);
    }
}
