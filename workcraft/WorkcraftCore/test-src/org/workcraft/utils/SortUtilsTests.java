package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class SortUtilsTests {

    @Test
    void compareNaturalTest() {
        Assertions.assertEquals(0, SortUtils.compareNatural(null, null));
        Assertions.assertEquals(-1, SortUtils.compareNatural(null, "a1"));
        Assertions.assertEquals(1, SortUtils.compareNatural("a1", null));
        Assertions.assertEquals(-1, SortUtils.compareNatural("a1", "a2"));
        Assertions.assertEquals(-1, SortUtils.compareNatural("a1", "a12"));
        Assertions.assertEquals(1, SortUtils.compareNatural("a10", "a2"));
        Assertions.assertEquals(1, SortUtils.compareNatural("a12", "a2"));
        Assertions.assertEquals(0, SortUtils.compareNatural("a1b", "a1b"));
        Assertions.assertEquals(1, SortUtils.compareNatural("123", "23"));
        Assertions.assertEquals(0, SortUtils.compareNatural("123", "123"));
        Assertions.assertEquals(-48, SortUtils.compareNatural("1", "a1"));
        Assertions.assertEquals(-1, SortUtils.compareNatural("a", "a1"));
    }

    @Test
    void sortNaturalTest() {
        List<String> sortedList = Arrays.asList(
                "1", "2", "13", "22",
                "a", "a1", "a2", "a13", "a22",
                "b", "b123", "bb",
                "c", "c1d", "cd", "cd");

        List<String> list = new ArrayList<>(sortedList);
        Collections.shuffle(list);
        SortUtils.sortNatural(list);
        Assertions.assertEquals(sortedList, list);
    }

}
