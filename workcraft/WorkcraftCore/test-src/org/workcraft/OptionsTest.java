package org.workcraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class OptionsTest {

    @Test
    void testEmptyOptions() {
        testOptions(new Options(Collections.emptyList()),
                Collections.emptyList(), null,
                null, Collections.emptyList(), null,
                null, false, false, false,
                false, false, Collections.emptyList());
    }

    @Test
    void testPartialOptions() {
        testOptions(new Options(Arrays.asList("aaa", "bbb", "-dir:abc/def", "-exec:script.js", "-port:12345",
                        "-nogui", "-noconfig-save", "-help", "-version")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                null, Collections.emptyList(), "script.js",
                12345, true, false, true,
                true, true, Collections.emptyList());
    }

    @Test
    void testAllOptions() {
        testOptions(new Options(Arrays.asList("aaa", "bbb", "-dir:abc/def",
                        "-config:config-local.xml", "-config-add:config-add_1.xml",  "-config-add:config-add_2.xml",
                        "-exec:script.js", "-port:12345", "-nogui", "-noconfig", "-help", "-version")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                "config-local.xml", List.of("config-add_1.xml", "config-add_2.xml"), "script.js",
                12345, true, true, true,
                true, true, Collections.emptyList());
    }

    @Test
    void testWrongOptions() {
        testOptions(new Options(new String[] {"-skip", "aaa", "-skip", "bbb", "-error", ""}),
                Arrays.asList("aaa", "bbb"), null,
                null, Collections.emptyList(), null,
                null, false, false, false,
                false, false, Arrays.asList("-skip", "-skip", "-error"));
    }

    @Test
    void testRepeatedOptions() {
        testOptions(new Options(Arrays.asList("-dir:123", "-exec:\"oneliner\"", "-port:0",
                        "aaa", "bbb", "-dir:abc/def", "-exec:script.js", "-port:12345")),
                Arrays.asList("aaa", "bbb"), new File("abc/def"),
                null, Collections.emptyList(), "script.js",
                12345, false, false, false,
                false, false, Collections.emptyList());
    }

    @Test
    void testHelpMessageOptions() {
        String helpMessage = Options.getHelpMessage();
        Assertions.assertNotNull(helpMessage);
        Assertions.assertFalse(helpMessage.isEmpty());
    }

    private void testOptions(Options options, Collection<String> paths, File directory,
            String config, List<String> configAdditions, String script, Integer port,
            boolean noGuiFlag, boolean noConfigLoadFlag, boolean noConfigSaveFlag,
            boolean helpFlag, boolean versionFlag, List<String> unsupportedFlags) {

        Assertions.assertArrayEquals(paths.toArray(), options.getPaths().toArray());
        Assertions.assertEquals(directory, options.getDirectory());
        Assertions.assertEquals(config, options.getConfig());
        Assertions.assertEquals(configAdditions, options.getConfigAdditions());
        Assertions.assertEquals(script, options.getScript());
        Assertions.assertEquals(port, options.getPort());
        Assertions.assertEquals(noGuiFlag, options.hasNoGuiFlag());
        Assertions.assertEquals(noConfigLoadFlag, options.hasNoConfigLoadFlag());
        Assertions.assertEquals(noConfigSaveFlag, options.hasNoConfigSaveFlag());
        Assertions.assertEquals(helpFlag, options.hasHelpFlag());
        Assertions.assertEquals(versionFlag, options.hasVersionFlag());
        Assertions.assertEquals(unsupportedFlags, options.getUnsupportedFlags());
    }

}
