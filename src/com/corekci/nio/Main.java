package com.corekci.nio;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static final int LETTER_COUNT = 26;

    public static void main(String[] args) {
        // write your code here
        long startTime = System.currentTimeMillis();

        final CopyOnWriteArrayList<BlockingQueue<Product>> productQueues = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<String, Double> productAmountsMap = new ConcurrentHashMap<>();

        String path = System.getProperty("user.dir")+"\\watchingFolder";

        final CopyOnWriteArrayList<FileProcessorThread> fileProcessorThreads = new CopyOnWriteArrayList<>();
        createFileProcessorThreads(fileProcessorThreads, productQueues, productAmountsMap, path);

        List<String> extensions = Collections.singletonList(".txt");
        RealTimeFileWatcher realTimeFileWatcher = new RealTimeFileWatcher("Real Time File Watcher Thread", path, extensions, productQueues);
        OldFileWatcher oldFileWatcher = new OldFileWatcher("Old File Watcher Thread", path, extensions, productQueues);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FileCreatorCallable fileCreatorCallable = new FileCreatorCallable("File Creator Callable Thread", path, 50, 1000);

        //starting threads
        fileProcessorThreads.forEach(FileProcessorThread::start);
        new Thread(oldFileWatcher).start();
        sleepFor(1000);
        new Thread(realTimeFileWatcher).start();
        sleepFor(1000);

        Future<String> message = executor.submit(fileCreatorCallable);
        String messageFromCreator = null;
        try {
            messageFromCreator = message.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //wait for user input to stop all threads
        waitForUserSignal(productQueues, productAmountsMap, fileProcessorThreads, realTimeFileWatcher, executor, messageFromCreator);

        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;
        System.out.println("Main Program executed for " + seconds + " seconds");
    }

    private static void waitForUserSignal(CopyOnWriteArrayList<BlockingQueue<Product>> productQueues, ConcurrentHashMap<String, Double> productAmountsMap, CopyOnWriteArrayList<FileProcessorThread> fileProcessorThreads, RealTimeFileWatcher realTimeFileWatcher, ExecutorService executor, String messageFromCreator) {
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        if (myObj.hasNext()) {
            executor.shutdown();
            realTimeFileWatcher.logAndStop();
            fileProcessorThreads.forEach(FileProcessorThread::logAndStop);
            System.out.println("**Processor*"+ productAmountsMap +"***");
            System.out.println("**Creator***"+ messageFromCreator +"***");
            //check only created files by Thread matches with productAmountsMap
            System.out.println(messageFromCreator.equals(productAmountsMap.toString()));
            System.out.println(productQueues);
        }
    }

    private static void createFileProcessorThreads(CopyOnWriteArrayList<FileProcessorThread> fileProcessorThreads, CopyOnWriteArrayList<BlockingQueue<Product>> productQueues, ConcurrentHashMap<String, Double> productAmountsMap, String path) {
        for (int i = 0; i < LETTER_COUNT; i++) {
            BlockingQueue<Product> productQueue = new LinkedBlockingQueue<>();
            productQueues.add(productQueue);

            char ch = (char) ((int)'a' + i);
            String threadName = ch + " processing thread";
            FileProcessorThread fileProcessorThread = new FileProcessorThread(threadName, path,ch,productQueue, productAmountsMap);
            fileProcessorThreads.add(fileProcessorThread);
        }
    }

    //wait for #millis
    private static void sleepFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
