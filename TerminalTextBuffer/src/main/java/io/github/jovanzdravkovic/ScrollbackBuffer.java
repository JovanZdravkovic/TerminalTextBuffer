package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Cell;
import io.github.jovanzdravkovic.models.Style;

import java.util.Arrays;
import java.util.EnumSet;

public class ScrollbackBuffer {
    /**
     * A scrollback buffer implemented as a ring buffer of Cell objects.
     * The buffer stores previously scrolled-off screen content in a fixed-size
     * circular array. New cells overwrite the oldest ones when the buffer is full.
     * This implementation is not thread-safe and is intended only for single-threaded use.
     */

    private int terminalHeight;
    private int terminalWidth;
    private byte foregroundColor;
    private byte backgroundColor;
    private EnumSet<Style> styles;

    // Maximum number of lines stored in the scrollback buffer.
    private int maximumScrollbackLines;

    // Total size of the underlying buffer array
    private int maximumScrollbackSize;
    private Cell[] buffer;

    // Index of the next cell to be written in the ring buffer.
    private int writeIndex;

    // Number of non-empty cells currently in the buffer
    private int count;

    public ScrollbackBuffer(
            int maximumScrollbackLines,
            int terminalHeight,
            int terminalWidth,
            byte foregroundColor,
            byte backgroundColor,
            EnumSet<Style> styles
    ) {
        this.maximumScrollbackLines = maximumScrollbackLines;
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = styles;
        this.maximumScrollbackSize = maximumScrollbackLines * terminalWidth;
        this.buffer = new Cell[maximumScrollbackSize];
        this.writeIndex = 0;
        this.count = 0;
    }

    public int getMaximumScrollbackLines() {
        return this.maximumScrollbackLines;
    }

    public int getMaximumScrollbackSize() {
        return this.maximumScrollbackSize;
    }

    public int getCount() {
        return this.count;
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

    /**
     * Returns the index of the oldest cell in the ring buffer.
     */
    private int startIndex() {
        return (writeIndex - count + maximumScrollbackSize) % maximumScrollbackSize;
    }

    /**
     * Resizes the scrollback buffer and preserves its content when the terminal dimensions change.
     * A new buffer array is allocated, and existing cells are copied into it.
     * If the new buffer is smaller than the old one, only the most recent cells that fit are copied.
     */
    public void resizeTerminal(int newTerminalHeight, int newTerminalWidth) {
        int newBufferMaximumSize = maximumScrollbackLines * newTerminalWidth;
        Cell[] newBuffer = new Cell[newBufferMaximumSize];
        int newBufferIterator = 0;
        int oldBufferIterator = startIndex();

        for(int i = 0; i < count; i++) {
            newBuffer[newBufferIterator] = buffer[oldBufferIterator];
            newBufferIterator = (newBufferIterator + 1) % newBufferMaximumSize;
            oldBufferIterator = increment(oldBufferIterator);
        }

        this.terminalHeight = newTerminalHeight;
        this.terminalWidth = newTerminalWidth;
        this.maximumScrollbackSize = newBufferMaximumSize;
        this.writeIndex = newBufferIterator;
        this.count = Math.min(this.count, newBufferMaximumSize);
        this.buffer = newBuffer;
    }

    /**
     * Fills the buffer with empty cells and resets the count and writeIndex fields.
     */
    public void clear() {
        Arrays.fill(this.buffer, null);
        this.writeIndex = 0;
        this.count = 0;
    }

    /**
     * Writes an array of cells into the scrollback buffer.
     * Null elements are converted to empty cells in order to preserve history structure of the buffer.
     * Count is updated and capped at the maximum buffer size, because when the buffer is full old cells are overwritten.
     */
    public void writeCells(Cell[] cells) {
        for (Cell cell : cells) {
            if (cell != null) {
                buffer[writeIndex] = cell;
            } else {
                buffer[writeIndex] = new Cell(' ', foregroundColor, backgroundColor, styles);
            }
            writeIndex = increment(writeIndex);
        }
        this.count = Math.min(this.count + cells.length, maximumScrollbackSize);
    }

    /**
     * Returns the index of the next element in the ring buffer.
     * Used for traversing the ring buffer for read and write operations.
     */
    private int increment(int x) {
        return (x + 1) % maximumScrollbackSize;
    }

    /**
     * Converts a logical scrollback index to the actual index in the ring buffer.
     * The logical index is zero-based, starting from the oldest cell.
     */
    private int realIndex(int logicalIndex) {
        return (startIndex() + logicalIndex) % maximumScrollbackSize;
    }

    /**
     * Returns the character at the specified row and column in the scrollback buffer.
     * Rows are counted from the oldest line in the buffer.
     * If the specified position is outside the scrollback buffer, a space character ' ' is returned.
     */
    public char charAtPosition(int row, int column) {
        int lineCount = count / terminalWidth;
        if(row < 0 || row >= lineCount || column < 0 || column >= terminalWidth) {
            return ' ';
        }
        int index = realIndex(row * terminalWidth + column);
        if(buffer[index] == null) {
            return ' ';
        } else {
            return buffer[index].getInformation();
        }
    }

    /**
     * Returns the styles at the specified row and column in the scrollback buffer.
     * Rows are counted from the oldest line in the buffer.
     * If the specified position is outside the scrollback buffer, null is returned.
     */
    public EnumSet<Style> stylesAtPosition(int row, int column) {
        int lineCount = count / terminalWidth;
        if(row < 0 || row >= lineCount || column < 0 || column >= terminalWidth) {
            return null;
        }
        int index = realIndex(row * terminalWidth + column);
        if(buffer[index] == null) {
            return null;
        } else {
            return buffer[index].getStyles();
        }
    }

    /**
     * Returns the content of the specified row in the scrollback buffer as a string.
     * Rows are counted from the oldest line in the buffer.
     * If the row corresponds to a partial line (not completely filled), only
     * the valid characters in that line are returned.
     * Returns null if the row is outside the valid range.
     */
    public String getLine(int row) {
        int lineCount = count / terminalWidth;
        if(count % terminalWidth > 0) {
            lineCount++;
        }
        if(row < 0 || row >= lineCount) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int lineIndex = realIndex(row * terminalWidth);
        // Use actual content length for the last line (if it is partial), otherwise use terminal width.
        int lineWidth = row == lineCount - 1 && count % terminalWidth > 0 ? count % terminalWidth : terminalWidth;

        for(int i = 0; i < lineWidth; i++) {
            if(buffer[lineIndex] != null) {
                sb.append(buffer[lineIndex].getInformation());
            } else {
                sb.append(' ');
            }
            lineIndex = increment(lineIndex);
        }

        return sb.toString();
    }

    /**
     * Returns the scrollback buffer content as a string.
     * The content string is built on demand by traversing the buffer,
     * since write operations are expected to be more frequent
     * than full-buffer reads.
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int bufferIterator = startIndex();
        for(int i = 0; i < count; i++) {
            if(buffer[bufferIterator] != null) {
                stringBuilder.append(buffer[bufferIterator].getInformation());
            }
            bufferIterator = increment(bufferIterator);
        }
        return stringBuilder.toString();
    }
}
