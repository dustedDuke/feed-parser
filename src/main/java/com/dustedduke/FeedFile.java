package com.dustedduke;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Поток записи в файл, ассоциированный с одним или несколькими фидами.
 */

public class FeedFile extends Thread {

    protected BlockingQueue queue = null;
    private String fileName;

    /**
     * Сохранение параметров для создания файла с очередью
     * @param fileName название файла
     * @param queue очередь записи из фида
     */
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
            System.out.println("Interrupted Feed thread");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
