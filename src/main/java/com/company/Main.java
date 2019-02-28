package com.company;

import com.company.ClInterface;
import com.company.SettingsManager;

import java.io.IOException;


public class Main {

    public static void main(String[] args) {

        SettingsManager settingsManager;
        FeedManager feedManager;

        try {

            settingsManager = new SettingsManager();

         } catch (IOException e) {
            System.out.println("Error while configuring settings.");
            System.out.println("Exception: " + e);
            return;
        }

        feedManager = new FeedManager(settingsManager);

        final ClInterface cl = new ClInterface(feedManager);
        cl.start();

    }
}
