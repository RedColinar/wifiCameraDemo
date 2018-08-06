package com.example.pq.wificamerademo.util;

import java.io.File;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 19:06
 * @description
 */
public class FileUtils {
    private final static String FILENAME_SEQUENCE_SEPARATOR = "-";

    public static String createUniqueFilename(String fileNamePath) {
        if(fileNamePath == null){
            return "error";
        }
        String filename = fileNamePath.substring(0, fileNamePath.lastIndexOf("."));
        String extension = fileNamePath.substring(fileNamePath.lastIndexOf("."), fileNamePath.length());
        String fullFilename = filename + extension;
        if(!new File(fullFilename).exists()) {
            return fullFilename;
        }
        filename = filename + FILENAME_SEQUENCE_SEPARATOR;
        int sequence = 1;

        for (int iteration = 0; iteration < 10000; ++iteration) {
            fullFilename = filename + sequence + extension;
            if (!new File(fullFilename).exists()) {
                return fullFilename;
            }
            sequence +=  1;
        }
        return fullFilename;
    }
}
