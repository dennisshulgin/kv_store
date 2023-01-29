package org.shulgin.tree;

import java.io.*;
import java.util.*;

public class MemTable<K,V> implements IMemTable<K,V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Node<K,V> root;
    private Comparator<? super K> comparator;

    private int size = 0;

    private int hiddenSize = 0;

    public MemTable() {
    }

    public MemTable(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int hiddenSize() {
        return hiddenSize;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        if (o == null || root == null || root.getKey().getClass() != o.getClass()) {
            return false;
        }
        K key = (K) o;
        Node<K, V> node = findNodeByKey(key);
        return node != null;
    }

    @Override
    public V get(Object o) {
        if (o == null || root == null || root.getKey().getClass() != o.getClass()) {
            return null;
        }
        K key = (K) o;
        Node<K, V> node = findNodeByKey(key);
        return node != null && !node.isDeleted ? node.getValue() : null;
    }

    @Override
    public V put(K k, V v) {
        if(k == null || v == null) {
            return null;
        }
        Node<K,V> node = new Node<>(k, v);
        if (root == null) {
            root = node;
            size++;
            hiddenSize++;
            return null;
        }

        boolean isInserted = false;
        Node<K, V> head = root;

        do {
            int cmp = compare(node.getKey(), head.getKey());
            if (cmp > 0) {
                if (head.right == null) {
                    head.right = node;
                    node.parent = head;
                    isInserted = true;
                    size++;
                    hiddenSize++;
                } else {
                    head = head.right;
                }
            } else if (cmp < 0) {
                if (head.left == null) {
                    head.left = node;
                    node.parent = head;
                    isInserted = true;
                    size++;
                    hiddenSize++;
                } else {
                    head = head.left;
                }
            } else {
                if(head.isDeleted) {
                    size++;
                }
                head.isDeleted = false;
                return head.setValue(node.getValue());
            }
        } while (!isInserted);

        upBalance(node);
        return null;
    }

    @Override
    public V remove(Object o) {
        if (isIncorrectKey(o)) {
            return null;
        }
        K key = (K) o;
        Node<K, V> node = findNodeByKey(key);
        V oldValue = null;
        if (node != null) {
            size = size > 0 ? size - 1 : 0;
            hiddenSize = hiddenSize > 0 ? hiddenSize - 1 : 0;
            oldValue = node.getValue();
            removeNode(node);
        }
        return oldValue;
    }

    @Override
    public V markAsDeleted(Object o) {
        if (isIncorrectKey(o)) {
            return null;
        }
        K key = (K) o;
        Node<K, V> node = findNodeByKey(key);
        V oldValue = null;
        if (node != null) {
            size = size > 0 ? size - 1 : 0;
            oldValue = node.getValue();
            node.isDeleted = true;
        }
        return oldValue;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        hiddenSize = 0;
    }

    @Override
    public K firstKey() {
        Node<K, V> firstNode = mostLeftNode(root);
        return firstNode == null ? null : firstNode.getKey();
    }

    @Override
    public K lastKey() {
        Node<K, V> lastNode = mostRightNode(root);
        return lastNode == null ? null : lastNode.getKey();
    }

    @Override
    public void printTree(PrintWriter pw) {
        int level = 0;

        Queue<Node<K,V>> queue = new ArrayDeque<>();

        if(root != null) {
            queue.add(root);
        }

        while(!queue.isEmpty()) {
            int size = queue.size();

            while(size-- > 0) {
                Node<K,V> node = queue.poll();
                pw.println("level:" + level + " key:" + node.key + " value:" + node.value);
                if(node.left != null) {
                    queue.add(node.left);
                }
                if(node.right != null) {
                    queue.add(node.right);
                }
            }

            level++;
        }
    }

    private boolean isIncorrectKey(Object o) {
        return (o == null || root == null || root.getKey().getClass() != o.getClass());
    }

    private void removeNode(Node<K,V> node) {
        if(node == null) {
            return;
        }
        Node<K,V> parent = node.parent;
        if(node.left == null && node.right == null) {
            node.parent = null;
            if(parent == null) {
                root = null;
            } else {
                if(parent.left == node) {
                    parent.left = null;
                } else {
                    parent.right = null;
                }
                upBalance(parent);
            }
        } else if(node.left == null || node.right == null) {
            if(node.left == null) {
                if(parent == null) {
                    root = node.right;
                    upBalance(root);
                } else {
                    if(parent.left == node) {
                        parent.left = node.right;
                        upBalance(parent.left);
                    } else {
                        parent.right = node.right;
                        upBalance(parent.right);
                    }
                }
                node.right.parent = parent;
            } else {
                if(parent == null) {
                    root = node.left;
                    upBalance(root);
                } else {
                    if(parent.left == node) {
                        parent.left = node.left;
                        upBalance(parent.left);
                    } else {
                        parent.right = node.left;
                        upBalance(parent.right);
                    }
                }
                node.left.parent = parent;
            }
        } else {
            Node<K,V> mostLeftNode = mostLeftNode(node.right);
            node.key = mostLeftNode.getKey();
            node.value = mostLeftNode.getValue();
            removeNode(mostLeftNode);
        }
    }

    private Node<K,V> findNodeByKey(K key) {
        if(root == null) {
            return null;
        }
        Node<K,V> node = root;
        do {
            int cmp = compare(key, node.getKey());
            if(cmp > 0) {
                node = node.right;
            } else if(cmp < 0) {
                node = node.left;
            } else {
                return node;
            }
        } while(node != null);

        return null;
    }

    private void upBalance(Node<K,V> node) {
        while(node.parent != null) {
            node = reBalance(node);
            node = node.parent;
        }
        root = reBalance(node);
    }

    private Node<K,V> mostLeftNode(Node<K,V> node) {
        while(node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<K,V> mostRightNode(Node<K,V> node) {
        while(node.right != null) {
            node = node.right;
        }
        return node;
    }

    private int height(Node<K,V> node) {
        return node == null ? -1 : node.height;
    }

    private int getBalance(Node<K,V> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private void updateHeight(Node<K,V> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private int compare(K key1, K key2) {
        return comparator == null ? ((Comparable)key1).compareTo((Comparable)key2)
                : comparator.compare(key1, key2);
    }

    private Node<K,V> reBalance(Node<K,V> node) {
        updateHeight(node);
        int balance = getBalance(node);

        if(balance > 1) {
            int leftBalance = getBalance(node.left);
            if(leftBalance > -1) {
                node = rightRotate(node);
            } else {
                node = bigRightRotate(node);
            }
        } else if(balance < -1) {
            int rightBalance = getBalance(node.right);
            if(rightBalance < 1) {
                node = leftRotate(node);
            } else {
                node = bigLeftRotate(node);
            }
        }
        return node;
    }

    private Node<K,V> leftRotate(Node<K,V> a) {
        Node<K,V> parent = a.parent;
        Node<K,V> b = a.right;
        a.right = b.left;
        b.left = a;
        b.parent = parent;
        a.parent = b;
        if(a.right != null) {
            a.right.parent = a;
        }
        if(parent != null) {
            if(parent.left == a) {
                parent.left = b;
            } else {
                parent.right = b;
            }
        }
        updateHeight(a);
        updateHeight(b);
        return b;
    }

    private Node<K,V> rightRotate(Node<K,V> a) {
        Node<K,V> parent = a.parent;
        Node<K,V> b = a.left;
        a.left = b.right;
        b.right = a;
        b.parent = parent;
        a.parent = b;
        if(a.left != null) {
            a.left.parent = a;
        }
        if(parent != null) {
            if(parent.left == a) {
                parent.left = b;
            } else {
                parent.right = b;
            }
        }
        updateHeight(a);
        updateHeight(b);
        return b;
    }

    private Node<K,V> bigLeftRotate(Node<K,V> a) {
        a.right = rightRotate(a.right);
        return leftRotate(a);
    }

    private Node<K,V> bigRightRotate(Node<K,V> a) {
        a.left = leftRotate(a.left);
        return rightRotate(a);
    }

    private static class Node<K,V>{
        K key;
        V value;
        int height;
        boolean isDeleted;
        Node<K,V> left;
        Node<K,V> right;
        Node<K,V> parent;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }
        public V getValue() {
            return value;
        }
        public V setValue(V v) {
            V oldValue = value;
            value = v;
            return oldValue;
        }
    }
}
