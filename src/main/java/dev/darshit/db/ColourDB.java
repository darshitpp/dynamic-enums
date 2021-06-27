package dev.darshit.db;

import java.util.List;

public interface ColourDB {

    List<ColourData> getColours();

    class ColourData {
        String colourName;
        int r;
        int g;
        int b;
        int ordinal;

        public ColourData(String colourName, int r, int g, int b, int ordinal) {
            this.colourName = colourName;
            this.r = r;
            this.g = g;
            this.b = b;
            this.ordinal = ordinal;
        }

        public String getColourName() {
            return colourName;
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

        public int getOrdinal() {
            return ordinal;
        }
    }
}