package com.dustedduke;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Менеджер фидов.
 * Осуществляет создание, удаление фида, запрашивает сохранение настроек, создание новых файлов и очередей.
 * Хранит информацию о потоках фидов, их параметрах, файлах для записи, очередях записи.
 */

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

    private static final Duration defaultUpdatePeriod = Duration.parse("PT20.345S");
    private static final Set<String> defaultFields = new HashSet<>(Arrays.asList("title", "pubDate", "description"));


    /**
     * Инициация потоков, файлов и очередей с информацией из config.properties
     * @param settingsManager менеджер настроек, осуществляющий работу с файлом настроек
     */
    public FeedManager(SettingsManager settingsManager) {

        feeds = new HashMap<>();
        fileQueues = new HashMap<>();
        files = new HashMap<>();
        fileQueuesUsage = new HashMap<>();
        this.settingsManager = settingsManager;

        Map<String, String> settings = this.settingsManager.getAllFromPropFile();

        for(Map.Entry<String, String> entry : settings.entrySet()) {

            Map<String, String> values = this.settingsManager.jsonStringToMap(entry.getValue());

            try {

                URL url = new java.net.URL(entry.getKey().replace("|", "/"));
                String name = values.get(NAME);
                String fileName = values.get(FILENAME);
                Set<String> fields = new HashSet<>(Arrays.asList(values.get(FIELDS).split(" ")));
                Duration dur = Duration.parse(values.get(UPDATEPERIOD));
                LocalDateTime zdt = LocalDateTime.from(LocalDateTime.parse(values.get(LASTUPDATETIME)));
                subscribeTo(url, name, fileName, fields, zdt, dur);

            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Создание очереди записи на основе имени файла
     * @param fileName имя файла
     * @return очередь записи
     */
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

    /**
     * Создание нового файла для записи потока и добавление в feeds
     * @param fileName имя файла
     * @param fileQueue очередь записи в файл
     * @return объект FeedFile
     */
    private FeedFile newFeedFile(String fileName, BlockingQueue<String> fileQueue) {

        FeedFile feedFile = new FeedFile(fileName, fileQueue);
        files.put(fileName, feedFile);

        return feedFile;
    }

    /**
     * Запрос на добавление новой записи в файл настроек. Используется при создании подключения к фиду.
     * @param url адрес фида
     * @param name имя фида
     * @param fileName название файла для записи
     * @param fields читаемые поля
     * @param lastUpdateTime время последнего обновления фида
     * @param updatePeriod период обновления фида
     */
    private void newSettingsItem(URL url, String name, String fileName, Set<String> fields, LocalDateTime lastUpdateTime, Duration updatePeriod) {

        Map<String, String> settings = new HashMap<>();
        settings.put(NAME, name);
        settings.put(URL, url.toString());
        settings.put(FILENAME, fileName);
        settings.put(FIELDS, String.join(" ", fields));
        settings.put(LASTUPDATETIME, lastUpdateTime.toString());
        settings.put(UPDATEPERIOD, updatePeriod.toString());

        settingsManager.addItem(url.toString().replace("/", "|"), settings);

    }

    /**
     * Тестирование подключения к RSS потоку для проверки наличия записей и поля pubDate.
     * Также используется для определения доступных для чтения полей
     * @param url адрес RSS потока
     * @return доступные для чтения поля
     */
    public Set<String> testConnection(URL url) {

        CloseableHttpClient client = HttpClients.createMinimal();
        HttpUriRequest request = new HttpGet(url.toString());

        try {

            CloseableHttpResponse response = client.execute(request);
            InputStream stream = response.getEntity().getContent();

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(stream));

            if(feed.getEntries().get(0).getPublishedDate() == null || feed.getEntries() == null) {
                System.out.println("No entries or no pubDate.");
                return null;
            }

            return checkAvailableTags(feed);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


    /**
     * Определение доступных полей
     * @param feed объект RSS/Atom потока
     * @return доступные для чтения поля
     */
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

    /**
     * Подписка на поток сохранением параметров в config.properties и внутренних структурах FeedManager
     * @param url адрес потока
     * @param name имя потока
     * @param fileName имя файла
     * @param fields набор читаемых полей
     * @param lastUpdateTime последнее время обновления
     * @param updatePeriod период обновления
     */
    public void subscribeTo(URL url, String name, String fileName, Set<String> fields, LocalDateTime lastUpdateTime, Duration updatePeriod) {

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
        LocalDateTime lastUpdateTime = LocalDateTime.now();
        Feed feed = new Feed(url, name, fileName, fields, fileQueue, lastUpdateTime, updatePeriod);

        //Запуск потока файла
        FeedFile feedFile = newFeedFile(fileName, fileQueue);
        feedFile.start();

        //Запуск потока
        feed.start();
        feeds.put(url, feed);

        newSettingsItem(url, name, fileName, fields, lastUpdateTime, updatePeriod);

    }

    /**
     * Отписка от RSS/Atom потока. Удаляет файл из настроек и информацию из полей FeedManager.
     * Удаляет файл с содержимым фида, если он не используется другими фидами
     * @param url адрес потока
     */
    public void unsubscribeFrom(URL url) {

        System.out.println("Unsubscribing from " + url.toString());

        for(Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {

            //TODO проверить нужно ли
            if(feedEntry.getKey().toString().replace("/", "|").equals(url.toString().replace("/", "|"))) {

                feedEntry.getValue().interrupt();

                String fileName = settingsManager.getProp(url.toString().replace("/", "|"), "fileName");
                settingsManager.delItem(url.toString().replace("/", "|"));

                System.out.println("fileQueuesUsage.get(fileName) == " + fileQueuesUsage.get(fileName) + "  *" + fileName);

                if(fileQueuesUsage.get(fileName) == 1) {

                    files.get(fileName).interrupt();
                    files.remove(fileName);

                    fileQueues.remove(fileName);
                    fileQueuesUsage.remove(fileName);

                    //Удалить сам файл
                    File file = new File(fileName);

                    if(file.delete()) {
                        System.out.println("Feed file deleted successfully.");
                    } else {
                        System.out.println("Error while deleting feed file " + fileName + ". Please delete " +
                                "file manually if possible.");
                    }

                }

                // TODO очень плохо
                feeds.remove(feedEntry);

            }
        }

    }

    /**
     * Получение объекта фида из feeds по URL
     * @param url адрес потока
     * @return объект потока
     */
    public Feed getFeedFromURL(URL url) {
        return feeds.get(url);
    }

    public void stopAllThreads() {

        for (Map.Entry<URL, Feed> feedEntry: feeds.entrySet()) {
            feedEntry.getValue().interrupt();
        }

    }

    public Map<String, String> getAllFeedInfo() {
        return settingsManager.getAllFromPropFile();
    }


}
