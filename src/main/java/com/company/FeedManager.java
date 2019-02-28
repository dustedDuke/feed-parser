package com.company;

//import com.company.Feed;
//import com.company.SettingsManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class FeedManager {

    private static Map<URL, Feed> feeds;
    private static SettingsManager settingsManager;

    // TODO исправить дату на Duration
    private static final Duration defaultUpdatePeriod = Duration.parse("PT20.345S");


    public FeedManager(SettingsManager settingsManager) {

        feeds = new HashMap<>();
        this.settingsManager = settingsManager;

        Map<String, String> settings = this.settingsManager.getAllFromPropFile();

        for(Map.Entry<String, String> entry : settings.entrySet()) {

            String[] values = entry.getValue().split("\\|");

            try {
                LocalDateTime ldt = LocalDateTime.parse(values[1]);
                subscribeTo(new URL(entry.getKey()), values[0], ZonedDateTime.of(ldt, ZoneId.of("Europe/Moscow")),
                        Duration.parse(values[2]));
            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            }

        }

        // TODO сбор всех настроек в конце


    }

    public void subscribeTo(URL url, String fileName, ZonedDateTime lastUpdateTime, Duration updatePeriod) {

        Feed feed = new Feed(url, fileName, lastUpdateTime, updatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);


    }

    public void subscribeTo(URL url, String fileName, Duration updatePeriod) {

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        Feed feed = new Feed(url, fileName, currentDateTime, updatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        Map<String, String> set = new HashMap<>();
        set.put("fileName", fileName);
        set.put("lastUpdateTime", currentDateTime.toString());
        set.put("updatePeriod", updatePeriod.toString());

        settingsManager.addItem(url.toString(), set);

    }

    public void subscribeTo(URL url, String fileName) {
        Feed feed = new Feed(url, fileName, ZonedDateTime.now(), defaultUpdatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void subscribeTo(URL url) {
        String fileHashName = Integer.toString(url.toString().hashCode());
        Feed feed = new Feed(url, fileHashName, ZonedDateTime.from(LocalDateTime.now()), defaultUpdatePeriod);

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

    }

    public void unsubscribeFrom(URL url) {

        for(Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {
            if(feedEntry.getKey() == url) {

                feedEntry.getValue().interrupt();
                settingsManager.delItem(url.toString());

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
