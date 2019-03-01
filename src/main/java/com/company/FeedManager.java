package com.company;

//import com.company.Feed;
//import com.company.SettingsManager;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
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

    private static final String NAME = "name";
    private static final String FILENAME = "fileName";
    private static final String URL = "url";
    private static final String FIELDS = "fields";
    private static final String LASTUPDATETIME = "lastUpdateTime";
    private static final String UPDATEPERIOD = "updatePeriod";


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

            Map<String, String> values = this.settingsManager.jsonStringToMap(entry.getValue());

            try {

                URL url = new URL(entry.getKey());
                String name = values.get(NAME);
                String fileName = values.get(FILENAME);
                Set<String> fields = new HashSet<>(Arrays.asList(values.get(FIELDS).split(" ")));
                Duration dur = Duration.parse(values.get(UPDATEPERIOD));
                ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(values.get(LASTUPDATETIME)), ZoneId.of("Europe/Moscow"));

                //TODO не default fields
                subscribeTo(url, name, fileName, fields, zdt, dur);


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

    private void newSettingsItem(URL url, String name, String fileName, Set<String> fields, ZonedDateTime lastUpdateTime, Duration updatePeriod) {

        Map<String, String> settings = new HashMap<>();
        settings.put(NAME, name);
        settings.put(URL, url.toString());
        settings.put(FILENAME, fileName);
        settings.put(FIELDS, String.join(" ", fields));
        settings.put(LASTUPDATETIME, lastUpdateTime.toString());
        settings.put(UPDATEPERIOD, updatePeriod.toString());

        settingsManager.addItem(url.toString(), settings);

    }

    public Set<String> testConnection(URL url) {

        try {
            CloseableHttpClient client = HttpClients.createMinimal();
            HttpUriRequest request = new HttpGet(url.toString());

            try {

                CloseableHttpResponse response = client.execute(request);
                InputStream stream = response.getEntity().getContent();

                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(stream));

                // Проверка на наличие поля даты и наличие истории
                if(feed.getPublishedDate() == null || feed.getEntries() == null) {
                    return null;
                }


                return checkAvailableTags(feed);


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } finally {
            System.out.println("done");
        }

        return null;
    }

    public Set<String> checkAvailableTags(SyndFeed feed) {

        Set<String> tags = new HashSet<>();

        for(SyndEntry entry : feed.getEntries()) {

            if(entry.getAuthor() != null) tags.add("author");
            if(entry.getCategories() != null) tags.add("category");
            if(entry.getSource() != null) tags.add("feed");
            if(entry.getDescription() != null) tags.add("description");
            if(entry.getSource() != null) tags.add("feed");
            if(entry.getUpdatedDate() != null) tags.add("lastBuildDate");
            if(entry.getLink() != null) tags.add("link");
            if(entry.getPublishedDate() != null) tags.add("pubDate");
            if(entry.getTitle() != null) tags.add("title");

        }

        if(feed.getImage() != null) tags.add("image");
        if(feed.getGenerator() != null) tags.add("generator");
        if(feed.getCopyright() != null) tags.add("copyright");
        if(feed.getManagingEditor() != null) tags.add("managingEditor");

        return tags;

    }

    public void subscribeTo(URL url, String name, String fileName, Set<String> fields, ZonedDateTime lastUpdateTime, Duration updatePeriod) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        Feed feed = new Feed(url, name, fileName, fields, fileQueue, lastUpdateTime, updatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока фида
        feed.start();
        feeds.put(url, feed);


        newSettingsItem(url, name, fileName, fields, lastUpdateTime, updatePeriod);


    }

    public void subscribeTo(URL url, String name, String fileName, Set<String> fields, Duration updatePeriod) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        ZonedDateTime lastUpdateTime = ZonedDateTime.now();
        Feed feed = new Feed(url, name, fileName, fields, fileQueue, lastUpdateTime, updatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        newSettingsItem(url, name, fileName, fields, lastUpdateTime, updatePeriod);

    }

    public void subscribeTo(URL url, String name, String fileName, Set<String> fields) {

        BlockingQueue<String> fileQueue = newFileQueue(fileName);

        ZonedDateTime lastUpdateTime = ZonedDateTime.now();
        Feed feed = new Feed(url, name, fileName, fields, fileQueue, lastUpdateTime, defaultUpdatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        newSettingsItem(url, name, fileName, fields, lastUpdateTime, defaultUpdatePeriod);

    }

    public void subscribeTo(URL url) {

        String fileName = Integer.toString(url.toString().hashCode());
        BlockingQueue<String> fileQueue = newFileQueue(fileName);
        fileQueues.put(fileName, fileQueue);

        ZonedDateTime lastUpdateTime = ZonedDateTime.from(LocalDateTime.now());
        Feed feed = new Feed(url, fileName, fileName, defaultFields, fileQueue, lastUpdateTime, defaultUpdatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        newSettingsItem(url, fileName, fileName, defaultFields, lastUpdateTime, defaultUpdatePeriod);

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

    public Map<String, String> getAllFeedInfo() {
        return settingsManager.getAllFromPropFile();
    }


    public void stopAllThreads() {
        for (Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {

            feedEntry.getValue().interrupt();

        }
    }


    public Feed getFeedFromURL(URL url) {
        return feeds.get(url);
    }


    public Map<String, String> getInfoByName(String name) {
        Map<String, String> result = new HashMap<>();
//
//
//
//
//
        return result;
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
