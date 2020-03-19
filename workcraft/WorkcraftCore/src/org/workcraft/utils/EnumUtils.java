package org.workcraft.utils;

import java.util.EnumSet;

import org.workcraft.exceptions.ArgumentException;

public class EnumUtils {

    public static <T extends Enum<T>> T itemFromString(String s, Class<T> enumType) {
        for (T item: EnumSet.allOf(enumType)) {
            if ((s != null) && (s.equals(item.toString()))) {
                return item;
            }
        }
        throw new ArgumentException("Cannot find value '" + s + "' in enum " + enumType);
    }

}
