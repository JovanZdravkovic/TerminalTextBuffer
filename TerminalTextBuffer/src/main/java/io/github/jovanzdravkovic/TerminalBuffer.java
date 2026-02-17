package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Cell;
import io.github.jovanzdravkovic.models.Style;

import java.util.Arrays;
import java.util.EnumSet;

public class TerminalBuffer {
    /**
     * Implements terminal text buffer with scrollback history.
     * Supports cursor movement, text insertion and writing, line filling, reading content at position and as a whole, and terminal resizing.
     */

    private int terminalWidth; // Width of the terminal screen
    private int terminalHeight; // Height of the terminal screen
    private int maximumScrollbackLines; // Maximum lines stored in the scrollback buffer
    private byte foregroundColor; // Default foreground color for new text
    private byte backgroundColor; // Default background color for new text
    private EnumSet<Style> styles; // Default text styles for new text
    private int totalScreenSize; // Total number of cells on the screen
    private int filledCellCount; // Number of non-empty cells on the screen
    private Cell[] screen; // Array representing the text buffer
    private Cell[] overflowRow; // Helper row used during cell shifts caused by writing
    private Cursor cursor; // Cursor object that tracks cursor position
    private ScrollbackBuffer scrollbackBuffer; // Scrollback buffer object

    public TerminalBuffer(int terminalHeight, int terminalWidth, int maximumScrollbackLines, byte foregroundColor, byte backgroundColor, EnumSet<Style> styles) {
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        this.totalScreenSize = this.terminalHeight * this.terminalWidth;
        this.filledCellCount = 0;
        this.maximumScrollbackLines = maximumScrollbackLines;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = styles;
        this.screen = new Cell[this.totalScreenSize];
        this.overflowRow = new Cell[this.terminalWidth];
        this.cursor = new Cursor(this.terminalHeight, this.terminalWidth);
        this.scrollbackBuffer = new ScrollbackBuffer(maximumScrollbackLines, terminalHeight, terminalWidth, foregroundColor, backgroundColor, styles);
    }

    public int getTotalScreenSize() {
        return this.totalScreenSize;
    }

    public ScrollbackBuffer getScrollbackBuffer() {
        return this.scrollbackBuffer;
    }

    public void setForegroundColor(byte foregroundColor) {
        this.foregroundColor = foregroundColor;
        if(this.scrollbackBuffer != null) {
            this.scrollbackBuffer.setForegroundColor(foregroundColor);
        }
    }

    public void setBackgroundColor(byte backgroundColor) {
        this.backgroundColor = backgroundColor;
        if(this.scrollbackBuffer != null) {
            this.scrollbackBuffer.setBackgroundColor(backgroundColor);
        }
    }

    public void setStyles(EnumSet<Style> styles) {
        this.styles = styles;
        if(this.scrollbackBuffer != null) {
            this.scrollbackBuffer.setStyles(styles);
        }
    }

    /** Sets the cursor to a specific row and column. */
    public void setCursorPosition(int row, int column) {
        this.cursor.setPosition(row, column);
    }

    public int getCursorPosition() {
        return this.cursor.getPosition();
    }

    /** Moves the cursor up by the given number of rows. */
    public void moveCursorUp(int steps) {
        this.cursor.moveUp(steps);
    }

    /** Moves the cursor down by the given number of rows. */
    public void moveCursorDown(int steps) {
        this.cursor.moveDown(steps);
    }

    /** Moves the cursor left by the given number of columns. */
    public void moveCursorLeft(int steps) {
        this.cursor.moveLeft(steps);
    }

    /** Moves the cursor right by the given number of columns. */
    public void moveCursorRight(int steps) {
        this.cursor.moveRight(steps);
    }

    /**
     * Moves the top line of the screen to the scrollback buffer.
     * Shifts all remaining lines up and fills the bottom line with any overflow row contents.
     */
    private void scrollback() {
        // Move the first line on the screen to the scrollback buffer
        scrollbackBuffer.writeCells(Arrays.copyOfRange(screen, 0, terminalWidth));
        // Shift the rest of the screen lines upwards
        int countFirstLine = 0;
        for(int i = terminalWidth; i < totalScreenSize; i++) {
            if(i - terminalWidth < terminalWidth && screen[i - terminalWidth] != null) {
                countFirstLine++;
            }
            screen[i - terminalWidth] = screen[i];
        }
        // Move the contents of the overflow row into the last line of the screen
        int countOverflow = 0;
        for(int i = totalScreenSize - terminalWidth, j = 0; i < totalScreenSize; i++, j++) {
            if(overflowRow[j] != null) {
                countOverflow++;
            }
            screen[i] = overflowRow[j];
            overflowRow[j] = null;
        }
        // Recalculate the number of non-empty cells on screen
        filledCellCount -= countFirstLine;
        filledCellCount += countOverflow;
    }

