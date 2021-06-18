package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogUtilsTests {

    @Test
    void prefixTest() {
        Assertions.assertNull(LogUtils.getTextWithoutPrefix(null));
        String text = "This is text without prefix";
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix(text));
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix("[INFO] " + text));
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix("[WARNING] " + text));
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix("[ERROR] " + text));
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix("[STDOUT] " + text));
        Assertions.assertEquals(text, LogUtils.getTextWithoutPrefix("[STDERR] " + text));
        Assertions.assertEquals("[UNDEFINED] " + text, LogUtils.getTextWithoutPrefix("[UNDEFINED] " + text));
    }

}
