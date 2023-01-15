package org.shulgin;

import org.shulgin.service.Store;
import org.shulgin.tree.AvlTreeMap;


public class Main {

    public static void main(String[] args) {
        Store<Integer, String> store = new Store<>(3, "./myfiles");
        store.put(1, "11");
        store.put(2, "22");
        store.put(3, "33");
        store.put(4, "44");

    }
}