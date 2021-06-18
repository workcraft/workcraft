package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

class SetUtilsTests {

    public static final HashSet<Integer> NUM_SET_1 = new HashSet<>(Arrays.asList(1, 2, 3, 4));
    public static final HashSet<Integer> NUM_SET_2 = new HashSet<>(Arrays.asList(2, 3, 4, 5));
    public static final HashSet<String> STRING_SET_1 = new HashSet<>(Arrays.asList("a", "bb", "ccc"));
    public static final HashSet<String> STRING_SET_2 = new HashSet<>(Arrays.asList("bb", "ccc", "dddd"));

    @Test
    void intersectionTest() {
        Assertions.assertEquals(new HashSet<>(Arrays.asList(2, 3, 4)),
                SetUtils.intersection(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("bb", "ccc")),
                SetUtils.intersection(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void unionTest() {
        Assertions.assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
                SetUtils.union(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("a", "bb", "ccc", "dddd")),
                SetUtils.union(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void symmetricDifferenceTest() {
        Assertions.assertEquals(new HashSet<>(Arrays.asList(1, 5)),
                SetUtils.symmetricDifference(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("a", "dddd")),
                SetUtils.symmetricDifference(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void isFirstSmallerTest() {
        Assertions.assertFalse(SetUtils.isFirstSmaller(NUM_SET_1, NUM_SET_2, false));
        Assertions.assertFalse(SetUtils.isFirstSmaller(STRING_SET_1, STRING_SET_2, true));
        Assertions.assertTrue(SetUtils.isFirstSmaller(new HashSet<>(Arrays.asList("a")),
                STRING_SET_1, false));
    }

}
