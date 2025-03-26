package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.types.Pair;

import java.util.*;

class SetUtilsTests {

    public static final HashSet<Integer> NUM_SET_1 = new HashSet<>(List.of(1, 2, 3, 4));
    public static final HashSet<Integer> NUM_SET_2 = new HashSet<>(List.of(2, 3, 4, 5));
    public static final HashSet<String> STRING_SET_1 = new HashSet<>(List.of("a", "bb", "ccc"));
    public static final HashSet<String> STRING_SET_2 = new HashSet<>(List.of("bb", "ccc", "dddd"));

    @Test
    void intersectionTest() {
        Assertions.assertEquals(NUM_SET_1,
                SetUtils.intersection(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(new HashSet<>(List.of(2, 3, 4)),
                SetUtils.intersection(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(List.of("bb", "ccc")),
                SetUtils.intersection(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void unionTest() {
        Assertions.assertEquals(NUM_SET_1,
                SetUtils.union(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(new HashSet<>(List.of(1, 2, 3, 4, 5)),
                SetUtils.union(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(List.of("a", "bb", "ccc", "dddd")),
                SetUtils.union(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void differenceTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.difference(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(new HashSet<>(List.of(1)),
                SetUtils.difference(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(List.of("a")),
                SetUtils.difference(STRING_SET_1, STRING_SET_2));

        Assertions.assertEquals(new HashSet<>(List.of("dddd")),
                SetUtils.difference(STRING_SET_2, STRING_SET_1));
    }

    @Test
    void symmetricDifferenceTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.symmetricDifference(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(new HashSet<>(List.of(1, 5)),
                SetUtils.symmetricDifference(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(new HashSet<>(List.of("a", "dddd")),
                SetUtils.symmetricDifference(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void isFirstSmallerTest() {
        Assertions.assertFalse(SetUtils.isFirstSmaller(NUM_SET_1, NUM_SET_2, false));
        Assertions.assertFalse(SetUtils.isFirstSmaller(STRING_SET_1, STRING_SET_2, true));
        Assertions.assertTrue(SetUtils.isFirstSmaller(new HashSet<>(List.of("a")),
                STRING_SET_1, false));
    }

    @Test
    void convertArraysToSetsTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.convertArraysToSets(new Object[][]{}));

        Set<String> abSet = new HashSet<>(List.of("a", "b"));
        Set<String> abcSet = new HashSet<>(List.of("a", "b", "c"));
        Assertions.assertEquals(new HashSet<>(List.of(abSet, abcSet)),
                SetUtils.convertArraysToSets(new String[][]{{"a", "b"}, {"b", "c", "a"}}));

        Assertions.assertEquals(new HashSet<>(List.of(abSet, abcSet)),
                SetUtils.convertArraysToSets(new String[][]{{"a", "b", "a", "b"}, {"b", "a", "c", "a"}}));

    }

    @Test
    void partitionTest() {
        Assertions.assertEquals(Pair.of(Collections.emptySet(), Collections.emptySet()),
                SetUtils.partition(new HashSet<>(), item -> true));

        Set<Integer> oddSet = new HashSet<>(List.of(1, 3, 5));
        Set<Integer> evenSet = new HashSet<>(List.of(2, 4, 6));
        List<Integer> ints = new ArrayList<>();
        ints.addAll(oddSet);
        ints.addAll(evenSet);
        Assertions.assertEquals(Pair.of(evenSet, oddSet),
                SetUtils.partition(ints, item -> item % 2 == 0));

        Set<String> shortSet = new HashSet<>(List.of("", "a", "ab"));
        Set<String> longSet = new HashSet<>(List.of("qwerty", "asdfgh", "zxcvbn"));
        List<String> strings = new ArrayList<>();
        strings.addAll(shortSet);
        strings.addAll(longSet);
        Assertions.assertEquals(Pair.of(shortSet, longSet),
                SetUtils.partition(strings, item -> item.length() < 3));
    }

    @Test
    void duplicatesTest() {
        Assertions.assertEquals(Collections.emptySet(),
                SetUtils.duplicates(Collections.emptyList()));

        Assertions.assertEquals(Collections.emptySet(),
                SetUtils.duplicates(List.of(1, 2, 3, 4, 5)));

        Assertions.assertEquals(new HashSet<>(List.of(1, 2)),
                SetUtils.duplicates(List.of(1, 2, 3, 1, 4, 2, 5, 2)));

        Assertions.assertEquals(new HashSet<>(List.of("a", "b")),
                SetUtils.duplicates(List.of("a", "b", "a", "c", "b", "d", "b")));
    }

}
