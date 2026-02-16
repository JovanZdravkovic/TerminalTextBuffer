package io.github.jovanzdravkovic.models;

import java.util.EnumSet;

public class Cell {
    private char information;
    private byte foregroundColor;
    private byte backgroundColor;
    private EnumSet<Style> styles;

    public Cell(char information, byte foregroundColor, byte backgroundColor, EnumSet<Style> styles) {
        this.information = information;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = styles;
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

    public EnumSet<Style> getStyles() {
        return this.styles;
    }
}
