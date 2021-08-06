#  Dynamic Enums 
> This post was first published at my blog: https://darshit.dev/posts/dynamic-enums/

### But can one even make Enums dynamic?

Enums, by definition, are static. They are used to "predefine" constants. I'll let the official [Oracle Java Tutorial on Enums](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html) to help explain the definition.
> An enum type is a special data type that enables for a variable to be a set of predefined constants. The variable must be equal to one of the values that have been predefined for it. Common examples include compass directions (values of NORTH, SOUTH, EAST, and WEST) and the days of the week.

```java
public enum Day {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
    THURSDAY, FRIDAY, SATURDAY 
}
```

The values of an enum are known to the compiler at compile time, are not supposed to change. Imagine if someone tried to introduce a new day of the week. This is pretty unlikely, and thus, for all intents and purposes, the days of the week remain the same.

However, you are now working on an application which has a legacy codebase. This legacy code has an enum named `Colour` ([the British spelling, because I'm not in the US](https://www.grammarly.com/blog/color-colour/)). The actual enum might be something different and complicated, but the example of `Colour` works for the purposes of this article.

`Colour` has three colours defined inside it -- `RED`, `GREEN`, and `BLUE`.

```java
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
    // getters and toString()
}
```

The `Colour` enum is being used across lots of applications as a dependency. First, you wonder why someone needed to put the Colours as an Enum datatype. But unable to do anything about it, you just accept it and work with whatever is in your fate.

The problem with the above enum is, every time some app needs a new colour, you need to add the new colour to your codebase. After adding a new colour, you now need to test, and then re-deploy all your applications. This has to be done every time a new colour is requested. You see the problem -- the colours are supposed to be dynamic, but someone took a decision an eternity ago, and now you are the one in soup.

You now want to change enums to make them dynamic, so that any new colour added, is picked up from a database, with minimal code changes across your stack. How do you proceed?

-----

### Enums are also classes

Yes! Enums are also classes. We know that enums are static, and classes are not. Technically, you could replace Enum with a class. If you replace an Enum with a Class, you would not even need to change imports in other classes. No part of your code base would even realize it! This will be the basis of our solution -- turn Enum into a Class.

But wait! It's not so straightforward. Enums can also be referred directly, like `Colour.RED`. We do not want to meddle with existing usages of `Colour.RED` in our code.

-----

### Step 1: Change `enum` to `class`

When you change the `enum` to `class`, your IDE should instantly throw up an error. This is because Enums are compile time constructs which are being referred in other parts of you application. But not to worry!

-----

### Step 2: Create constants of existing Colours

To keep the usages of `Colour.RED` same, we would need to create constants for all the colours defined in the enum (now class).

```java
public final class Colour {

    public static final Colour RED = new Colour(255, 0, 0);
    public static final Colour GREEN = new Colour(0, 255, 0);
    public static final Colour BLUE = new Colour(0, 0, 255);

	private final int r, g, b;

    private Colour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    // getters and toString()
}
```

We also do not want the objects of `Colour` to be created outside the class. If we do not do this, we are essentially allowing anyone to create `Colour` objects, and defeats the purpose of mimicing the enum. Thus, we would mark the constructor as `private`.

Congrats! The IDE should stop raising an error now (inside the `Colour` class atleast). The above refactoring makes sure that you can still refer to `Colour.RED` as earlier. Also, now that we made the variables of `r`, `g`, and `b` as `private final` as we do not want them to change after object creation.

-----

### Step 3: Implement other Enum methods like `values()` and `valueOf()`

We know that methods like `values()` and `valueOf()` are used quite often with enums. To ensure that these usages do not break, we would need to "mimic" these methods. How can we do that?

Note the return types of `values()` and `valueOf()`.

* `valueOf(String)` returns in instance of `Colour` defined by the name provided as a parameter.
* `values()` returns an array of `Colour`, i.e. `Colour[]`

Let us start with the `valueOf()` method, and it will lead us to the solution of `values()`

As we know, the `valueOf()` accepts a `String` as an argument, and returns an instance of `Colour`. What can we use to preserve the mapping between a `String` and `Colour`? A Map!! ~~We can use a `HashMap` or `ConcurrentHashMap` for this.~~

*Update 17-07-2021: DO NOT USE  `HashMap` or `ConcurrentHashMap`, instead use `LinkedHashMap`, for reasons that will be explained later in the article.*

Note that we currently have no way of knowing what colour an instance is. Of course, we have a constant declared as
```java
public static final Colour RED = new Colour(255, 0, 0);
```
but even if we load it into a map, how would we know if the object is for `Colour.RED` or `Colour.GREEN`? Is there a way we can get the "text"/"name" of the variable as a `String`? Not directly, no. 

Enter: [Java Reflection](https://www.oracle.com/technical-resources/articles/java/javareflection.html)
> It allows an executing Java program to examine or "introspect" upon itself, and manipulate internal properties of the program. **For example, it's possible for a Java class to obtain the names of all its members and display them.**

We can use this! Let us first define a Map of type `Map<String, Colour>` as a `HashMap` or `ConcurrentHashMap`. We will then use Java Reflection to load up the values inside the Map through a `static` block.

```java
public final class Colour {

    public static final Colour RED = new Colour(255, 0, 0);
    public static final Colour GREEN = new Colour(0, 255, 0);
    public static final Colour BLUE = new Colour(0, 0, 255);

    private static final Map<String, Colour> map = new LinkedHashMap<>();

    static {
    	loadClassData();
    }

    private static void loadClassData() {
        Arrays.stream(Colour.class.getDeclaredFields())
                .filter(declaredField -> declaredField.getType() == Colour.class)
                .forEach(Colour::putInMap);
    }


    private static void putInMap(Field declaredField) {
        try {
            map.putIfAbsent(declaredField.getName(), (Colour) declaredField.get(null));
        } catch (IllegalAccessException e) {
            System.err.println("Could not initialize Colour Map value: " + declaredField.getName() + " " + e);
        }
    }


	private final int r, g, b;

    private Colour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }


    // getters and toString()
}
```

The above codeblock uses Java Streams to load up the map. Let us go through what it does.

1. The `Colour.class.getDeclaredFields()` fetches all the fields declared inside the `Colour` class.
2. For each of the field returned, we only want the fields of type `Colour`. (The previous statement would also return `Map<String, Colour>`)
3. For each of the fields of `Colour`, we would call the `putInMap()` method.
4. The `putInMap()` takes a parameter of type `Field`, and loads the data in the map. The variable name is obtained by `declaredField.getName()`, and the actual object is returned by `declaredField.get(null)`


If you do not understand the above Stream based code, or you're just working with Java 7 and lower, you can use the following:

```java
private static void loadClassData() {
    for (Field declaredField : Colour.class.getDeclaredFields()) {
        if (declaredField.getType() == Colour.class) {
            putInMap(declaredField);
        }
    }
}
```

We have the data in the map! We can now just implement the `valueOf()` method as follows:
```java
public static Colour valueOf(String name) {
    Colour colour = map.get(name);
    if (colour == null) {
        throw new IllegalArgumentException("No Colour by the name " + name + " found");
    }
    return colour;
}
```

Note that in Enums, the `valueOf()` returns an `IllegalArgumentException` if no value is found within the enum. Similarly, we will ensure that our implementation also returns the same exception.

The usage of Map enables us to easily implement the `values()` method. We can implement it by using the `map.values()` method.

```java
public static Colour[] values() {
    return map.values().toArray(Colour[]::new).clone();
}
```

~~*Update 14-07-2021: The map does not have ordered values, so we need to sort it! The tests have been updated in the later sections.*~~

*Update 17-07-2021: Since the `values()` method produces an array in the order in which the Enum values are defined, we need to preserve the order in the Map as well. This is why we need to use a `LinkedHashMap`, rather than another Map implementation.*

Every time the `values()` is called, it returns the clone of the array values in the map.

-----

### Step 4: Load data from the Database

However, the purpose of having a dynamic Enum is still not achieved. We want the values of the colour to be loaded up from a database. The problem now seems trivial. Similar to the way we loaded up the class data inside the map, we must also fetch the data from the database and load it. This needs to take place in the static block as well.

However, in this case we cannot use Java Reflection to get the variable name, simply because there isn't any static variable we can refer to.

Thus, we must add another field into the class named `colourName`.

```java
public final class Colour {

    public static final Colour RED = new Colour("RED", 255, 0, 0);
    public static final Colour GREEN = new Colour("GREEN", 0, 255, 0);
    public static final Colour BLUE = new Colour("BLUE", 0, 0, 255);

    private static final Map<String, Colour> map = new LinkedHashMap<>();

    // other implemented methods


    // new field
    private final String colourName;
	private final int r, g, b;

    private Colour(String colourName, int r, int g, int b) {
    	this.colourName = colourName;
        this.r = r;
        this.g = g;
        this.b = b;
    }


    // getters and toString()
}
```

Thus, when we load up the data from the database, we would know what key to use for the Map.

Our static block would now have 
```java
static {
    loadClassData();
    loadDataFromDb();
}

private static void loadDataFromDb() {
    List<ColourDB.ColourData> colourData = new ColourDB().getColours();
    for (ColourDB.ColourData colourDatum : colourData) {
        map.putIfAbsent(colourDatum.getColourName(), new Colour(colourDatum));
    }
}
```


Our `ColourDB` contains the static class `ColourData`, which is exactly the same as the `Colour` POJO. As we cannot create `Colour` objects, we need another type to put the data in, and get the data from.

The `ColourDB` is like the following:
```java
public class ColourDB {

    public List<ColourData> getColours() {
    	// data from DB
    }

    static class ColourData {
        String colourName;
        int r;
        int g;
        int b;

        public ColourData(String colourName, int r, int g, int b) {
            this.colourName = colourName;
            this.r = r;
            this.g = g;
            this.b = b;
        }
        // getters
    }
}
```

We can now add another private constructor within `Colour` that accepts `ColourData`.
```java
private Colour(ColourDB.ColourData colourDatum) {
    this.colourName = colourDatum.getColourName();
    this.r = colourDatum.getR();
    this.g = colourDatum.getG();
    this.b = colourDatum.getB();
}
```

Notice that using this method allows you to make Colours dynamic, but prevents you from creating static objects like `Colour.RED`. If we add the data for the colour "BLACK" in the DB, we cannot refer to it as `Colour.BLACK` after this change. We would need to refer to it as `Colour.valueOf("BLACK")`, and get the value. This is a trade off required to make it dynamic. However, this allows us to ensure that the existing code is not impacted.

If you have a implemented a getter method for `colourName`, refactor it from `getColourName()` to `name()`.

Similarly, if you use `ordinal()` method of the enum, ensure that you introduce the ordinal field in the `Colour` class as well. You would need to store the ordinal field in the DB too.

You can also implement the `equals()` method and change it to compare using `==` as is done in Enums. One would also need to implement the `Comparable` interface with the `compareTo()` method.

We will also implement the Serializable interface to enable us to serialize objects. This will ensure we conform to the `Enum` functionality. Also, Enums cannot be cloned. Hence, we will also implement the clone method and throw `CloneNotSupportedException`.

The updated code would look like the following:

```java
public final class Colour implements Comparable<Colour>, Serializable {

    public static final Colour RED = new Colour("RED", 255, 0, 0, 0);
    public static final Colour GREEN = new Colour("GREEN", 0, 255, 0, 1);
    public static final Colour BLUE = new Colour("BLUE", 0, 0, 255, 2);

    private static final Map<String, Colour> map = new LinkedHashMap<>();

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
        List<ColourDB.ColourData> colourData = new ColourDB().getColours();
        for (ColourDB.ColourData colourDatum : colourData) {
            map.putIfAbsent(colourDatum.getColourName(), new Colour(colourDatum));
        }
    }

    public static Colour[] values() {
        return map.values().toArray(Colour[]::new).clone();
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

    public int ordinal() {
        return ordinal;
    }

    // getters
    // ..
    // ..

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
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
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
}
```
and `ColourDB` as
```java
public class ColourDB {

    public List<ColourData> getColours() {
    	// data from DB
    }

    static class ColourData {
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
        // getters
    }
}
```

-----

### Step 5: Test

Ordinarily, this would work. Unless, of course, you actually want to write unit tests for the class.

However, how can we test it? Notice that testing it is almost impossible, unless you have an actual DB. This is because of the following piece of code.
```java
private static void loadDataFromDb() {
    List<ColourDB.ColourData> colourData = new ColourDB().getColours();
    for (ColourDB.ColourData colourDatum : colourData) {
        map.putIfAbsent(colourDatum.getColourName(), new Colour(colourDatum));
    }
}
```

Here, the dependency for the Database `new ColourDB()` is hard-coded inside the class. Testing would require an actual Database connection. We would want to Mock it during testing. If we were using a DI framework like Spring, we could have injected it. However, using pure Java code would require some more refactoring.

First, we need to extract `ColourDB` into an interface and include the actual implementation as `ColourDbImpl`.
```java
public interface ColourDB {

    List<ColourData> getColours();

    class ColourData {
        // existing
    }
}

public class ColourDBImpl implements ColourDB {

    @Override
    public List<ColourData> getColours() {
        // get from DB
    }
}
```

We will now create a class named `DB`:
```java
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
```

We can now replace `ColourDB` in `loadDataFromDb` with `DB.getColourDb()`. The code now looks like
```java
private static void loadDataFromDb() {
    List<ColourDB.ColourData> colourData = DB.getColourDb().getColours();
    for (ColourDB.ColourData colourDatum : colourData) {
        map.putIfAbsent(colourDatum.getColourName(), new Colour(colourDatum));
    }
}
```

Now that we have refactored it, we can Mock it successfully.

You can see the test class `ColourTest` as follows
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ColourTest {

    public static final String BLACK = "BLACK";
    public static final String WHITE = "WHITE";
    public static final String YELLOW = "YELLOW";
    public static final String RED = "RED";

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

    @Test
    void test_values() {
        Colour[] values = Colour.values();

        assertEquals(Colour.RED.name(), values[0].name());
        assertEquals(Colour.valueOf(YELLOW).name(), values[values.length - 1].name());
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
}
```

You will need to use `@BeforeAll` with `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` as the static block is only executed once.
Else Mockito will throw an `UnnecessaryStubbingException` because the Mock would not execute every test.

-----

### Step 6: Serialization!

We did not consider what would happen if we serialize the Colour class. [Enum constants are serialized differently than ordinary serializable or externalizable objects.](https://docs.oracle.com/javase/7/docs/platform/serialization/spec/serial-arch.html) Only the `name` of the field is serialized, and deserialization uses `valueOf()` method to get the Enum constant back.

Enums are effectively singletons. However, the [singleton property of our class can be broken during deserialization.](https://dzone.com/articles/prevent-breaking-a-singleton-class-pattern) Thus, we need to ensure that we preserve the singleton property of `Colour` as well. We can do that by implementing the `readResolve`[^1] method. This will ensure that we only receive an instance of the same class as the one that we have already created. We already store the colour name. So when a new object is created, it will still return the already existing objects that we expect.

```java
public final class Colour implements Comparable<Colour>, Serializable {
    // Existing code
    //
    //

    private Object readResolve() {
        return Colour.valueOf(colourName);
    }
}
```

We can add a test and verify if this will work.
```java
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
    try (FileOutputStream fos = new FileOutputStream("data.obj");
         ObjectOutputStream oos = new ObjectOutputStream(fos)) {
        oos.writeObject(colour);
    }
}

Colour deserialize() throws IOException, ClassNotFoundException {
    try (FileInputStream fis = new FileInputStream("data.obj");
         ObjectInputStream ois = new ObjectInputStream(fis)) {
        return (Colour) ois.readObject();
    }
}
```

It does! :)

-----

### Limitations

As you know, this has a limitation that you cannot statically infer Enum constants, except the ones defined inside `Colour`. The values in DB must be referred through `Colour.valueOf()`. Moreover, you would need to change any switch-case statements to use the value of the the Constant, instead of static enum types supported by switch-case. An example:

```java
switch (Colour.RED.name()) {
    case "RED" :
        System.out.println("RED");  
        break;
    default:
}
```

I discovered that this is also known as a type-safe enum, which was used before Enum as types were introduced in Java 5. Granted, this a step lower than Enums, but... you know. 

-----

The above is a hack though. In an ideal world, you would never have to try and implement these hacks. However, if you find yourself in such a spot, you know what to do. Please, try to refactor and remove this code though. 

You can find the code for the project on my Github repository: [dynamic-enums](https://github.com/darshitpp/dynamic-enums).


Done! We have now created a "Dynamic Enum"! :sunglasses:

You can now rest in peace, and wish/hope that the next developer touching this piece of code does not try to contact you.

References and Reading material:

1. [Java Magazine tutorial on Enums](https://blogs.oracle.com/javamagazine/java-enumerated-enums-class)
2. [How to use typesafe enums in Java](https://www.infoworld.com/article/3543350/how-to-use-typesafe-enums-in-java.html)
3. [Beware of Java typesafe enumerations](https://www.infoworld.com/article/2077487/java-tip-122--beware-of-java-typesafe-enumerations.html)
4. [More on typesafe enums](https://www.infoworld.com/article/2077499/java-tip-133--more-on-typesafe-enums.html)
5. [Enum Tricks: Dynamic Enums](https://dzone.com/articles/enum-tricks-dynamic-enums)
6. [Introduction to Java Serialization](https://www.baeldung.com/java-serialization
)

[^1]: [Java Serialization Docs](https://docs.oracle.com/javase/8/docs/platform/serialization/spec/input.html#5903)
