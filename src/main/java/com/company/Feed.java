package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.util.Date;
import java.io.InputStreamReader;
import java.util.List;

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
    private volatile String fileName;
    private volatile ZonedDateTime lastUpdateDateTime;
    private volatile Duration updatePeriod;

    private volatile List<String> itemsToRead;

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

    public Feed(URL url, String fileName, ZonedDateTime lastUpdateDateTime, Duration updatePeriod) {
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

                while(lastUpdateDateTime.compareTo(ZonedDateTime.now()) < 0) {

                    CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent();

                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(stream));
                    //System.out.println(feed.getTitle());

                    // TODO проверка даты в самом RSS


                    // TODO выбор полей
                    fileWriter.append(feed.toString());

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