    /**
     * Writes text at the current cursor position, advancing the cursor.
     * Scrolls lines into the scrollback buffer if the screen is full.
     * This method corresponds to the operation in the Editing section:
     *  - "Write a text on a line, overriding the current content. Moves the cursor."
     */
    public void write(String text) {
        if(text == null) {
            return ;
        }
        int index = 0;
        while(index < text.length()) {
            if(screen[cursor.getPosition()] == null) {
                filledCellCount++;
            }
            screen[cursor.getPosition()] = new Cell(text.charAt(index), foregroundColor, backgroundColor, styles);
            if(cursor.isLastCell()) {
                scrollback();
                cursor.moveToTheBeginning();
            } else {
                cursor.moveRight(1);
            }
            index++;
        }
    }

    /**
     * Shifts cells in the screen buffer to the right starting from a given index.
     * Cells at the end of the screen that would be pushed past the buffer width
     * are moved into the overflow row. Returns true if such an overflow occurred.
     * Assumes that shiftLength parameter does not exceed terminalWidth field of the class.
     *
     * @param startIndex the index in the screen buffer from which to start shifting
     * @param shiftLength the number of positions to shift cells to the right
     * @return true if an overflow to the overflow row occurred, false otherwise
     */
    private boolean shiftCellsToRight(int startIndex, int shiftLength) {
        if(shiftLength < 1) {
            return false;
        }

        boolean overflowOccured = false;
        int lastNonNullIndex = totalScreenSize - 1;
        while(lastNonNullIndex >= 0 && screen[lastNonNullIndex] == null) {
            lastNonNullIndex--;
        }

        if (lastNonNullIndex == -1) {
            return false;
        }
        if(shiftLength > totalScreenSize - 1 - lastNonNullIndex) {
            overflowOccured = true;
        }

        if(overflowOccured) {
            for(int i = totalScreenSize - shiftLength, overflowIndex = 0; i < totalScreenSize; i++, overflowIndex++) {
                overflowRow[overflowIndex] = screen[i];
            }
        }
        for(int i = totalScreenSize - 1; i - shiftLength >= startIndex; i--) {
            screen[i] = screen[i - shiftLength];
        }

        return overflowOccured;
    }

    /**
     * Inserts text at the current cursor position, shifting existing cells to the right.
     * Text is inserted in batches with maximum size of a single line (terminalWidth).
     * If an overflow occured at the bottom of the terminal because of shifting, scrollback.
     * This method corresponds to the operation in the Editing section:
     *  - "Insert a text on a line, possibly wrapping the line. Moves the cursor."
     *
     * @param text text to insert
     */
    public void insert(String text) {
        if(text == null) {
            return ;
        }
        int index = 0;
        while(index < text.length()) {
            int batchSize = Math.min(text.length() - index, terminalWidth);
            boolean overflowOccured = shiftCellsToRight(cursor.getPosition(), batchSize);
            for(int i = 0; i < batchSize; i++) {
                screen[cursor.getPosition()] = new Cell(text.charAt(index), foregroundColor, backgroundColor, styles);
                cursor.moveRight(1);
                filledCellCount++;
                index++;
            }
            if(overflowOccured) {
                scrollback();
                cursor.moveUp(1);
            }
        }
    }

    /**
     * Fills the row at which cursor is currently at with a given character.
     * This method corresponds to the operation in the Editing section:
     *  - "Fill a line with a character (or empty)."
     */
    public void fillLine(char c) {
        for(int columnIndex = 0; columnIndex < terminalWidth; columnIndex++) {
            int index = cursor.getRow() * terminalWidth + columnIndex;
            if(screen[index] == null) {
                filledCellCount++;
            }
            screen[index] = new Cell(c, foregroundColor, backgroundColor, styles);
        }
    }

    /**
     * Inserts an empty line at the bottom, scrolling the top line into the scrollback buffer.
     * This method corresponds to the operation in the Editing section:
     *  - "Insert an empty line at the bottom of the screen"
     */
    public void insertEmptyLineAtBottom() {
        scrollback();
    }

    /**
     * Clears the screen and resets the cursor to the top-left.
     * This method corresponds to the operation in the Editing section:
     *  - "Clear the entire screen."
     */
    public void clearScreen() {
        this.filledCellCount = 0;
        Arrays.fill(this.screen, null);
        Arrays.fill(this.overflowRow, null);
        cursor.setPosition(0,0);
    }

    /**
     * Clears both the screen and the scrollback buffer.
     * This method corresponds to the operation in the Editing section:
     *  - "Clear the screen and scrollback."
     */
    public void clearScreenAndScrollback() {
        clearScreen();
        scrollbackBuffer.clear();
    }

    /**
     * Returns the character at a specific position on the screen.
     * This method corresponds to the operation in the Content Access section:
     *  - Get character at position from screen
     * @param row zero-indexed row
     * @param column zero-indexed column
     * @return character at the position, or ' ' if empty or out of bounds
     */
    public char charAtPositionScreen(int row, int column) {
        if(row < 0 || row >= terminalHeight || column < 0 || column >= terminalWidth) {
            return ' ';
        }
        if(screen[row * terminalWidth + column] == null) {
            return ' ';
        } else {
            return screen[row * terminalWidth + column].getInformation();
        }
    }

