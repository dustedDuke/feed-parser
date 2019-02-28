package com.company;

import java.io.File;
import java.util.List;

public class FileManager {
    // TODO store all file descriptors here
    private List<String> fileNames;

    public String createFile(String fileName) {
        File f = new File(fileName);
        //fileNames.add()

        // TODO makedirs
        return fileName;
    }

}
