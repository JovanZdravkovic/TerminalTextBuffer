package io.github.jovanzdravkovic.models;

public class Cell {
    private char information;
    private byte foregroundColor;
    private byte backgroundColor;
    private byte styleFlag;

    public Cell(char information, byte foregroundColor, byte backgroundColor, byte styleFlag) {
        this.information = information;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styleFlag = styleFlag;
    }

    public char getInformation() {
        return this.information;
    }

    public byte getForegroundColor() {
        return this.foregroundColor;
    }

    public byte getBackgroundColor() {
        return this.backgroundColor;
    }

    public byte getStyleFlag() {
        return this.styleFlag;
    }
}
