package org.shulgin.tree;

import java.util.Comparator;

public class MemTree <K,V>{
    private Node<K, V> root;
    private Comparator<? super K> comparator;

    public MemTree() {
        this.comparator = null;
    }

    public MemTree(Comparator<K> comparator) {
        this.comparator = comparator;
    }

    private int height(Node<K,V> node) {
        return node == null ? -1 : node.height;
    }

    private int getBalance(Node<K,V> node) {
        return node == null ? 0 : height(node.right) - height(node.left);
    }

    private void updateHeight(Node<K,V> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private class Node<K,V> {
        int height;
        K key;
        V value;
        Node<K,V> left;
        Node<K,V> right;
        Node<K,V> parent;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
