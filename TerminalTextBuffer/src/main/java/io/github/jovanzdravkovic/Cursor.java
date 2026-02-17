package io.github.jovanzdravkovic;

public class Cursor {
    /**
     * Represents a cursor on a terminal screen using a single position index.
     * The cursor position is tracked as a linear index rather than separate row and column
     * because the screen is implemented as a flat array of cells.
     */

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

    public int getPosition() {
        return this.position;
    }

    public int getRow() {
        return this.position / terminalWidth;
    }

    public int getColumn() {
        return this.position % terminalWidth;
    }

    /**
     * Advances the cursor by the specified number of steps, clamped to screen bounds.
     * Used internally for left/right movement, allowing row wrapping.
     */
    private void addToPosition(int steps) {
        this.position += steps;
        this.position = Math.max(this.position, 0);
        this.position = Math.min(this.position, terminalHeight * terminalWidth - 1);
    }

    /**
     * Sets the cursor to a specific row and column.
     * Values outside the screen are corrected to the nearest valid position.
     */
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

    /**
     * Moves the cursor up by the specified number of steps.
     * The cursor will not move above the first row.
     */
    public void moveUp(int steps) {
        setPosition(this.getRow() - steps, this.getColumn());
    }

    /**
     * Moves the cursor down by the specified number of steps.
     * The cursor will not move below the last row.
     */
    public void moveDown(int steps) {
        setPosition(this.getRow() + steps, this.getColumn());
    }

    /**
     * Moves the cursor left by the specified number of steps.
     * The cursor will not move before the first cell.
     */
    public void moveLeft(int steps) {
        addToPosition(-steps);
    }

    /**
     * Moves the cursor right by the specified number of steps.
     * The cursor will not move beyond the last cell.
     */
    public void moveRight(int steps) {
        addToPosition(steps);
    }

    /** Moves the cursor to the beginning of the current row. */
    public void moveToTheBeginning() {
        setPosition(this.getRow(), 0);
    }

    /** Returns true if the cursor is at the last cell on the screen. */
    public boolean isLastCell() {
        return this.position == (terminalHeight * terminalWidth - 1);
    }

    /**
     * Updates the cursor when the terminal is resized.
     * If the new screen is smaller and the current position is now invalid, the cursor is moved to the last valid cell.
     */
    public void resizeTerminal(int terminalHeight, int terminalWidth) {
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        int maximumPossiblePosition = terminalHeight * terminalWidth - 1;
        this.position = Math.min(this.position, maximumPossiblePosition);
    }
}
