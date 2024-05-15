package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ListUtilsTests {

    @Test
    void zipTest() {
        Assertions.assertEquals(List.of(),
                ListUtils.zip(List.of(), List.of(), 1));

        Assertions.assertEquals(List.of(1),
                ListUtils.zip(List.of(1), List.of(), 2));

        Assertions.assertEquals(List.of(2, 3, 4),
                ListUtils.zip(List.of(), List.of(2, 3, 4), 2));

        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5),
                ListUtils.zip(Arrays.asList(1, 3, 5), Arrays.asList(2, 4), 1));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4"),
                        Arrays.asList("b1", "b2", "b3", "b4"), 2));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4", "b5", "b6", "b7"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4"),
                        Arrays.asList("b1", "b2", "b3", "b4", "b5", "b6", "b7"), 2));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4", "a5", "a6", "a7"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4", "a5", "a6", "a7"),
                        Arrays.asList("b1", "b2", "b3", "b4"), 2));
    }

}
