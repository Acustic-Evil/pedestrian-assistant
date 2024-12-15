package com.pedestrianassistant.Service;

import java.io.InputStream;

public interface MediaService {

    /**
     * Process and store media files from the provided ZIP archive.
     *
     * @param zipInputStream InputStream of the ZIP archive containing media files.
     */
    void processAndStoreMedia(InputStream zipInputStream);
}
