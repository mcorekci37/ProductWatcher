package com.corekci.nio;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RealTimeFileWatcher extends ProductProcessor implements Runnable {

    private volatile boolean running = true;

    public RealTimeFileWatcher(String name, String path, List<String> extensions, CopyOnWriteArrayList<BlockingQueue<Product>> productQueues) {
        super(name, path, extensions, productQueues);
    }

    public void logAndStop(){
        System.out.println("********** Stopping **********" + this.getName());
        System.out.println(this.getName() + " | " + " --> ");
        System.out.println(this.getName() + " | " + "productQueues" + " : " + this.getProductQueues());
        this.running = false;
    }


    @Override
    public void run() {
        watchFolderForEvent();
    }

    private void watchFolderForEvent() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path directory = Path.of(this.getPath());
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            while (running) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {

                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();

                    WatchEvent.Kind<?> kind = event.kind();
                    synchronized (kind){
                        if (filterExtensions(fileName.toString())){
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
//                                System.out.println(this.getName() + " | " + "A new file is created : " + fileName);
                                sendFileToResponsibleThread(Paths.get(directory + "\\" + fileName));
                            }
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
//                                System.out.println(this.getName() + " | " +"A file has been modified: " + fileName);
                                sendFileToResponsibleThread(Paths.get(directory + "\\" + fileName));
                            }
                        }
                    }
                }

                boolean valid = watchKey.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
