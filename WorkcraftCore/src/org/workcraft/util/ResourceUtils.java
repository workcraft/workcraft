package org.workcraft.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtils {

    public static Set<String> getResources(String path) throws URISyntaxException, IOException {
        HashSet<String> result = new HashSet<String>();
        if (!path.endsWith("/")) {
            path += "/";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> dirUrls = classLoader.getResources(path);
        while (dirUrls.hasMoreElements()) {
            URL dirUrl = dirUrls.nextElement();
            result.addAll(getResources(path, dirUrl));
        }
        return result;
    }

    private static Set<String> getResources(String path, URL url) throws URISyntaxException, IOException {
        HashSet<String> result = new HashSet<String>();
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            File dir = new File(url.toURI());
            for (String fileName: dir.list()) {
                result.add(path + fileName);
            }
        } else if ("jar".equals(protocol)) {
            String dirPath = url.getPath();
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

}
