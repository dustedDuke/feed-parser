package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SettingsManager {

    private static final String propFileName = "config.properties";
    private static Properties props;
    private static InputStream inputStream;

    // TODO пересмотреть SettingsManager IOException
    public SettingsManager() throws IOException {


        try {

            props = new Properties();
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            props.load(inputStream);


        } catch (Exception e) {
            System.out.println("Exception: " + e);

        } finally {
            inputStream.close();
        }

    }

    // Параметры могут вводится через точку (Nested)
    // TODO посмотреть как возвращать исключения при чтении файла
    public String getProp(String prop) {

        try {

            String propValue = props.getProperty(prop);
            return propValue;

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return null;
        }

    }

    public void setProp(String prop, String value) {

        try {

            props.putIfAbsent(prop, value);
            props.setProperty(prop, value);

        } catch (Exception e) {
            System.out.println("Exception: " + e);

        }

    }

    public void delProp(String prop) {
        try {

            props.remove(prop);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public Map<String, String>  getAllFromPropFile() {
        Map<String, String> data = new HashMap<>();

        for(Map.Entry<Object, Object> entry : props.entrySet()) {
            data.put((String)entry.getKey(), (String)entry.getValue());
        }

        return data;
    }

    public void clearSettings() {}

}
