package com.corekci.nio;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileProcessorThread extends Thread {
    private String path;
    private Character processingChar;
    private BlockingQueue<Product> productQueue;
    private ConcurrentHashMap<String, Double> productAmountsMap;
    private CopyOnWriteArrayList<Product> processedProducts;
    private Double totalAmountsProcessed;
    private volatile boolean running = true;

    public FileProcessorThread() {
    }

    public FileProcessorThread(String name, String path, Character processingChar, BlockingQueue<Product> productQueue, ConcurrentHashMap<String, Double> productAmountsMap) {
        super(name);
        this.path = path;
        this.processingChar = processingChar;
        this.productQueue = productQueue;
        this.productAmountsMap = productAmountsMap;
        this.processedProducts = new CopyOnWriteArrayList<>();
        this.totalAmountsProcessed = Double.valueOf(0);
    }

    public synchronized void incrementAmount(Product product){
        processedProducts.add(product);
        totalAmountsProcessed+=product.getAmount();
    }

    public void logAndStop(){
        System.out.println("********** Stopping **********" + this.getName());
        System.out.println(this.getName() + " | " +" --> ");
        System.out.println(this.getName() + " | " +"processedProducts" + " : " + processedProducts);
        System.out.println(this.getName() + " | " +"totalAmountsProcessed" + " : " + totalAmountsProcessed);
        this.running = false;
    }

    @Override
    public void run() {
        while (running){
            if (!productQueue.isEmpty()){
                Product product = productQueue.poll();
                System.out.println("******************************************************************************************************************************");
                System.out.println(this.getName() + " | " +product.getFilePath() + " is processed");

                this.incrementProductAmount(product);
                this.incrementAmount(product);

                System.out.println(this.getName() + " | " +" global amount of the last received product: ");
                System.out.println(this.getName() + " | " +productAmountsMap.get(product.getName()));
                System.out.println(this.getName() + " | " +" all products processed: ");
                System.out.println(this.getName() + " | " +this.processedProducts);
                System.out.println();
            }
        }
    }

    private synchronized void incrementProductAmount(Product product) {
        if (productAmountsMap.containsKey(product.getName())) {
            productAmountsMap.put(product.getName(), productAmountsMap.get(product.getName()) + product.getAmount());
        }else {
            productAmountsMap.put(product.getName(), product.getAmount());
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Character getProcessingChar() {
        return processingChar;
    }

    public void setProcessingChar(Character processingChar) {
        this.processingChar = processingChar;
    }

    public Queue<Product> getProductQueue() {
        return productQueue;
    }

    public void setProductQueue(BlockingQueue<Product> productQueue) {
        this.productQueue = productQueue;
    }

    public ConcurrentHashMap<String, Double> getProductAmountsMap() {
        return productAmountsMap;
    }

    public void setProductAmountsMap(ConcurrentHashMap<String, Double> productAmountsMap) {
        this.productAmountsMap = productAmountsMap;
    }

    @Override
    public String toString() {
        return "FileProcessorThread{" +
                "path='" + path + '\'' +
                ", processingChar=" + processingChar +
                ", productQueue=" + productQueue +
                ", productAmountsMap=" + productAmountsMap +
                ", processedProducts=" + processedProducts +
                ", totalAmountsProcessed=" + totalAmountsProcessed +
                '}';
    }
}
