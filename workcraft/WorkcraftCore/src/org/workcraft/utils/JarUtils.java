package org.workcraft.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {

    public static final String FILE_PROTOCOL = "file";
    public static final String JAR_PROTOCOL = "jar";

    public static Set<String> getResourcePaths(String path) throws IOException {
        HashMap<String, URL> resourceToDirMap = new HashMap<>();
        if (!path.endsWith("/")) {
            path += "/";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> dirUrls = classLoader.getResources(path);
        while (dirUrls.hasMoreElements()) {
            URL dirUrl = dirUrls.nextElement();
            for (String resource : getResourcePaths(path, dirUrl)) {
                if (resourceToDirMap.containsKey(resource)) {
                    LogUtils.logError("Skipping resource '" + resource + "' from '" + dirUrl
                            + "' as it is already present in '" + resourceToDirMap.get(resource) + "'");
                }
                resourceToDirMap.put(resource, dirUrl);
            }
        }
        return new HashSet<>(resourceToDirMap.keySet());
    }

    private static Set<String> getResourcePaths(String path, URL dirUrl) throws IOException {
        HashSet<String> result = new HashSet<>();
        String protocol = dirUrl.getProtocol();
        if (FILE_PROTOCOL.equals(protocol)) {
            for (String fileName : getDirectoryContent(dirUrl)) {
                result.add(path + fileName);
            }
        } else if (JAR_PROTOCOL.equals(protocol)) {
            String dirPath = dirUrl.getPath();
            String jarPath = dirPath.substring(5, dirPath.indexOf("!"));
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if ((entryName.length() > path.length()) && entryName.startsWith(path)) {
                    result.add(entryName);
                }
            }
            jarFile.close();
        }
        return result;
    }

    private static Set<String> getDirectoryContent(URL dirUrl) {
        Set<String> result = new HashSet<>();
        URI uri = null;
        try {
            uri = dirUrl.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (uri != null) {
            File dir = new File(uri);
            String[] content = dir.list();
            if (content != null) {
                result.addAll(Arrays.asList(content));
            }
        }
        return result;
    }

}
