package org.workcraft.plugins;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.DotLayoutSettings;
import org.workcraft.plugins.builtin.settings.RandomLayoutSettings;

class LayoutSettingsTest {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void dotLayoutSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "DotLayoutSettings";

        Assertions.assertEquals(Config.toString(DotLayoutSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(DotLayoutSettings.getRankdir()),
                framework.getConfigVar(prefix + ".rankdir", false));

        Assertions.assertEquals(Config.toString(DotLayoutSettings.getNodesep()),
                framework.getConfigVar(prefix + ".sepNodesep", false));

        Assertions.assertEquals(Config.toString(DotLayoutSettings.getRanksep()),
                framework.getConfigVar(prefix + ".sepRanksep", false));

        Assertions.assertEquals(Config.toString(DotLayoutSettings.getImportConnectionsShape()),
                framework.getConfigVar(prefix + ".importConnectionsShape", false));
    }

    @Test
    void randomLayoutSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "RandomLayoutSettings";

        Assertions.assertEquals(Config.toString(RandomLayoutSettings.getStartX()),
                framework.getConfigVar(prefix + ".startX", false));

        Assertions.assertEquals(Config.toString(RandomLayoutSettings.getStartY()),
                framework.getConfigVar(prefix + ".startY", false));

        Assertions.assertEquals(Config.toString(RandomLayoutSettings.getRangeX()),
                framework.getConfigVar(prefix + ".rangeX", false));

        Assertions.assertEquals(Config.toString(RandomLayoutSettings.getRangeY()),
                framework.getConfigVar(prefix + ".rangeY", false));
    }

}
