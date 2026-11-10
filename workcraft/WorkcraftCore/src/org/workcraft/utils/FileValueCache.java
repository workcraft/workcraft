package org.workcraft.utils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache of values that are extracted from the content of files, e.g. the type or the title of the model in a file.
 * A value is valid as long as the file is not modified, which is detected by its modification time and size.
 * A value of a file that was modified just before the value was extracted is not cached, as a further modification
 * within the resolution of the file system timestamps would not be detected.
 */
public class FileValueCache<T> {

    private static final int MAX_SIZE = 1000;
    private static final long RACY_PERIOD_MS = 2000;

    private record Key(String path, long modified, long size) {
    }

    private final Map<Key, T> values = new ConcurrentHashMap<>();

    private static Key getKey(File file) {
        return new Key(file.getAbsolutePath(), file.lastModified(), file.length());
    }

    /**
     * @return the value that was extracted from the current content of the file, or null if there is none
     */
    public T get(File file) {
        return (file == null) ? null : values.get(getKey(file));
    }

    /**
     * Remember the value that was extracted from the content of the file. If the file was modified while the value
     * was being extracted, then its modification time is recent, and the value is not cached.
     */
    public void put(File file, T value) {
        if ((file != null) && (value != null) && file.isFile()) {
            Key key = getKey(file);
            if (System.currentTimeMillis() - key.modified() > RACY_PERIOD_MS) {
                if (values.size() >= MAX_SIZE) {
                    values.clear();
                }
                values.put(key, value);
            }
        }
    }

}
