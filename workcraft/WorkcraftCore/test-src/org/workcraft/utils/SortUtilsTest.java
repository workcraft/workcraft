package org.workcraft.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SortUtilsTest {

    @Test
    public void compareNaturalTest() {
        Assert.assertEquals(0, SortUtils.compareNatural(null, null));
        Assert.assertEquals(-1, SortUtils.compareNatural(null, "a1"));
        Assert.assertEquals(1, SortUtils.compareNatural("a1", null));
        Assert.assertEquals(-1, SortUtils.compareNatural("a1", "a2"));
        Assert.assertEquals(-1, SortUtils.compareNatural("a1", "a12"));
        Assert.assertEquals(1, SortUtils.compareNatural("a10", "a2"));
        Assert.assertEquals(1, SortUtils.compareNatural("a12", "a2"));
        Assert.assertEquals(0, SortUtils.compareNatural("a1b", "a1b"));
        Assert.assertEquals(1, SortUtils.compareNatural("123", "23"));
        Assert.assertEquals(0, SortUtils.compareNatural("123", "123"));
        Assert.assertEquals(-48, SortUtils.compareNatural("1", "a1"));
        Assert.assertEquals(-1, SortUtils.compareNatural("a", "a1"));
    }

    @Test
    public void sortNaturalTest() {
        List<String> sortedList = Arrays.asList(
                "1", "2", "13", "22",
                "a", "a1", "a2", "a13", "a22",
                "b", "b123", "bb",
                "c", "c1d", "cd", "cd");

        List<String> list = new ArrayList<>(sortedList);
        Collections.shuffle(list);
        SortUtils.sortNatural(list);
        Assert.assertEquals(sortedList, list);
    }

}
