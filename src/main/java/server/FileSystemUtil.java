package server;

import java.io.File;

public class FileSystemUtil {

    private FileSystemUtil() { }

    public static File[] getFilesFromDirectory(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }

}
