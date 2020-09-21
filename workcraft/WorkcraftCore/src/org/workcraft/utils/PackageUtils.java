package org.workcraft.utils;

public class PackageUtils {

    public static String getPackageName(Class<?> cls) {
        return cls.getPackage().getName();
    }

    public static String getPackagePath(Class<?> cls) {
        return getPackageName(cls).replace(".", "/");
    }

    public static String getPackagePath(Class<?> cls, String resourceName) {
        return getPackagePath(cls) + "/" + resourceName;
    }

}
