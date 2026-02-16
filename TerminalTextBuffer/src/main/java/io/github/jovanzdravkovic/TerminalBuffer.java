package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Cell;
import io.github.jovanzdravkovic.models.Style;

import java.util.Arrays;
import java.util.EnumSet;

public class TerminalBuffer {

    private int terminalWidth;
    private int terminalHeight;
    private int maximumScrollbackLines;
    private byte foregroundColor;
    private byte backgroundColor;
    private EnumSet<Style> styles;
    private int totalScreenSize;
    private int filledCellCount;
    private Cell[] screen;
    private Cell[] overflowRow;
    private Cursor cursor;
    private ScrollbackBuffer scrollbackBuffer;

    public TerminalBuffer(int terminalWidth, int terminalHeight, int maximumScrollbackLines, byte foregroundColor, byte backgroundColor, EnumSet<Style> styles) {
        this.terminalWidth = terminalWidth;
        this.terminalHeight = terminalHeight;
        this.totalScreenSize = this.terminalHeight * this.terminalWidth;
        this.filledCellCount = 0;
        this.maximumScrollbackLines = maximumScrollbackLines;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = styles;
        this.screen = new Cell[this.totalScreenSize];
        this.overflowRow = new Cell[this.terminalWidth];
        this.cursor = new Cursor(this.terminalHeight, this.terminalWidth);
        this.scrollbackBuffer = new ScrollbackBuffer(maximumScrollbackLines, terminalHeight, terminalWidth);
    }

    public void setForegroundColor(byte foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public void setBackgroundColor(byte backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setStyles(EnumSet<Style> styles) {
        this.styles = styles;
    }

    public void setCursorPosition(int row, int column) {
        this.cursor.setPosition(row, column);
    }

    public void moveCursorUp(int steps) {
        this.cursor.moveUp(steps);
    }

    public void moveCursorDown(int steps) {
        this.cursor.moveDown(steps);
    }

    public void moveCursorLeft(int steps) {
        this.cursor.moveLeft(steps);
    }

    public void moveCursorRight(int steps) {
        this.cursor.moveRight(steps);
    }

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

    public void write(String text) {
        if(text == null) {
            return ;
        }
        int index = 0;
        while(index < text.length()) {
            screen[cursor.getPosition()] = new Cell(text.charAt(index), foregroundColor, backgroundColor, styles);
            filledCellCount++;
            if(index < text.length() - 1 && cursor.isLastCell()) {
                scrollback();
                cursor.moveToTheBeginning();
            } else {
                cursor.moveRight(1);
            }
            index++;
        }
    }

    // This function returns a flag if an overflow occurred because of shifting.
    // It also expects that the shiftLength argument will not exceed terminalWidth value
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

    public void fillLine(char c) {
        for(int columnIndex = 0; columnIndex < terminalWidth; columnIndex++) {
            int index = cursor.getRow() * terminalWidth + columnIndex;
            if(screen[index] == null) {
                filledCellCount++;
            }
            screen[index] = new Cell(c, foregroundColor, backgroundColor, styles);
        }
    }

    public void insertEmptyLineAtBottom() {
        scrollback();
    }

    public void clearScreen() {
        this.filledCellCount = 0;
        Arrays.fill(this.screen, null);
        Arrays.fill(this.overflowRow, null);
        cursor.setPosition(0,0);
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollbackBuffer.clear();
    }

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

    public char charAtPositionScrollback(int row, int column) {
        return this.scrollbackBuffer.charAtPosition(row, column);
    }

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

    public EnumSet<Style> stylesAtPositionScrollback(int row, int column) {
        return this.scrollbackBuffer.stylesAtPosition(row, column);
    }

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

    public String getLineScrollback(int row) {
        return this.scrollbackBuffer.getLine(row);
    }

    public String getScreenContent() {
        return this.toString();
    }

    public String getScreenAndScrollbackContent() {
        return this.toString() + this.scrollbackBuffer.toString();
    }

    public void resizeTerminal(int terminalHeight, int terminalWidth) {
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        scrollbackBuffer.resizeTerminal(this.terminalHeight, this.terminalWidth);
        cursor.resizeTerminal(terminalHeight, terminalWidth);
        // TODO: implement screen resizing logic here
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < totalScreenSize; i++) {
            if(screen[i] != null) {
                stringBuilder.append(screen[i].getInformation());
            } else {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }
}
