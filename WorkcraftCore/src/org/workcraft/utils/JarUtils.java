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

public class JarUtils {

    public static Set<String> getResourcePaths(String path)
            throws URISyntaxException, IOException {

        HashMap<String, URL> resourceToDirMap = new HashMap<>();
        if (!path.endsWith("/")) {
            path += "/";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> dirUrls = classLoader.getResources(path);
        while (dirUrls.hasMoreElements()) {
            URL dirUrl = dirUrls.nextElement();
            for (String resource: getResourcePaths(path, dirUrl)) {
                if (resourceToDirMap.containsKey(resource)) {
                    LogUtils.logError("Skipping resource '" + resource + "' from '" + dirUrl
                            + "' as it is already present in '" + resourceToDirMap.get(resource) + "'");
                }
                resourceToDirMap.put(resource, dirUrl);
            }
        }
        return new HashSet<>(resourceToDirMap.keySet());
    }

    private static Set<String> getResourcePaths(String path, URL dirUrl)
            throws URISyntaxException, IOException {

        HashSet<String> result = new HashSet<>();
        String protocol = dirUrl.getProtocol();
        if ("file".equals(protocol)) {
            File dir = new File(dirUrl.toURI());
            for (String fileName: dir.list()) {
                result.add(path + fileName);
            }
        } else if ("jar".equals(protocol)) {
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

}
