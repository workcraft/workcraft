package org.workcraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

class OptionsTest {

    @Test
    void testEmptyOptions() {
        testOptions(new Options(Collections.emptyList()),
                Collections.emptyList(),
                null, null, null, null, null,
                false, false, false, false, false);
    }

    @Test
    void testPartialOptions() {
        testOptions(new Options(Arrays.asList("aaa", "bbb", "-dir:abc/def", "-exec:script.js", "-port:12345",
                        "-nogui", "-noconfig-save", "-help", "-version")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                null, null, "script.js", 12345,
                true, false, true, true, true);
    }

    @Test
    void testAllOptions() {
        testOptions(new Options(Arrays.asList("aaa", "bbb", "-dir:abc/def",
                        "-config:config-local.xml", "-config-add:config-add.xml", "-exec:script.js", "-port:12345",
                        "-nogui", "-noconfig", "-help", "-version")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                "config-local.xml", "config-add.xml", "script.js", 12345,
                true, true, true, true, true);
    }

    @Test
    void testWrongOptions() {
        testOptions(new Options(new String[] {"-skip", "aaa", "-skip", "bbb", ""}),
                Arrays.asList("aaa", "bbb"), null,
                null, null, null, null,
                false, false, false, false, false);
    }

    @Test
    void testRepeatedOptions() {
        testOptions(new Options(Arrays.asList("-dir:123", "-exec:\"oneliner\"", "-port:0",
                "aaa", "bbb", "-dir:abc/def", "-exec:script.js", "-port:12345")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                null, null, "script.js", 12345,
                false, false, false, false, false);
    }

    @Test
    void testHelpMessageOptions() {
        String helpMessage = Options.getHelpMessage();
        Assertions.assertNotNull(helpMessage);
        Assertions.assertFalse(helpMessage.isEmpty());
    }

    private void testOptions(Options options, Collection<String> paths, File directory,
            String config, String configAdditional, String script, Integer port,
            boolean noGuiFlag, boolean noConfigLoadFlag, boolean noConfigSaveFlag,
            boolean helpFlag, boolean versionFlag) {

        Assertions.assertArrayEquals(paths.toArray(), options.getPaths().toArray());
        Assertions.assertEquals(directory, options.getDirectory());
        Assertions.assertEquals(config, options.getConfig());
        Assertions.assertEquals(configAdditional, options.getConfigAddition());
        Assertions.assertEquals(script, options.getScript());
        Assertions.assertEquals(port, options.getPort());
        Assertions.assertEquals(noGuiFlag, options.hasNoGuiFlag());
        Assertions.assertEquals(noConfigLoadFlag, options.hasNoConfigLoadFlag());
        Assertions.assertEquals(noConfigSaveFlag, options.hasNoConfigSaveFlag());
        Assertions.assertEquals(helpFlag, options.hasHelpFlag());
        Assertions.assertEquals(versionFlag, options.hasVersionFlag());
    }

}
