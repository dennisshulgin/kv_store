package avl_tree_tests;

import org.junit.jupiter.api.*;
import org.shulgin.tree.AvlTreeMap;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvlTreeMapTest {
    int elementCount = 10000;
    int removeCount = 100;
    Random random = new Random();
    int min = 3;
    int max = 500;
    static SortedMap<Integer, String> avlTreeMap;
    static SortedMap<Integer, String> treeMap;

    @BeforeAll
    public static void setUp() {
        avlTreeMap = new AvlTreeMap<>();
        treeMap = new TreeMap<>();
    }

    @Test
    @Order(1)
    public void putTest() {
        String message = "Old values are not equal";
        String messageSize = "Sizes are not equal";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            String value = UUID.randomUUID().toString();
            Assertions.assertEquals(treeMap.put(key, value), avlTreeMap.put(key, value), message);
            Assertions.assertEquals(treeMap.size(), avlTreeMap.size(), messageSize);
        }
    }

    @Test
    @Order(2)
    public void firstKeyTest() {
        Assertions.assertEquals(treeMap.firstKey(), avlTreeMap.firstKey());
    }

    @Test
    @Order(3)
    public void lastKeyTest() {
        Assertions.assertEquals(treeMap.lastKey(), avlTreeMap.lastKey());
    }

    @Test
    @Order(4)
    public void getTest() {
        String message = "Values are not equal";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            Assertions.assertEquals(treeMap.get(key), avlTreeMap.get(key), message);
        }
    }

    @Test
    @Order(5)
    public void containsKeyTest() {
        String message = "Invalid answer";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            Assertions.assertEquals(treeMap.containsKey(key), avlTreeMap.containsKey(key), message);
        }
    }

    @Test
    @Order(6)
    public void removeTest() {
        String message = "Invalid answer";
        for(int i = 0; i < removeCount; i++) {
            int key = getRandom(min, max);
            treeMap.remove(key); avlTreeMap.remove(key);
        }
    }

    @Test
    @Order(7)
    public void sizeTest() {
        String message = "Invalid answer";
        Assertions.assertEquals(treeMap.size(), avlTreeMap.size(), message);
    }

    public int getRandom(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
