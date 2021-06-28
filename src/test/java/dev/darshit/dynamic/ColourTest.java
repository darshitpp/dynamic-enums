package dev.darshit.dynamic;

import dev.darshit.db.ColourDB;
import dev.darshit.db.ColourDBImpl;
import dev.darshit.db.DB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ColourTest {

    public static final String BLACK = "BLACK";
    public static final String WHITE = "WHITE";
    public static final String YELLOW = "YELLOW";
    public static final String RED = "RED";
    private static final String DATA_OBJ = "data.obj";

    @Mock
    ColourDBImpl colourDB;

    @InjectMocks
    DB db;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ColourDB.ColourData black = new ColourDB.ColourData(BLACK, 255, 255, 255, 3);
        ColourDB.ColourData white = new ColourDB.ColourData(WHITE, 0, 0, 0, 4);
        ColourDB.ColourData yellow = new ColourDB.ColourData(YELLOW, 255, 255, 0, 5);
        Mockito.when(colourDB.getColours()).thenReturn(List.of(black, white, yellow));
    }

    @AfterAll
    void tearDown() throws IOException {
        boolean deleteIfExists = Files.deleteIfExists(Path.of(DATA_OBJ));
        System.out.println(deleteIfExists ? "File " + DATA_OBJ + " deleted" : "File not deleted");
    }

    @Test
    void test_values() {
        Colour[] values = Colour.values();
        assertTrue(Arrays.stream(values).anyMatch(colour -> colour.name().equals(Colour.RED.name())));
        assertTrue(Arrays.stream(values).anyMatch(colour -> colour.name().equals(Colour.valueOf(WHITE).name())));
        assertEquals(6, values.length);
    }

    @Test
    void test_if_instances_are_same() {
        assertSame(Colour.RED, Colour.valueOf(RED));
        assertSame(Colour.valueOf(RED), Colour.valueOf(RED));
        assertEquals(Colour.valueOf(RED), Colour.valueOf(RED));

        assertSame(Colour.valueOf(WHITE), Colour.valueOf(WHITE));
        assertEquals(Colour.valueOf(WHITE), Colour.valueOf(WHITE));
    }

    @Test
    void test_ordinal() {
        assertEquals(0, Colour.RED.ordinal()); // static
        assertEquals(5, Colour.valueOf(YELLOW).ordinal()); // dynamic
    }

    @Test
    void test_invalid_colour() {
        String magenta = "MAGENTA";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Colour.valueOf(magenta));
        assertEquals("No Colour by the name " + magenta + " found", exception.getMessage());
    }

    @Test
    void test_compareTo() {
        int red = Colour.RED.compareTo(Colour.valueOf("WHITE"));
        assertTrue(red < 0);

        int yellow = Colour.valueOf("YELLOW").compareTo(Colour.RED);
        assertTrue(yellow > 0);
    }

    @Test
    void test_name() {
        assertEquals("RED", Colour.RED.name());
        assertEquals("YELLOW", Colour.valueOf("YELLOW").name());
    }

    @Test
    void test_clone_not_supported() {
        assertThrows(CloneNotSupportedException.class, () -> Colour.valueOf(BLACK).clone());
    }

    @Test
    void test_serialization_deserialization() throws IOException, ClassNotFoundException {
        serialize(Colour.valueOf(BLACK));
        Colour black = deserialize();

        assertNotNull(black);
        assertEquals(Colour.valueOf(BLACK), black);
        assertSame(Colour.valueOf(BLACK), black);

        serialize(Colour.RED);
        Colour red = deserialize();

        assertNotNull(red);
        assertEquals(Colour.valueOf(RED), red);
        assertSame(Colour.valueOf(RED), red);

        assertEquals(Colour.RED, red);
        assertSame(Colour.RED, red);
    }

    void serialize(Colour colour) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(DATA_OBJ);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(colour);
        }
    }

    Colour deserialize() throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(DATA_OBJ);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Colour) ois.readObject();
        }
    }
}