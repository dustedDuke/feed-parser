package com.dustedduke;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FeedManagerTest {

    @Test
    void stubRSS() {



    }

    @Test
    void periodicFeedToFile() {

    }

    @Test
    void multipleFeedIntoSingleFile() {

    }

    @Test
    void testConnection() {

        try {

            URL url = new URL("https://meduza.io/all/rss");
            SettingsManager settingsManager = new SettingsManager();
            FeedManager feedManager = new FeedManager(settingsManager);

            Set<String> availableTags = feedManager.testConnection(url);
            Set<String> actualTags = new HashSet<>(Arrays
                    .asList("image", "author", "link", "description", "category", "title", "pubDate"));

            assertEquals(availableTags, actualTags);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    void checkAvailableTags() {

        try {

            URL url = new URL("https://meduza.io/all/rss");
            SettingsManager settingsManager = new SettingsManager();
            FeedManager feedManager = new FeedManager(settingsManager);

            CloseableHttpClient client = HttpClients.createMinimal();
            HttpUriRequest request = new HttpGet(url.toString());

            CloseableHttpResponse response = client.execute(request);
            InputStream stream = response.getEntity().getContent();

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(stream));

            Set<String> availableTags = feedManager.checkAvailableTags(feed);
            Set<String> actualTags = new HashSet<>(Arrays
                    .asList("image", "author", "link", "description", "category", "title", "pubDate"));

            assertEquals(availableTags, actualTags);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    @Test
    void subscribeTo() {

        try {

            SettingsManager settingsManager = new SettingsManager();
            FeedManager feedManager = new FeedManager(settingsManager);

            URL url = new URL("https://meduza.io/all/rss");
            String name = "testFeed";
            String fileName = "testFeed";
            Set<String> fields = new HashSet<>(Arrays.asList("title", "pubDate"));
            LocalDateTime lastUpdateTime = LocalDateTime.now();
            Duration updatePeriod = Duration.parse("PT20.345S");

            feedManager.subscribeTo(url, name, fileName, fields, lastUpdateTime, updatePeriod);
            Feed feed = feedManager.getFeedFromURL(url);

            Map<String, String> settings = settingsManager.getAllFromPropFile();
            String settingsValue = settings.get(url.toString());

            assertNotNull(feed);
            assertNotNull(settingsValue);

        } catch (Exception e) {

        }


    }


    @Test
    void unsubscribeFrom() {

        try {

            SettingsManager settingsManager = new SettingsManager();
            FeedManager feedManager = new FeedManager(settingsManager);

            URL url = new URL("https://meduza.io/all/rss");
            String name = "testFeed";
            String fileName = "testFeed";
            Set<String> fields = new HashSet<>(Arrays.asList("title", "pubDate"));
            LocalDateTime lastUpdateTime = LocalDateTime.now();
            Duration updatePeriod = Duration.parse("PT20.345S");

            feedManager.subscribeTo(url, name, fileName, fields, lastUpdateTime, updatePeriod);
            Feed feed = feedManager.getFeedFromURL(url);

            feedManager.unsubscribeFrom(url);

            Map<String, String> settings = settingsManager.getAllFromPropFile();
            String settingsValue = settings.get(url.toString());

            assertNull(settingsValue);


        } catch (Exception e) {

        }



    }

}