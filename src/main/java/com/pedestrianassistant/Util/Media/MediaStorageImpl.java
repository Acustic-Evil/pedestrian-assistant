package com.pedestrianassistant.Util.Media;

import com.pedestrianassistant.Util.ZipUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MediaStorageImpl implements MediaStorage {

    @Value("${media.storage.path}")
    private String baseStoragePathStr;

    @Value("${media.storage.package.report.name}")
    private String packageBaseReportName;

    private Path baseStoragePath;

    @PostConstruct
    public void init() throws IOException {
        // Validate and initialize the base storage directory
        if (baseStoragePathStr == null || baseStoragePathStr.isBlank()) {
            throw new IllegalArgumentException("The property 'media.storage.path' is not set in the configuration.");
        }
        baseStoragePath = Paths.get(baseStoragePathStr);
        Files.createDirectories(baseStoragePath); // Create the directory if it doesn't exist
    }

    @Override
    public Path extractZipToUniqueDir(InputStream zipInputStream) {
        // Get today's date as directory name
        String todayDate = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        Path dailyDirPath = baseStoragePath.resolve(todayDate);

        try {
            // Check if the daily directory exists; if not, create it
            if (!Files.exists(dailyDirPath)) {
                Files.createDirectories(dailyDirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating daily directory: " + dailyDirPath, e);
        }

        // Generate a unique file name for the ZIP extraction
        int index = 1;
        Path uniqueFilePath;
        do {
            String uniqueFileName = packageBaseReportName + "_" + index + "_" + todayDate;
            uniqueFilePath = dailyDirPath.resolve(uniqueFileName);
            index++;
        } while (Files.exists(uniqueFilePath));

        // Extract the ZIP file to the daily directory
        ZipUtil.extractZipToDirectory(zipInputStream, uniqueFilePath);

        return uniqueFilePath;
    }

    @Override
    public Path getDirectory(String dirName) {
        // Resolve the directory path and validate its existence
        Path dirPath = baseStoragePath.resolve(dirName);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new RuntimeException("Directory not found: " + dirName);
        }
        return dirPath;
    }

    @Override
    public void deleteDirectory(String dirName) {
        Path dirPath = baseStoragePath.resolve(dirName);
        try {
            if (Files.exists(dirPath)) {
                // Delete files and subdirectories in reverse order
                Files.walk(dirPath)
                        .sorted((path1, path2) -> path2.compareTo(path1)) // Reverse order for deletion
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Error deleting file or directory: " + path, e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting directory: " + dirName, e);
        }
    }
}
