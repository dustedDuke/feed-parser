package com.company;

//import com.company.Feed;
//import com.company.SettingsManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FeedManager {

    private static Map<URL, Feed> feeds;
    private static Map<String, FeedFile> files;
    private static Map<String, BlockingQueue<String>> fileQueues;
    private static Map<String, Integer> fileQueuesUsage;
    private static SettingsManager settingsManager;

    // TODO исправить дату на Duration
    private static final Duration defaultUpdatePeriod = Duration.parse("PT20.345S");
    private static final Set<String> defaultFields = new HashSet<>(Arrays.asList("title", "pubDate", "description"));


    public FeedManager(SettingsManager settingsManager) {

        feeds = new HashMap<>();
        fileQueues = new HashMap<>();
        this.settingsManager = settingsManager;

        Map<String, String> settings = this.settingsManager.getAllFromPropFile();

        for(Map.Entry<String, String> entry : settings.entrySet()) {

            String[] values = entry.getValue().split("\\|");

            try {

                URL url = new URL(entry.getKey());
                String fileName = values[0];
                Duration dur = Duration.parse(values[2]);

                ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(values[1]), ZoneId.of("Europe/Moscow"));

                //TODO не default fields
                subscribeTo(url, fileName, defaultFields, zdt, dur);


            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            }

        }

        // TODO сбор всех настроек в конце


    }

    private BlockingQueue<String> newFileQueue(String fileName) {
        BlockingQueue<String> fileQueue;
        if(!fileQueues.containsKey(fileName)) {
            fileQueue = new LinkedBlockingQueue<>();
            fileQueues.put(fileName, fileQueue);
            fileQueuesUsage.put(fileName, 1);
        } else {
            fileQueue = fileQueues.get(fileName);
            fileQueuesUsage.replace(fileName, fileQueuesUsage.get(fileName) + 1);
        }

        return fileQueue;
    }

    private FeedFile newFeedFile(String fileName, BlockingQueue<String> fileQueue) {
        FeedFile feedFile = new FeedFile(fileName, fileQueue);
        files.put(fileName, feedFile);

        return feedFile;
    }

    public void subscribeTo(URL url, String fileName, Set<String> fields, ZonedDateTime lastUpdateTime, Duration updatePeriod) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        Feed feed = new Feed(url, fileName, fields, fileQueue, lastUpdateTime, updatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока фида
        feed.start();
        feeds.put(url, feed);


    }

    public void subscribeTo(URL url, String fileName, Set<String> fields, Duration updatePeriod) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        Feed feed = new Feed(url, fileName, fields, fileQueue, currentDateTime, updatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        Map<String, String> set = new HashMap<>();
        set.put("fileName", fileName);
        set.put("lastUpdateTime", currentDateTime.toString());
        set.put("updatePeriod", updatePeriod.toString());

        settingsManager.addItem(url.toString(), set);

    }

    public void subscribeTo(URL url, String fileName, Set<String> fields) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        Feed feed = new Feed(url, fileName, fields, fileQueue, ZonedDateTime.now(), defaultUpdatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void subscribeTo(URL url) {

        String fileHashName = Integer.toString(url.toString().hashCode());
        BlockingQueue<String> fileQueue = newFileQueue(fileHashName);
        fileQueues.put(fileHashName, fileQueue);

        Feed feed = new Feed(url, fileHashName, defaultFields, fileQueue, ZonedDateTime.from(LocalDateTime.now()), defaultUpdatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileHashName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void unsubscribeFrom(URL url) {

        for(Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {
            if(feedEntry.getKey() == url) {

                feedEntry.getValue().interrupt();
                settingsManager.delItem(url.toString());

                //Удаление очереди на файл после удаления фида
                String fileName = settingsManager.getProp(url.toString(), "fileName");

                // Проверка, ссылается ли кто-то на этот файл
                if(fileQueuesUsage.get(fileName) == 1) {

                    files.get(fileName).interrupt();
                    files.remove(fileName);

                    fileQueues.remove(fileName);
                    fileQueuesUsage.remove(fileName);
                }

                // TODO очень плохо
                feeds.remove(feedEntry);

            }
        }

    }

    public Map<URL, Feed> getAllFeeds() {
        return feeds;
    }

    public void stopAllThreads() {
        for (Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {

            feedEntry.getValue().interrupt();

        }
    }

    public void getAllFeedAttribures() {}



    public void changeFeedUrl(URL url, URL newUrl) {

    }

    public void changeFeedFileName(URL url, String newFileName) {

    }
    public void changeFeedLastUpdateTime(URL url, LocalDateTime newUpdateTime) {

    }
    public void changeFeedUpdatePeriod(URL url, Duration dur) {

        Feed feed = feeds.get(url);
        feed.setUpdatePeriod(dur);

    }


}
