package com.dustedduke;

import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Менеджер файла настроек config.properties
 */

public class SettingsManager {

    private String propFileName = "config.properties";
    private Properties props;
    private InputStream inputStream;
    private FileOutputStream fout;

    /**
     * Создание объекта Properties и загрузка данных предыдущей сессии из файла настроек.
     * @throws IOException
     */
    SettingsManager() throws IOException {

        props = new Properties();
        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);


        try {

            props.load(inputStream);
            String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            propFileName = rootPath + propFileName;
            fout = new FileOutputStream(propFileName, false);

        } catch (IOException e) {
            File file = new File(propFileName);
            fout = new FileOutputStream(propFileName, false);
            System.out.println("Settings file created.");
            //inputStream.close();
            //fout.close();
            //throw new IOException(e);
        } finally {
            //inputStream.close();
            //fout.close();
        }

    }

    /**
     * Запись настроек в файл
     */
    private void storeProps() {

        try {

            props.store(fout, "Writing properties to file.");

        } catch (IOException e) {
            System.out.println("Error while saving Properties to file.");
        }

    }

    /**
     * Добавление элемента в файл настроек
     * @param item название элемента
     * @param propTable параметры элемента с названиями
     */
    public void addItem(String item, Map<String, String> propTable) {

        JSONObject itemProps = new JSONObject(propTable);
        props.putIfAbsent(item, itemProps.toString());
        storeProps();
    }

    /**
     * Удаление элемента из файла настроек
     * @param item
     */
    public void delItem(String item) {

        props.remove(item);
        storeProps();

    }

    /**
     * Получение параметра конкретного элемента
     * @param item название элемента
     * @param prop название параметра
     * @return значение параметра
     */
    public String getProp(String item, String prop) {

        String itemValue = props.getProperty(item);

        if(itemValue != null) {
            JSONObject itemProps = new JSONObject(itemValue);
            return itemProps.getString(prop);
        }

        return null;

    }

    /**
     * Задание параметра
     * @param item название элемента
     * @param prop название параметра
     * @param value новое значение параметра
     */
    public void setProp(String item, String prop, String value) {

        String itemValue = props.getProperty(item);
        JSONObject itemProps = new JSONObject(itemValue);

        itemProps.put(prop, value);
        props.setProperty(item, itemProps.toString());
        storeProps();

    }

    /**
     * Получение всех полей из файла настроек
     * @return поля с соответствующими параметрами
     */
    public Map<String, String>  getAllFromPropFile() {

        Map<String, String> data = new HashMap<>();

        for(Map.Entry<Object, Object> entry : props.entrySet()) {
            data.put((String)entry.getKey(), (String)entry.getValue());
        }

        return data;
    }


    /**
     * Конвертация JSON строки в отображение
     * @param jsonString
     * @return элементы объекта JSON
     */
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

    /**
     * Очистка файла настроек и внутреннего представления настроек.
     */
    public void clearSettings() {
        props.clear();
        storeProps();
    }

}
