package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Cell;
import io.github.jovanzdravkovic.models.Style;

import java.util.Arrays;
import java.util.EnumSet;

public class ScrollbackBuffer {
    private int terminalHeight;
    private int terminalWidth;
    private int maximumScrollbackSize;
    private int maximumScrollbackLines; // maximum size in lines
    private Cell[] buffer;
    private int writeIndex;
    private int count;

    // Note: This buffer is written for single thread applications
    public ScrollbackBuffer(int maximumScrollbackLines, int terminalHeight, int terminalWidth) {
        this.maximumScrollbackLines = maximumScrollbackLines;
        this.terminalHeight = terminalHeight;
        this.terminalWidth = terminalWidth;
        this.maximumScrollbackSize = maximumScrollbackLines * terminalWidth;
        this.buffer = new Cell[maximumScrollbackSize];
        this.writeIndex = 0;
        this.count = 0;
    }

    private int startIndex() {
        return (writeIndex - count + maximumScrollbackSize) % maximumScrollbackSize;
    }

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

    public void clear() {
        Arrays.fill(this.buffer, null);
        this.writeIndex = 0;
        this.count = 0;
    }

    public void writeCells(Cell[] cells) {
        for(int i = 0; i < cells.length; i++) {
            buffer[writeIndex] = cells[i];
            writeIndex = increment(writeIndex);
        }
        this.count = Math.min(this.count + cells.length, maximumScrollbackSize);
    }

    private int increment(int x) {
        return (x + 1) % maximumScrollbackSize;
    }

    private int realIndex(int logicalIndex) {
        return (startIndex() + logicalIndex) % maximumScrollbackSize;
    }

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

    public String getLine(int row) {
        int lineCount = count / terminalWidth;
        if(row < 0 || row >= lineCount) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int lineIndex = realIndex(row * terminalWidth);
        for(int i = 0; i < terminalWidth; i++) {
            if(buffer[lineIndex] != null) {
                sb.append(buffer[lineIndex].getInformation());
            } else {
                sb.append(' ');
            }
            lineIndex = increment(lineIndex);
        }
        return sb.toString();
    }

    // There are two approaches to this problem:
    //  First: We could have a variable called bufferString, that would in every point in time contain the value
    //  of the buffer in string format. It would be updated with every change which would make writes slower, but it would
    //  make getting the whole scrollback content in an instant.
    //  Second: Just traverse the whole scrollback content and build the string.
    // I chose the second approach because I think writing is a much more frequent operation than getting the whole scrollback content.
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int bufferIterator = startIndex();
        for(int i = 0; i < count; i++) {
            if(buffer[bufferIterator] != null) {
                stringBuilder.append(buffer[bufferIterator].getInformation());
            } else {
                stringBuilder.append(' ');
            }
            bufferIterator = increment(bufferIterator);
        }
        return stringBuilder.toString();
    }
}
