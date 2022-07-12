package org.workcraft.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SortUtils {

    public static <T> List<T> getSortedNatural(Collection<T> collection, Function<T, String> converter) {
        List<T> list = new ArrayList<>(collection);
        sortNatural(list, converter);
        return list;
    }

    public static <T> void sortNatural(List<T> list, Function<T, String> converter) {
        list.sort((s1, s2) -> compareNatural(converter.apply(s1), converter.apply(s2)));
    }

    public static <T> int compareNatural(T c1, T c2, Function<T, String> converter) {
        return compareNatural(converter.apply(c1), converter.apply(c2));
    }

    public static List<String> getSortedNatural(Collection<String> collection) {
        List<String> list = new ArrayList<>(collection);
        sortNatural(list);
        return list;
    }

    public static void sortNatural(List<String> list) {
        list.sort(SortUtils::compareNatural);
    }

    public static int compareNatural(String s1, String s2) {
        if ((s1 == null) && (s2 == null)) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        int len1 = s1.length();
        int len2 = s2.length();
        int i1 = 0;
        int i2 = 0;
        while ((i1 < len1) && (i2 < len2)) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);
            int b;
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                String d1 = getNumericalPart(s1, i1);
                String d2 = getNumericalPart(s2, i2);
                i1 += d1.length();
                i2 += d2.length();
                b = Integer.compare(Integer.parseInt(d1), Integer.parseInt(d2));
            } else {
                b = Character.compare(c1, c2);
                i1++;
                i2++;
            }
            if (b != 0) {
                return b;
            }
        }
        return Integer.compare(len1 - i1, len2 - i2);
    }

    private static String getNumericalPart(String s, int index) {
        StringBuilder result = new StringBuilder();
        for (int i = index; i < s.length();  i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            result.append(c);
        }
        return result.toString();
    }

}
