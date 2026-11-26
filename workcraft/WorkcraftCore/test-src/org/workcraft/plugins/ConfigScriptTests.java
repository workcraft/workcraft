package org.workcraft.plugins;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;

import java.io.File;
import java.io.IOException;
import java.util.List;

class ConfigScriptTests {

    @BeforeAll
    static void init() throws IOException {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
        framework.execJavaScriptResource("scripts/core-config.js");
    }

    @AfterEach
    void restore() {
        final Framework framework = Framework.getInstance();
        framework.setConfigFiles(null, List.of());
        framework.resetConfig();
    }

    @Test
    void testSaveAndLoadConfig(@TempDir File dir) {
        final Framework framework = Framework.getInstance();
        File mainFile = new File(dir, "config.xml");
        File additionalFile = new File(dir, "config-additional.xml");
        Config additionalConfig = new Config();
        additionalConfig.setInt("CommonEditorSettings.flashInterval", 1234);
        additionalConfig.save(additionalFile);
        framework.setConfigFiles(mainFile, List.of(additionalFile));

        EditorCommonSettings.setRecentCount(7);
        framework.execJavaScript("saveConfig();");
        Assertions.assertTrue(mainFile.exists());

        EditorCommonSettings.setRecentCount(3);
        framework.execJavaScript("loadConfig();");
        Assertions.assertEquals(7, EditorCommonSettings.getRecentCount());
        Assertions.assertEquals(1234, EditorCommonSettings.getFlashInterval());
    }

    @Test
    void testSetConfigVarKeepsOtherSettings() {
        final Framework framework = Framework.getInstance();
        // Changed directly, as the settings dialog does
        EditorCommonSettings.setRecentCount(5);
        framework.execJavaScript("setConfigVar('CommonEditorSettings.flashInterval', '777');");
        Assertions.assertEquals(777, EditorCommonSettings.getFlashInterval());
        Assertions.assertEquals(5, EditorCommonSettings.getRecentCount());
    }

}
