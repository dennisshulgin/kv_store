package avl_tree_tests;

import org.junit.jupiter.api.*;
import org.shulgin.tree.AvlTreeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvlTreeMapTest {
    int elementCount = 1000000;
    int removeCount = 10000;
    Random random = new Random();
    int min = 1;
    int max = 250;
    static Map<Integer, String> avlTreeMap;
    static Map<Integer, String> hashMap;

    @BeforeAll
    public static void setUp() {
        avlTreeMap = new AvlTreeMap<>();
        hashMap = new HashMap<>();
    }

    @Test
    @Order(1)
    public void putTest() {
        String message = "Old values are not equal";
        String messageSize = "Sizes are not equal";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            String value = UUID.randomUUID().toString();
            Assertions.assertEquals(hashMap.put(key, value), avlTreeMap.put(key, value), message);
            Assertions.assertEquals(hashMap.size(), avlTreeMap.size(), messageSize);
        }
    }

    @Test
    @Order(2)
    public void getTest() {
        String message = "Values are not equal";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            Assertions.assertEquals(hashMap.get(key), avlTreeMap.get(key), message);
        }
    }

    @Test
    @Order(3)
    public void containsKeyTest() {
        String message = "Invalid answer";
        for(int i = 0; i < elementCount; i++) {
            int key = getRandom(min, max);
            Assertions.assertEquals(hashMap.containsKey(key), avlTreeMap.containsKey(key), message);
        }
    }

    @Test
    @Order(4)
    public void keySetTest() {
        String message = "Key sets are not equal";
        Assertions.assertEquals(hashMap.keySet(), avlTreeMap.keySet(), message);
    }

    @Test
    @Order(5)
    public void removeTest() {
        String message = "Invalid answer";
        for(int i = 0; i < removeCount; i++) {
            int key = getRandom(min, max);
            hashMap.remove(key); avlTreeMap.remove(key);
        }
        keySetTest();
    }

    @Test
    @Order(6)
    public void sizeTest() {
        String message = "Invalid answer";
        Assertions.assertEquals(hashMap.size(), avlTreeMap.size(), message);
    }

    public int getRandom(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
