package com.dustedduke;

import java.io.IOException;


public class Main {

    public static void main(String[] args) {

        SettingsManager settingsManager;
        FeedManager feedManager;



        settingsManager = new SettingsManager();


        feedManager = new FeedManager(settingsManager);

        final ClInterface cl = new ClInterface(feedManager);
        cl.start();

    }
}
