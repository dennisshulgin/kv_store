package org.shulgin.service;

import org.shulgin.tree.AvlTreeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

public class Store <K,V>{
    private SortedMap<K,V> memTable;
    private int memTableSize;

    private String pathToSaveData;

    private BlockingQueue<SortedMap<K,V>> blockingMemTablesQueue;

    private final Object lock = new Object();

    public Store(int memTableSize, String pathToSaveData) {
        this.memTable = new AvlTreeMap<>();
        this.memTableSize = memTableSize;
        this.pathToSaveData = pathToSaveData;
        this.blockingMemTablesQueue = new ArrayBlockingQueue<>(100);
        SaveMemTableThread saveMemTableThread = new SaveMemTableThread(blockingMemTablesQueue, pathToSaveData);
        //saveMemTableThread.setDaemon(true);
        saveMemTableThread.start();
    }

    public void put(K key, V value) {
        synchronized (lock) {
            memTable.put(key, value);
            if(memTable.size() == memTableSize) {
                SortedMap<K,V> fullMemTable = memTable;
                memTable = new AvlTreeMap<>();
                try {
                    blockingMemTablesQueue.put(fullMemTable);
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    private class SaveMemTableThread extends Thread {

        private final BlockingQueue<SortedMap<K,V>> blockingMemTablesQueue;
        private final String pathToSaveFiles;

        private final String defaultFileName = "file";
        private int maxFileNumber;

        public SaveMemTableThread(BlockingQueue<SortedMap<K,V>> blockingMemTableQueue, String pathToSaveFiles) {
            this.blockingMemTablesQueue = blockingMemTableQueue;
            this.pathToSaveFiles = pathToSaveFiles;
            scanDirectory(pathToSaveFiles);
        }

        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    exportMemTableToFile(blockingMemTablesQueue.take());
                }
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
        }

        private void exportMemTableToFile(SortedMap<K,V> table) {
            K minKey = table.firstKey();
            K maxKey = table.lastKey();
            System.out.println("hello");
        }

        private void scanDirectory(String path) {
            List<File> files = getTableFilesFromDirectory(path);
            files.sort((f1, f2) -> {
                String name1 = f1.getName();
                String name2 = f2.getName();
                int index1 = Integer.parseInt(name1.substring(defaultFileName.length()));
                int index2 = Integer.parseInt(name2.substring(defaultFileName.length()));
                return Integer.compare(index1, index2);
            });
            if(!files.isEmpty()) {
                maxFileNumber = Integer.parseInt(files.get(files.size() - 1).getName()
                        .substring(defaultFileName.length()));
            }
            System.out.println(maxFileNumber);
        }

        private List<File> getTableFilesFromDirectory(String path) {
            List<File> files = new ArrayList<>();
            String regex = defaultFileName + "\\d*";
            File directory = new File(path, "");
            if(!directory.exists()) {
                directory.mkdir();
            }
            for(File file : Objects.requireNonNull(directory.listFiles())) {
                if(file.isFile() && Pattern.matches(regex, file.getName())) {
                    files.add(file);
                }
            }
            return files;
        }
    }
}
