package org.shulgin;


import org.shulgin.tree.MemTable;

public class Main {

    public static void main(String[] args) {
        MemTable<Integer,String> mem = new MemTable<>();
        mem.put(1, "1");
        mem.put(2, "2");
        mem.put(3, "3");
        mem.put(4, "4");
        mem.put(5, "5");
        mem.put(6, "6");
        mem.put(7, "7");
        System.out.println("size = " + mem.size());
        mem.remove(6);

        //mem.printTree(System.out);



        System.out.println("size = " + mem.size());
        //mem.printTree(System.out);
    }
}