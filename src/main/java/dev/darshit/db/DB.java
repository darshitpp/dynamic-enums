package dev.darshit.db;

public class DB {
    private static ColourDB COLOUR_DB;

    public DB(ColourDB colourDB) {
        COLOUR_DB = colourDB;
    }

    public static ColourDB getColourDb() {
        if (COLOUR_DB == null) {
            COLOUR_DB = new ColourDBImpl();
        }
        return COLOUR_DB;
    }

    public static void setColourDb(ColourDB colourDb) {
        COLOUR_DB = colourDb;
    }
}