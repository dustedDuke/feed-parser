package com.company;

//import com.company.Feed;
//import com.company.SettingsManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FeedManager {

    private static Map<URL, Feed> feeds;
    private static SettingsManager settingsManager;

    // TODO исправить дату на Duration
    private static final int defaultUpdatePeriod = 30;


    public FeedManager(SettingsManager settingsManager) {

        feeds = new HashMap<>();
        this.settingsManager = settingsManager;

        Map<String, String> settings = this.settingsManager.getAllFromPropFile();

        for(Map.Entry<String, String> entry : settings.entrySet()) {

            String[] values = entry.getValue().split("\\|");

            try {
                subscribeTo(new URL(entry.getKey()), values[0], LocalDateTime.parse(values[1]),
                        Integer.parseInt(values[2]));
            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            }

        }


    }

    public void subscribeTo(URL url, String fileName, LocalDateTime lastUpdateTime, int updatePeriod) {

        Feed feed = new Feed(url, fileName, lastUpdateTime, updatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);


    }

    public void subscribeTo(URL url, String fileName, int updatePeriod) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Feed feed = new Feed(url, fileName, currentDateTime, updatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        StringBuilder builder = new StringBuilder();
        builder.append(url.toString() + "|")
                .append(fileName + "|")
                .append(currentDateTime.toString() + "|")
                .append(Integer.toString(updatePeriod));

        settingsManager.setProp(url.toString(), builder.toString());
    }

    public void subscribeTo(URL url, String fileName) {
        Feed feed = new Feed(url, fileName, LocalDateTime.now(), defaultUpdatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void subscribeTo(URL url) {
        String fileHashName = Integer.toString(url.toString().hashCode());
        Feed feed = new Feed(url, fileHashName, LocalDateTime.now(), defaultUpdatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void unsubscribeFrom(URL url) {

        for(Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {
            if(feedEntry.getKey() == url) {

                feedEntry.getValue().interrupt();
                settingsManager.delProp(url.toString());

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

    public void setFeedAttribute() {}



}
