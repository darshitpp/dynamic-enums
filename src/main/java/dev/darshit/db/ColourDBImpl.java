package dev.darshit.db;

import java.util.List;

public class ColourDBImpl implements ColourDB {
    @Override
    public List<ColourData> getColours() {
        ColourData black = new ColourData("BLACK", 0, 0, 0, 4);
        ColourData white = new ColourData("WHITE", 255, 255, 255, 5);
        return List.of(black, white);
    }
}