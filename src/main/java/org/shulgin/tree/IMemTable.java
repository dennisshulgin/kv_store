package org.shulgin.tree;

import java.io.PrintStream;
import java.io.PrintWriter;

public interface IMemTable<K,V> {
    int size();
    int hiddenSize();
    boolean isEmpty();
    boolean containsKey(Object o);
    V get(Object o);
    V put(K k, V v);
    V remove(Object o);
    V markAsDeleted(Object o);
    K firstKey();
    K lastKey();
    void printTree(PrintWriter pw);
    void clear();
}
