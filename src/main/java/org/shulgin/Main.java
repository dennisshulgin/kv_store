package org.shulgin;

import org.shulgin.tree.AvlTreeMap;

import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        AvlTreeMap<Integer, String> map = new AvlTreeMap<>();
        map.put(5,"a");
        map.put(1,"a");
        map.put(10,";;;;");
        map.put(6,"a");
        map.put(7,"h");
        map.put(15, "f");
        map.put(23, "f");
        map.put(4, "f");
        map.put(45, "f");
        map.put(0, "f");
        map.put(46, "f");
        map.put(47, "f");
        map.put(48, "f");
        map.put(49, "f");
        map.put(50, "f");
        map.put(51, "f");
        map.put(52, "f");
        //System.out.println(map.keySet());
        map.remove(7);
        map.remove(0);
        map.remove(4);
        //map.remove(7);
        //map.remove(7);
        map.print();
        System.out.println(map.get(10));
    }
}