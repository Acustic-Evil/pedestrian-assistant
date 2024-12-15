package com.pedestrianassistant.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    /**
     * Extracts a ZIP file from the provided InputStream to the specified destination directory.
     *
     * @param zipInputStream The InputStream of the ZIP file.
     * @param destinationDir The directory where the files will be extracted.
     */
    public static void extractZipToDirectory(InputStream zipInputStream, Path destinationDir) {
        try {
            // Ensure the destination directory exists
            Files.createDirectories(destinationDir);

            try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    System.out.println("Processing entry: " + entry.getName());

                    if (!entry.isDirectory()) {
                        // Получаем только имя файла (без родительских папок)
                        Path filePath = destinationDir.resolve(Path.of(entry.getName()).getFileName().toString());

                        // Создаем файл
                        Files.createDirectories(filePath.getParent());
                        Files.write(filePath, zis.readAllBytes(), StandardOpenOption.CREATE);
                        System.out.println("Extracted file: " + filePath);
                    }
                    zis.closeEntry();
                }
            }
        } catch (IOException e) {
            System.err.println("Error while extracting ZIP file to directory: " + destinationDir);
            throw new RuntimeException("Error while extracting ZIP file to directory: " + destinationDir, e);
        }
    }

}
