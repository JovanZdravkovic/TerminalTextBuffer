package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Style;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {
    private TerminalBuffer terminalBuffer;
    private int terminalHeight = 4;
    private int terminalWidth = 5;
    private int maximumScrollbackLines = 4;
    private byte foregroundColor = (byte)0;
    private byte backgroundColor = (byte)0;
    private EnumSet<Style> styles = EnumSet.of(Style.ITALIC, Style.UNDERLINE);

    @BeforeEach
    void setup() {
        terminalBuffer = new TerminalBuffer(
                terminalHeight,
                terminalWidth,
                maximumScrollbackLines,
                foregroundColor,
                backgroundColor,
                styles
        );
    }

    @Test
    void testWriteIntoEmptyBuffer() {
        assertEquals("", terminalBuffer.toString());
        terminalBuffer.write("TEST");
        assertEquals("TEST", terminalBuffer.toString());
    }

    @Test
    void testWriteOverflowBuffer() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.write("TEST");
        String resultScrollback = "XXXXX";
        String resultScreen = "XXXXXXXXXXXXXXTEST";
        assertEquals(resultScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(resultScreen, terminalBuffer.toString());
    }

    @Test
    void testInsertIntoPartiallyFilledBuffer() {
        assertEquals("", terminalBuffer.toString());

        terminalBuffer.write("TESTTESTTEST");
        terminalBuffer.setCursorPosition(0, 0);
        terminalBuffer.moveCursorRight(5);
        terminalBuffer.insert("XXXX");
        assertEquals("TESTTXXXXESTTEST", terminalBuffer.toString());
    }

    @Test
    void testInsertIntoFullBuffer() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.setCursorPosition(0, 0);
        terminalBuffer.moveCursorRight(7);
        terminalBuffer.insert("TEST");

        String expectedResultScrollback = "XXXXX";
        String expectedResultScreen = "XXTESTXXXXXXXXXXXX";

        assertEquals(expectedResultScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(expectedResultScreen, terminalBuffer.toString());
    }

    @Test
    void testInsertIntoFullBufferFirstLine() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.setCursorPosition(0, 0);
        terminalBuffer.insert("TEST");

        String expectedResultScrollback = "TESTX";
        String expectedResultScreen = "XXXXXXXXXXXXXXXXXX";

        assertEquals(expectedResultScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(expectedResultScreen, terminalBuffer.toString());
    }

    @Test
    void testFillLineFullBuffer() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.moveCursorUp(2);
        terminalBuffer.moveCursorLeft(2);
        terminalBuffer.fillLine('A');

        stringToFillBuffer.replace(5, 10, "AAAAA");
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());
    }

    @Test
    void insertEmptyLineAtBottomFullBuffer() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.insertEmptyLineAtBottom();
        String expectedScrollback = "XXXXX";
        String expectedScreen = stringToFillBuffer.delete(stringToFillBuffer.length() - 5, stringToFillBuffer.length()).toString();
        assertEquals(expectedScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(expectedScreen, terminalBuffer.toString());
    }

    @Test
    void testClearScreen() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.clearScreen();
        assertEquals(0, terminalBuffer.getCursorPosition());
        assertEquals("", terminalBuffer.toString());
    }

    @Test
    void testClearScreenAndScrollback() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.write("TEST");
        String resultScrollback = "XXXXX";
        String resultScreen = "XXXXXXXXXXXXXXTEST";
        assertEquals(resultScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(resultScreen, terminalBuffer.toString());

        terminalBuffer.clearScreenAndScrollback();
        assertEquals("", terminalBuffer.toString());
        assertEquals("", terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(0, terminalBuffer.getCursorPosition());
    }

    @Test
    void testCharAtValidPositionScreen() {
        assertEquals("", terminalBuffer.toString());

        terminalBuffer.write("ABCDE");

        assertEquals('A', terminalBuffer.charAtPositionScreen(0, 0));
        assertEquals('B', terminalBuffer.charAtPositionScreen(0, 1));
        assertEquals('C', terminalBuffer.charAtPositionScreen(0, 2));
        assertEquals('D', terminalBuffer.charAtPositionScreen(0, 3));
        assertEquals('E', terminalBuffer.charAtPositionScreen(0, 4));
    }

    @Test
    void testCharAtEmptyPositionScreen() {
        assertEquals("", terminalBuffer.toString());

        terminalBuffer.write("ABCDE");

        assertEquals('A', terminalBuffer.charAtPositionScreen(0, 0));
        assertEquals(' ', terminalBuffer.charAtPositionScreen(1, 0));
        assertEquals(' ', terminalBuffer.charAtPositionScreen(1, 2));
        assertEquals(' ', terminalBuffer.charAtPositionScreen(3, 1));
    }

    @Test
    void testStylesAtValidPositionScreen() {
        assertEquals("", terminalBuffer.toString());

        terminalBuffer.write("ABCDE");

        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 0));
        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 1));
        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 2));
        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 3));
        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 4));
    }

    @Test
    void testStylesAtEmptyPositionScreen() {
        assertEquals("", terminalBuffer.toString());

        terminalBuffer.write("ABCDE");

        assertEquals(styles, terminalBuffer.stylesAtPositionScreen(0, 0));
        assertNull(terminalBuffer.stylesAtPositionScreen(1, 1));
        assertNull(terminalBuffer.stylesAtPositionScreen(2, 2));
        assertNull(terminalBuffer.stylesAtPositionScreen(3, 3));
    }

    @Test
    void testGetFullLine() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToPartiallyFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 2 * terminalWidth + 2; i++) {
            stringToPartiallyFillBuffer.append('X');
        }

        terminalBuffer.write(stringToPartiallyFillBuffer.toString());
        assertEquals(stringToPartiallyFillBuffer.toString(), terminalBuffer.toString());

        assertEquals("XXXXX", terminalBuffer.getLineScreen(0));
        assertEquals("XXXXX", terminalBuffer.getLineScreen(1));
        assertEquals("XX   ", terminalBuffer.getLineScreen(2));
        assertEquals("     ", terminalBuffer.getLineScreen(3));
    }

    @Test
    void testGetScreenAndScrollbackContent() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.write("TEST");
        String resultScrollback = "XXXXX";
        String resultScreen = "XXXXXXXXXXXXXXTEST";
        assertEquals(resultScrollback, terminalBuffer.getScrollbackBuffer().toString());
        assertEquals(resultScreen, terminalBuffer.toString());

        assertEquals(resultScreen + resultScrollback, terminalBuffer.getScreenAndScrollbackContent());
    }

    @Test
    void testResizeTerminalToLargerTerminal() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());
        int oldCursorPosition = terminalBuffer.getCursorPosition();

        terminalBuffer.resizeTerminal(5, 6);
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());
        assertEquals(oldCursorPosition, terminalBuffer.getCursorPosition());
        assertEquals(5 * 6, terminalBuffer.getTotalScreenSize());
    }

    @Test
    void testResizeTerminalToSmallerTerminal() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());
        int oldCursorPosition = terminalBuffer.getCursorPosition();

        terminalBuffer.resizeTerminal(3, 3);
        assertEquals("XXXXXXXX", terminalBuffer.toString());
        assertEquals("XXXXXXXXXXX", terminalBuffer.getScrollbackBuffer().toString());
        assertNotEquals(oldCursorPosition, terminalBuffer.getCursorPosition());
        assertEquals(8, terminalBuffer.getCursorPosition());
        assertEquals(3 * 3, terminalBuffer.getTotalScreenSize());
    }
}