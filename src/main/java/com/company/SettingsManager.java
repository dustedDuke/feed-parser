package com.company;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SettingsManager {

    private final String propFileName = "config.properties";
    private Properties props;
    private InputStream inputStream;

    // TODO пересмотреть SettingsManager IOException
    SettingsManager() throws IOException {


        props = new Properties();
        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        try {

            props.load(inputStream);

        } catch (IOException e) {
            inputStream.close();
            throw new IOException(e);
        }

    }

    public void addItem(String item, Map<String, String> propTable) {
        JSONObject itemProps = new JSONObject(propTable);
        props.putIfAbsent(item, itemProps.toString());

    }


    public void delItem(String item) {
        props.remove(item);
    }


    public String getProp(String item, String prop) {

        String itemValue = props.getProperty(item);

        if(itemValue != null) {
            JSONObject itemProps = new JSONObject(itemValue);
            return itemProps.getString(prop);
        }

        return null;

    }

    public void setProp(String item, String prop, String value) {

        String itemValue = props.getProperty(item);
        JSONObject itemProps = new JSONObject(itemValue);
        itemProps.put(prop, value);
        props.setProperty(item, itemProps.toString());

    }

    public Map<String, String>  getAllFromPropFile() {

        Map<String, String> data = new HashMap<>();

        for(Map.Entry<Object, Object> entry : props.entrySet()) {
            data.put((String)entry.getKey(), (String)entry.getValue());
        }

        return data;
    }

    public Map<String, String> jsonStringToMap(String jsonString) {

        JSONObject object = new JSONObject(jsonString);

        Map<String, Object> map = object.toMap();

        Map<String,String> newMap = new HashMap<String,String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if(entry.getValue() instanceof String){
                newMap.put(entry.getKey(), (String) entry.getValue());
            }
        }

        return newMap;

    }

    public void clearSettings() {
        props.clear();
    }

}
