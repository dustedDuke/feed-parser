package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class FeedFile extends Thread {

    protected BlockingQueue queue = null;
    private String fileName;

    public FeedFile(String fileName, BlockingQueue<String> queue) {
        this.queue = queue;
        this.fileName = fileName;

    }

    public void run() {
        try {

            FileWriter fileWriter = new FileWriter(fileName, true);
            while(true) {
                fileWriter.append(queue.take().toString());
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
