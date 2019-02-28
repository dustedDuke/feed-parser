package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ClientInfoStatus;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClInterface {

    private static Scanner in;
    private static final String messageFormat = "\t%-20s%s%n";
    private FeedManager feedManager;

    public ClInterface(FeedManager feedManager) {
        this.feedManager = feedManager;
    }

    private void showWelcomeMessage() {
        System.out.println("***RSS reader***");
        //TODO Вывод времени последнего использования
        System.out.println("Last usage: 18:29 2019-02-25");
        System.out.println("type 'help' for help");
    }

    private void showHelp() {
        System.out.printf(messageFormat, "add [URL]" ,"add new feed");
        System.out.printf(messageFormat, "rm [URL]", "stop feed subscription");
        System.out.printf(messageFormat, "list", "show all subscriptions");
        System.out.printf(messageFormat, "params", "add new feed");
        System.out.printf(messageFormat, "exit", "exit");
    }

    private void showOptions() {
        System.out.printf(messageFormat, "par [feed ID]", "show current feed parameters");
        //TODO Изменение параметров feed
        System.out.printf(messageFormat, "freq [feed ID] [freq]", "reload period");
        System.out.printf(messageFormat, "file [file path]", "choose file to store feed");
        System.out.printf(messageFormat, "batch [batch size]", "choose batch size");
    }

    public void start() {
        showWelcomeMessage();
        in = new Scanner(System.in);
        readMainOptions();
    }

    public void readMainOptions() {
        String input = "";
        try {
            while (!input.equalsIgnoreCase("exit")) {
                System.out.print(">> ");
                input = in.nextLine();
                String[] splittedInput = input.split("\\s+");
                if (splittedInput[0].equals("add")) {
                    System.out.println("Adding new feed...");
                    feedManager.subscribeTo(new URL(splittedInput[1]));
                } else if (splittedInput[0].equals("rm")) {
                    feedManager.unsubscribeFrom(new URL(splittedInput[1]));
                } else if (splittedInput[0].equals("list")) {

                    // do something else
                    System.out.println(feedManager.getAllFeeds().toString());

                } else if (splittedInput[0].equals("params")) {
                    showOptions();
                    readSettingsOptions();
                } else if (splittedInput[0].equals("help")) {
                    showHelp();
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Try again.");
            System.out.print(">> ");
            input = in.nextLine();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL. Try again.");
            System.out.print(">> ");
            input = in.nextLine();
        }

        feedManager.stopAllThreads();
    }

    public void readSettingsOptions() {
        String input = "";
        try {
            while (!input.equalsIgnoreCase("exit")) {
                System.out.print(">> ");
                input = in.nextLine();
                String[] splittedInput = input.split("\\s+");
                if (splittedInput[0].equals("par")) {
                    //do something
                } else if (splittedInput[0].equals("freq")) {
                    //do something else
                } else if (splittedInput[0].equals("file")) {
                    // do something else
                } else if (splittedInput[0].equals("batch")) {
                // do something else
                }
            }

        } catch (InputMismatchException e) {
            System.out.println("Try again.");
            System.out.print(">> ");
            input = in.nextLine();
        }
    }

}