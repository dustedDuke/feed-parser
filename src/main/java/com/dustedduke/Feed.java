package com.dustedduke;

import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.util.Date;
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

/**
 * Скачивание и обработка отдельного RSS feed.
 */

public class Feed extends Thread {

    private volatile URL url;
    private volatile String name;
    private volatile String fileName;
    private volatile LocalDateTime lastUpdateDateTime;
    private volatile Duration updatePeriod;
    private volatile BlockingQueue<String> fileQueue;
    private volatile Set<String> itemsToRead;

    public String getFeedName() {
        return name;
    }
    public String getFileName() {
        return fileName;
    }
    public LocalDateTime getLastUpdatedateTime() {
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
    public void setLastUpdateDateTime(LocalDateTime lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }
    public void setUpdatePeriod(Duration updatePeriod) {
        this.updatePeriod = updatePeriod;
    }
    public void setItemsToRead(Set<String> itemsToRead) {
        this.itemsToRead = itemsToRead;
    }

    /**
     * Чтение параметров для будущего создания соединения
     * @param url адрес RSS feed
     * @param name название для дальнейшего обращения вместо ссылки
     * @param fileName название файла для хранения записей
     * @param itemsToRead множество полей для чтения
     * @param fileQueue очередь записи в файл, ассоциированная с данным фидом
     * @param lastUpdateDateTime время последнего обновления фида (при инициации выставляется текущее время)
     * @param updatePeriod период обновления фида
     */
    public Feed(URL url, String name, String fileName, Set<String> itemsToRead, BlockingQueue<String> fileQueue, LocalDateTime lastUpdateDateTime, Duration updatePeriod) {
        this.url = url;
        this.name = name;
        this.fileName = fileName;
        this.lastUpdateDateTime = lastUpdateDateTime;
        this.updatePeriod = updatePeriod;
        this.fileQueue = fileQueue;
        this.itemsToRead = itemsToRead;
    }

    /**
     * Фильтрация полей отдельной записи в соответствии с заданным фильтром полей.
     * @param entry запись Rome.
     * @return отфильтрованная запись в виде строки.
     */
    private String filterFeedEntry(SyndEntry entry) {

        String result = "";

        for(String item: itemsToRead) {

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

    @Override
    public void run() {

        try {

            CloseableHttpClient client = HttpClients.createMinimal();
            HttpUriRequest request = new HttpGet(url.toString());

            try {

                while(lastUpdateDateTime.compareTo(LocalDateTime.now()) < 0) {

                    CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent();

                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(stream));

                    // Проверка валидности фида
                    if(feed.getPublishedDate() == null || feed.getEntries() == null) {
                        this.interrupt();
                    }

                    // Проверка даты в самом RSS
                    Date lastUpdate = Date.from(lastUpdateDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    if(lastUpdate.compareTo(feed.getPublishedDate()) >= 0) {
                        continue;
                    }

                    // Выбор полей
                    //fileQueue.put(feed.toString()); -- зачем, если потом запись по отдельности?
                    for(SyndEntry entry: feed.getEntries()) {
                        fileQueue.put(filterFeedEntry(entry));
                    }

                    lastUpdateDateTime = LocalDateTime.now().plus(updatePeriod);

                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } finally {
            //System.out.println("done");
        }
    }

}
