package com.c00098391.planttracker;

import java.io.File;

/**
 * Created by Darran on 11/01/2019.
 */

public class FolderUtil {

    private FolderUtil(){

    }

    public static void createDefaultFolder(String dirPath){

        File directory = new File(dirPath);
        if (!directory.exists()){
            directory.mkdir();
        }
    }

    public boolean checkFileExists(String filePath){
        File file = new File(filePath);
        return file.exists();
    }
}