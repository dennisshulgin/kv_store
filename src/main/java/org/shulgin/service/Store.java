package org.shulgin.service;

import org.shulgin.exception.CreateDirectoryException;
import org.shulgin.exception.CreateFileException;
import org.shulgin.tree.IMemTable;
import org.shulgin.tree.MemTable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class Store <K,V>{
    private IMemTable<K,V> memTable;
    private final int memTableSize;
    private final String pathToSaveData;
    private final String defaultFileName;
    private final BlockingQueue<IMemTable<K,V>> blockingMemTablesQueue;
    private final Object lock = new Object();

    private final List<File> files;

    public Store(int memTableSize, String pathToSaveData, String defaultFileName) throws CreateDirectoryException{
        this.memTable = new MemTable<>();
        this.memTableSize = memTableSize;
        this.pathToSaveData = pathToSaveData;
        this.defaultFileName = defaultFileName;
        this.blockingMemTablesQueue = new ArrayBlockingQueue<>(100);
        this.files = getKeyValueStoreFiles(new File(pathToSaveData));

        WriteMemTableThread writeMemTableThread;
        try {
            writeMemTableThread = new WriteMemTableThread(blockingMemTablesQueue, files, pathToSaveData, defaultFileName);
            writeMemTableThread.start();
        } catch (Exception ignored) { }
    }

    public void put(K key, V value) {
        synchronized (lock) {
            memTable.put(key, value);
            if(memTable.size() == memTableSize) {
                IMemTable<K,V> fullMemTable = memTable;
                memTable = new MemTable<>();
                try {
                    blockingMemTablesQueue.put(fullMemTable);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    public V get(K key) {
        synchronized (lock) {
            V value = memTable.get(key);
            if(value != null) {
                return value;
            }
            for(int i = files.size() - 1; i >= 0; i--) {
                try(FileInputStream fis = new FileInputStream(files.get(i));
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                    K firstKey = (K)ois.readObject();
                    K lastKey = (K)ois.readObject();
                    Comparator<? super K> comparator = memTable.comparator();
                    if(comparator.compare(key, firstKey) >= 0
                            && comparator.compare(key, lastKey) <= -1) {
                        IMemTable<K,V> currMemTable = (IMemTable<K, V>) ois.readObject();
                        V val = currMemTable.get(key);

                        if(val != null) {
                            return val;
                        }
                    }
                } catch (IOException | ClassNotFoundException ignored) { }
            }

            return null;
        }
    }

    private List<File> getKeyValueStoreFiles(File directory) throws CreateDirectoryException{
        List<File> files = new CopyOnWriteArrayList<>();

        if(!directory.exists()) {
            if(!directory.mkdir()) {
                throw new CreateDirectoryException();
            }
        }

        String regex = defaultFileName + "\\d*";

        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(file.isFile() && Pattern.matches(regex, file.getName())) {
                files.add(file);
            }
        }
        files.sort((first, second) -> {
            String filenameFirst = first.getName();
            String filenameSecond = second.getName();
            int indexFirst = Integer.parseInt(filenameFirst.substring(defaultFileName.length()));
            int indexSecond = Integer.parseInt(filenameSecond.substring(defaultFileName.length()));
            return Integer.compare(indexFirst, indexSecond);
        });

        return files;
    }

    private class WriteMemTableThread extends Thread {
        private final BlockingQueue<IMemTable<K,V>> blockingMemTablesQueue;
        private final String pathToSaveFiles;
        private final String defaultFileName;
        private final List<File> files;

        public WriteMemTableThread(BlockingQueue<IMemTable<K,V>> blockingMemTableQueue,
                                   List<File> files, String pathToSaveFiles,
                                   String defaultFileName) throws CreateDirectoryException{
            this.blockingMemTablesQueue = blockingMemTableQueue;
            this.pathToSaveFiles = pathToSaveFiles;
            this.defaultFileName = defaultFileName;
            this.files = files;
        }

        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    exportMemTableToFile(blockingMemTablesQueue.take());
                }
            } catch (Exception ignored) { }
        }

        private int getIndexFile(File file) {
            return Integer.parseInt(file.getName().substring(0, defaultFileName.length()));
        }

        private void exportMemTableToFile(IMemTable<K,V> table) throws CreateDirectoryException, CreateFileException, IOException{
            File file = new File(pathToSaveFiles);

            if(!file.exists() || file.isFile()) {
                throw new CreateDirectoryException();
            }
            int indexNewFile = files.size() > 0 ? getIndexFile(files.get(files.size() - 1)) + 1 : 0;

            String nameNewFile = defaultFileName + indexNewFile;
            File newFile = new File(file.getAbsolutePath() + "/" + nameNewFile);

            if(newFile.exists() || !newFile.createNewFile()) {
                throw new CreateFileException();
            }
            try(FileOutputStream fos = new FileOutputStream(newFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                K firstKey = table.firstKey();
                K lastKey = table.lastKey();
                oos.writeObject(firstKey);
                oos.writeObject(lastKey);
                oos.writeObject(table);
            }
        }
    }
}
