package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.util.Date;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Feed extends Thread {

    private volatile URL url;
    private volatile String name;
    private volatile String fileName;
    private volatile ZonedDateTime lastUpdateDateTime;
    private volatile Duration updatePeriod;
    private volatile BlockingQueue<String> fileQueue;
    private volatile Set<String> itemsToRead;

    public String getFeedName() {
        return name;
    }
    public String getFileName() {
        return fileName;
    }
    public ZonedDateTime getLastUpdatedateTime() {
        return lastUpdateDateTime;
    }

    public Duration getUpdatePeriod() {
        return updatePeriod;
    }

    public Set<String> getItemsToRead() {
        return itemsToRead;
    }


    public void setUrl(URL url) {
        this.url = url;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setLastUpdateDateTime(ZonedDateTime lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }
    public void setUpdatePeriod(Duration updatePeriod) {
        this.updatePeriod = updatePeriod;
    }
    public void setItemsToRead(Set<String> itemsToRead) {
        this.itemsToRead = itemsToRead;
    }




    public Feed(URL url, String name, String fileName, Set<String> itemsToRead, BlockingQueue<String> fileQueue, ZonedDateTime lastUpdateDateTime, Duration updatePeriod) {
        this.url = url;
        this.name = name;
        this.fileName = fileName;
        this.lastUpdateDateTime = lastUpdateDateTime;
        this.updatePeriod = updatePeriod;
        this.fileQueue = fileQueue;
        this.itemsToRead = itemsToRead;
    }

    private String filterFeedEntry(SyndEntry entry) {

        String result = "";

        for(String item: itemsToRead) {

            // TODO поставить в правильном порядке

            if(item.equals("author")) result += entry.getAuthor();
            if(item.equals("category")) result += entry.getCategories().get(0).toString();
            if(item.equals("channel") || item.equals("feed")) result += entry.getSource().toString();
            //if(item == "copyright" || item == "rights") result += entry.get
            if(item.equals("description") || item.equals("summary")) result += entry.getContents().toString();
            //if(item == "generator") result += entry.get
            //if(item == "guid" || item == "id") result += entry.get
            if(item.equals("lastBuildDate") || item.equals("updated")) result += entry.getUpdatedDate().toString();
            if(item.equals("link")) result += entry.getLink();
            if(item.equals("pubDate") || item.equals("published")) result += entry.getPublishedDate().toString();
            if(item.equals("title")) result += entry.getTitle();

        }

        return result;
    }

    // TODO разбить на функции

    public void run() {

        try {
            CloseableHttpClient client = HttpClients.createMinimal();
            HttpUriRequest request = new HttpGet(url.toString());

            try {

                while(lastUpdateDateTime.compareTo(ZonedDateTime.now()) < 0) {

                    CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent();

                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(stream));
                    //System.out.println(feed.getTitle());

                    // Проверка валидности фида
                    if(feed.getPublishedDate() == null || feed.getEntries() == null) {
                        this.interrupt();
                    }

                    // TODO проверка даты в самом RSS
                    Date lastUpdate = Date.from(lastUpdateDateTime.toInstant());
                    if(lastUpdate.compareTo(feed.getPublishedDate()) >= 0) {
                        continue;
                    }

                    // Выбор полей
                    fileQueue.put(feed.toString());
                    for(SyndEntry entry: feed.getEntries()) {
                        fileQueue.put(filterFeedEntry(entry));
                    }

                    lastUpdateDateTime = ZonedDateTime.now().plus(updatePeriod);

                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } finally {
            System.out.println("done");
        }

    }

    // TODO переместить в utils
//    private static ZonedDateTime sum(ZonedDateTime t1, ZonedDateTime t2) {
//
//        LocalDateTime newT1 = t1.plusYears(t2.getYear())
//            .plusMonths(t2.getMonthValue())
//            .plusDays(t2.getDayOfMonth())
//            .plusHours(t2.getHour())
//            .plusMinutes(t2.getMinute())
//            .plusSeconds(t2.getSecond());
//
//        ZonedDateTime newT1 =
//                t1.pl
//
//        return newT1;
//
//    }

}
