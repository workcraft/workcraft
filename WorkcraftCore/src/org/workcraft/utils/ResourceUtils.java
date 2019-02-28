package org.workcraft.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtils {

    public static Set<String> getResources(String path) throws URISyntaxException, IOException {
        HashMap<String, URL> resourceToDirMap = new HashMap<String, URL>();
        if (!path.endsWith("/")) {
            path += "/";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> dirUrls = classLoader.getResources(path);
        while (dirUrls.hasMoreElements()) {
            URL dirUrl = dirUrls.nextElement();
            for (String resource: getResources(path, dirUrl)) {
                if (resourceToDirMap.containsKey(resource)) {
                    LogUtils.logError("Skipping resource '" + resource + "' from '" + dirUrl
                            + "' as it is already present in '" + resourceToDirMap.get(resource) + "'");
                }
                resourceToDirMap.put(resource, dirUrl);
            }
        }
        return new HashSet<>(resourceToDirMap.keySet());
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
