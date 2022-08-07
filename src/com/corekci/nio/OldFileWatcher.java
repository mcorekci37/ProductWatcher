package com.corekci.nio;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class OldFileWatcher extends ProductProcessor implements Runnable {

    public OldFileWatcher(String name, String path, List<String> extensions, CopyOnWriteArrayList<BlockingQueue<Product>> productQueues) {
        super(name, path, extensions, productQueues);
    }

    @Override
    public void run() {
        processExistingFiles();
    }


    private void processExistingFiles() {
        File directoryPath = new File(this.getPath());
        //List of all files and directories
        Arrays.stream(directoryPath.listFiles())
                .filter(File::isFile)
                .map(File::getAbsolutePath)
                .filter(this::filterExtensions)
                .map(Path::of)
                .forEach(this::sendFileToResponsibleThread);
    }
}
