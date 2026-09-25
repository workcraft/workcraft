package org.workcraft.utils;

import org.workcraft.exceptions.ArgumentException;

import java.util.EnumSet;
import java.util.LinkedHashSet;

public final class EnumUtils {

    private EnumUtils() {
    }

    public static <T extends Enum<T>> T itemFromString(String s, Class<T> enumType) {
        for (T item : EnumSet.allOf(enumType)) {
            if ((s != null) && s.equals(item.toString())) {
                return item;
            }
        }
        throw new ArgumentException("Cannot find value '" + s + "' in enum " + enumType);
    }

    public static <T extends Enum<T>> LinkedHashSet<String> orderedItemNames(Class<T> enumType) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (T item : EnumSet.allOf(enumType)) {
            result.add(item.toString());
        }
        return result;
    }

}
