package dev.darshit;

public enum Colour {

    RED(255, 0, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255);

    int r, g, b;

    Colour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    @Override
    public String toString() {
        return "Colour{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }
}