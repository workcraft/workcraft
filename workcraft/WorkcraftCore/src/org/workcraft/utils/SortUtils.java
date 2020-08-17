package org.workcraft.utils;

import java.util.Collections;
import java.util.List;

public class SortUtils {

    public static void sortNatural(List<String> list) {
        Collections.sort(list, (s1, s2) -> compareNatural(s1, s2));
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
            int b = 0;
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                String d1 = getNumericalPart(s1, i1);
                String d2 = getNumericalPart(s2, i2);
                i1 += d1.length();
                i2 += d2.length();
                b = Integer.compare(Integer.valueOf(d1), Integer.valueOf(d2));
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
        String result = "";
        for (int i = index; i < s.length();  i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            result += c;
        }
        return result;
    }

}
