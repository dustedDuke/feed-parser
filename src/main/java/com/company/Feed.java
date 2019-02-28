package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.InputStreamReader;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Feed extends Thread {

    private URL url;
    private String fileName;
    private LocalDateTime lastUpdateDateTime;
    private int updatePeriod;

    public Feed(URL url, String fileName, LocalDateTime lastUpdateDateTime, int updatePeriod) {
        this.url = url;
        this.fileName = fileName;
        this.lastUpdateDateTime = lastUpdateDateTime;
        this.updatePeriod = updatePeriod;
    }

    // TODO разбить на функции

    public void run() {

        try {
            CloseableHttpClient client = HttpClients.createMinimal();
            HttpUriRequest request = new HttpGet(url.toString());

            try {

                FileWriter fileWriter = new FileWriter(fileName, true);

                while(lastUpdateDateTime.compareTo(LocalDateTime.now()) < 0) {

                    CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent();
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(stream));
                    //System.out.println(feed.getTitle());

                    // TODO проверка даты в самом RSS


                    // TODO выбор полей
                    fileWriter.append(feed.toString());

                    lastUpdateDateTime = LocalDateTime.now().plusMinutes(30);

                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } finally {
            System.out.println("done");
        }

    }

    // TODO переместить в utils
    private static LocalDateTime sum(LocalDateTime t1, LocalDateTime t2) {

        LocalDateTime newT1 = t1.plusYears(t2.getYear())
            .plusMonths(t2.getMonthValue())
            .plusDays(t2.getDayOfMonth())
            .plusHours(t2.getHour())
            .plusMinutes(t2.getMinute())
            .plusSeconds(t2.getSecond());

        return newT1;

    }

}
