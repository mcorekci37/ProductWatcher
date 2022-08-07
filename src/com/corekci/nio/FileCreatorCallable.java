package com.corekci.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

// generates random files containing Products
public class FileCreatorCallable implements Callable<String>{
    private static final Map<String, Double> productNames = new HashMap<String, Double>() {{
        put("Product0", 120D);
        put("Product1", 180D);
        put("Product2", 200D);
        put("Product3", 250D);
        put("Product4", 110D);
        put("Product5", 150D);
        put("Product6", 160D);
        put("Product7", 170D);
        put("Product8", 180D);
        put("Product9", 190D);
    }};

    private String name;
    private int fileCount;
    private String path;
    private long createFrequenceMillis;


    public FileCreatorCallable(String name, String path, int fileCount, long createFrequenceMillis) {
        this.name = name;
        this.fileCount = fileCount;
        this.path = path;
        this.createFrequenceMillis = createFrequenceMillis;
    }

    @Override
    public String call() {
        long startTime = System.currentTimeMillis();
        List<String> createdFileNames = new ArrayList<>();
        Map<String, Double> totalProductAmounts = new HashMap();
        for (int i = 0; i < fileCount; i++) {
            createRandomFile(createdFileNames, totalProductAmounts, i);
        }
//        System.out.println("size " + createdFileNames.size());
        System.out.println(totalProductAmounts);

        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;

        System.out.println(this.getClass().getName() + " | Program executed for " + seconds + " seconds");

        return totalProductAmounts.toString();
    }

    private void createRandomFile(List<String> createdFileNames, Map<String, Double> totalProductAmounts, int i) {
        Random rand = new Random(); //instance of random class
        String fileName;
        do {
            fileName = generateRandomFileName();
        }while (createdFileNames.contains(fileName));
        try {
            String fullPathStr = path + "/" + fileName;
            Path fullPath = Paths.get(fullPathStr);
            Path fullPathCreated = Files.createFile(fullPath);
            String productKey = "Product" + rand.nextInt(10);
            String strProduct = productKey + ":" + productNames.get(productKey);
            Files.write(fullPathCreated, strProduct.getBytes());
            totalProductAmounts.put(productKey,
                    totalProductAmounts.containsKey(productKey) ?
                            totalProductAmounts.get(productKey) + productNames.get(productKey)
                            : productNames.get(productKey));
//            System.out.println(fileName + " is created in " + path + " for " + i);
            createdFileNames.add(fileName);
        } catch (IOException e) {
            System.out.println(this.getClass().getName() + " | error in file creation");
            e.printStackTrace();
        }
        sleepForAWhile();
    }

    private void sleepForAWhile() {
        try {
            Thread.sleep(createFrequenceMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomFileName() {
        Random rand = new Random(); //instance of random class
        //generate random values from 0-25 for random cha
        int randInt = 'A' + rand.nextInt(26);
        char ch = (char) randInt;

        int randNum = rand.nextInt(10000);
        String fileName = ch + String.valueOf(randNum) + ".txt";
        return fileName;
    }

}
