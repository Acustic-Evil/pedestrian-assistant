package com.pedestrianassistant.Util.Media;

import java.io.InputStream;
import java.nio.file.Path;

public interface MediaStorage {
    /**
     * Extract a ZIP file into a unique directory.
     *
     * @param zipInputStream InputStream of the ZIP archive.
     * @return Path to the created directory.
     */
    Path extractZipToUniqueDir(InputStream zipInputStream);

    /**
     * Get the path to a directory by its name.
     *
     * @param dirName Name of the directory.
     * @return Path to the directory.
     */
    Path getDirectory(String dirName);

    /**
     * Delete a directory and its contents.
     *
     * @param dirName Name of the directory.
     */
    void deleteDirectory(String dirName);

    /**
     * Read a file from a given directory and return an InputStream.
     *
     * @param dirPath Path to the directory.
     * @param fileName Name of the file.
     * @return InputStream of the file.
     */
    InputStream readFile(Path dirPath, String fileName);
}
