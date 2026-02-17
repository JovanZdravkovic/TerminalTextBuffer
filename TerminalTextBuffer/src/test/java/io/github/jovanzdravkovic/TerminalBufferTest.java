package io.github.jovanzdravkovic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {
    private TerminalBuffer terminalBuffer;

    @BeforeEach
    void setup() {
        terminalBuffer = new TerminalBuffer(
                4,
                5,
                4,
                (byte)0,
                (byte)0,
                null
        );
    }

    @Test
    void testWriteIntoEmptyBuffer() {
        assertEquals("", terminalBuffer.toString());
        terminalBuffer.write("TEST");
        assertEquals("TEST", terminalBuffer.toString());
    }

    @Test
    void testOverflowBuffer() {
        assertEquals("", terminalBuffer.toString());

        StringBuilder stringToFillBuffer = new StringBuilder();
        for(int i = 0; i < terminalBuffer.getTotalScreenSize() - 1; i++) {
            stringToFillBuffer.append('X');
        }

        terminalBuffer.write(stringToFillBuffer.toString());
        assertEquals(stringToFillBuffer.toString(), terminalBuffer.toString());

        terminalBuffer.moveCursorDown(4);
        terminalBuffer.moveCursorRight(5);
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
}