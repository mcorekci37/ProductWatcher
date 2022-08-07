package com.corekci.nio;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductProcessor {

    private final static String SEPARATOR = ":";
    private String name;
    private String path;
    private List<String> extensions;
    private CopyOnWriteArrayList<BlockingQueue<Product>> productQueues;

    public ProductProcessor(String name, String path, List<String> extensions, CopyOnWriteArrayList<BlockingQueue<Product>> productQueues) {
        this.name = name;
        this.path = path;
        this.extensions = extensions;
        this.productQueues = productQueues;
    }

    protected Product mapFileToProduct(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            //only care about first line
            for (String line : lines) {
                String[] arr = line.split(SEPARATOR);
                Product product = null;
                try {
                    product = new Product(path.toString(), arr[0],Double.valueOf(arr[1]));
                    deleteFile(path);
                }catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
                    System.out.println("FileUtils-->mapFileToProduct" + " | file content is not clear!");
                }
                return product;
            }
        } catch (IOException e) {
//            System.out.println("FileUtils-->mapFileToProduct" + " | error in file reading");
//            e.printStackTrace();
        }
        return null;
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        }
        catch (NoSuchFileException e) {
            System.out.println("FileUtils-->deleteFile | No such file/directory exists");
        }
        catch (DirectoryNotEmptyException e) {
            System.out.println("FileUtils-->deleteFile | Directory is not empty.");
        }
        catch (IOException e) {
            System.out.println("FileUtils-->deleteFile | Invalid permissions.");
        }
    }
    protected boolean filterExtensions(String filename) {
        for (String extension : extensions) {
            if(filename.endsWith(extension)){
                return true;
            }
        }
        return false;
    }
    protected void sendFileToResponsibleThread(Path path) {
        char c = path.getFileName().toString().toLowerCase().charAt(0);
        int index = c - 'a';
        BlockingQueue<Product> queue = this.productQueues.get(index);
        Product product = mapFileToProduct(path);
        if (product!=null){
//            System.out.println(this.getName() + " | adding product to Queue--> filePath = " + file.getAbsolutePath());
            queue.add(product);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public CopyOnWriteArrayList<BlockingQueue<Product>> getProductQueues() {
        return productQueues;
    }

    public void setProductQueues(CopyOnWriteArrayList<BlockingQueue<Product>> productQueues) {
        this.productQueues = productQueues;
    }
}