    /**
     * Returns the character at a specific position in the scrollback buffer.
     * This method corresponds to the operation in the Content Access section:
     *  - Get character at position from scrollback
     * @param row zero-indexed row
     * @param column zero-indexed column
     * @return character at the position, or ' ' if empty or out of bounds
     */
    public char charAtPositionScrollback(int row, int column) {
        return this.scrollbackBuffer.charAtPosition(row, column);
    }

    /**
     * Returns the styles at a specific screen position.
     * This method corresponds to the operation in the Content Access section:
     *  - Get attributes at position from screen.
     *
     * @param row zero-indexed row
     * @param column zero-indexed column
     * @return styles at the position, or null if empty or out of bounds
     */
    public EnumSet<Style> stylesAtPositionScreen(int row, int column) {
        if(row < 0 || row >= terminalHeight || column < 0 || column >= terminalWidth) {
            return null;
        }
        if(screen[row * terminalWidth + column] == null) {
            return null;
        } else {
            return screen[row * terminalWidth + column].getStyles();
        }
    }

    /**
     * Returns the styles at a specific position in the scrollback buffer.
     * This method corresponds to the operation in the Content Access section:
     *  - Get attributes at position from scrollback.
     *
     * @param row zero-based row
     * @param column zero-based column
     * @return styles at the position, or null if empty or out of bounds
     */
    public EnumSet<Style> stylesAtPositionScrollback(int row, int column) {
        return this.scrollbackBuffer.stylesAtPosition(row, column);
    }

    /**
     * Returns the line on the specified row on the screen.
     * This method corresponds to the operation in the Content Access section:
     *  -  Get line as string from screen
     *
     * @param row zero-based row
     */
    public String getLineScreen(int row) {
        if(row < 0 || row >= terminalHeight) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int lineStart = row * terminalWidth;
        int lineEnd = row * terminalWidth + terminalWidth - 1;
        for(int i = lineStart; i <= lineEnd; i++) {
            if(screen[i] != null) {
                sb.append(screen[i].getInformation());
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Returns the line on the specified row in the scrollback buffer.
     * This method corresponds to the operation in the Content Access section:
     *  -  Get line as string from scrollback
     *
     * @param row zero-based row
     */
    public String getLineScrollback(int row) {
        return this.scrollbackBuffer.getLine(row);
    }

    /**
     * Returns the entire screen content as a string.
     * This method corresponds to the operation in the Content Access section:
     *  -  "Get entire screen content as string"
     */
    public String getScreenContent() {
        return this.toString();
    }

    /**
     * Returns both the screen and scrollback content as a single string.
     * This method corresponds to the operation in the Content Access section:
     *  -  "Get entire screen+scrollback content as string"
     */
    public String getScreenAndScrollbackContent() {
        return this.toString() + this.scrollbackBuffer.toString();
    }

    /** Recomputes the number of non-empty cells currently on the screen. */
    private int recomputeCount() {
        int cnt = 0;
        for(int i = 0; i < this.totalScreenSize; i++) {
            if(screen[i] != null) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Resize the terminal screen and scrollback buffer.
     * Allocates a new screen array based on the new dimensions.
     * If the new screen is smaller than the current one, top rows are moved to the scrollback buffer.
     * Updates the overflow row and cursor to fit the new terminal size.
     * Recomputes the non-empty cell count if the screen shrinks.
     *
     * @param terminalHeight the new height of the terminal in rows
     * @param terminalWidth  the new width of the terminal in columns
     */
    public void resizeTerminal(int terminalHeight, int terminalWidth) {
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        this.overflowRow = new Cell[terminalWidth];
        scrollbackBuffer.resizeTerminal(this.terminalHeight, this.terminalWidth);
        cursor.resizeTerminal(terminalHeight, terminalWidth);
        int newTotalScreenSize = this.terminalHeight * this.terminalWidth;
        if(this.totalScreenSize > newTotalScreenSize) {
            Cell[] newArray = new Cell[newTotalScreenSize];
            this.scrollbackBuffer.writeCells(Arrays.copyOfRange(screen, 0, this.totalScreenSize - newTotalScreenSize));
            System.arraycopy(screen, this.totalScreenSize - newTotalScreenSize, newArray, 0, newTotalScreenSize);
            this.screen = newArray;
            this.totalScreenSize = newTotalScreenSize;
            this.filledCellCount = recomputeCount();
        } else {
            Cell[] newArray = new Cell[newTotalScreenSize];
            System.arraycopy(this.screen, 0, newArray, 0, this.totalScreenSize);
            this.screen = newArray;
            this.totalScreenSize = newTotalScreenSize;
            // There is no need to recompute the cell count since all the non-empty cells have been moved to the new screen
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < totalScreenSize; i++) {
            if(screen[i] != null) {
                stringBuilder.append(screen[i].getInformation());
            }
        }
        return stringBuilder.toString();
    }
}
