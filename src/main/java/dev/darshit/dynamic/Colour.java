package dev.darshit.dynamic;

import dev.darshit.db.ColourDB;
import dev.darshit.db.DB;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Colour implements Comparable<Colour>, Serializable {

    public static final Colour RED = new Colour("RED", 255, 0, 0, 0);
    public static final Colour GREEN = new Colour("GREEN", 0, 255, 0, 1);
    public static final Colour BLUE = new Colour("BLUE", 0, 0, 255, 2);

    private static final Map<String, Colour> map = new ConcurrentHashMap<>();

    static {
        loadClassData();
        loadDataFromDb();
    }

    private static void loadClassData() {
        Arrays.stream(Colour.class.getDeclaredFields())
                .filter(declaredField -> declaredField.getType() == Colour.class)
                .forEach(Colour::putInMap);
    }

    private static void loadDataFromDb() {
        List<ColourDB.ColourData> colourData = DB.getColourDb().getColours();
        for (ColourDB.ColourData colourDatum : colourData) {
            map.putIfAbsent(colourDatum.getColourName(), new Colour(colourDatum));
        }
    }

    public static Colour[] values() {
        return map.values().stream().sorted().toArray(Colour[]::new).clone();
    }

    public static Colour valueOf(String name) {
        Colour colour = map.get(name);
        if (colour == null) {
            throw new IllegalArgumentException("No Colour by the name " + name + " found");
        }
        return colour;
    }

    private final String colourName;
    private final int r, g, b;
    private final int ordinal;

    private Colour(String colourName, int r, int g, int b, int ordinal) {
        this.colourName = colourName;
        this.r = r;
        this.g = g;
        this.b = b;
        this.ordinal = ordinal;
    }

    private Colour(ColourDB.ColourData colourData) {
        this.colourName = colourData.getColourName();
        this.r = colourData.getR();
        this.g = colourData.getG();
        this.b = colourData.getB();
        this.ordinal = colourData.getOrdinal();
    }

    private static void putInMap(Field declaredField) {
        try {
            map.putIfAbsent(declaredField.getName(), (Colour) declaredField.get(null));
        } catch (IllegalAccessException e) {
            System.err.println("Could not initialize Colour Map value: " + declaredField.getName() + " " + e);
        }
    }

    public String name() {
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

    public int ordinal() {
        return ordinal;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colourName, r, g, b, ordinal);
    }

    @Override
    public final int compareTo(Colour o) {
        Colour self = this;
        return self.ordinal - o.ordinal;
    }

    @Override
    public String toString() {
        return "Colour{" +
                "colourName='" + colourName + '\'' +
                ", r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", ordinal=" + ordinal +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private Object readResolve() {
        return Colour.valueOf(colourName);
    }
}