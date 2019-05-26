package org.workcraft.plugins;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.DotLayoutSettings;
import org.workcraft.plugins.builtin.settings.RandomLayoutSettings;

public class LayoutSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void dotLayoutSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "DotLayoutSettings";

        Assert.assertEquals(Config.toString(DotLayoutSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(DotLayoutSettings.getRankdir()),
                framework.getConfigVar(prefix + ".rankdir", false));

        Assert.assertEquals(Config.toString(DotLayoutSettings.getNodesep()),
                framework.getConfigVar(prefix + ".sepNodesep", false));

        Assert.assertEquals(Config.toString(DotLayoutSettings.getRanksep()),
                framework.getConfigVar(prefix + ".sepRanksep", false));

        Assert.assertEquals(Config.toString(DotLayoutSettings.getImportConnectionsShape()),
                framework.getConfigVar(prefix + ".importConnectionsShape", false));
    }

    @Test
    public void randomLayoutSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "RandomLayoutSettings";

        Assert.assertEquals(Config.toString(RandomLayoutSettings.getStartX()),
                framework.getConfigVar(prefix + ".startX", false));

        Assert.assertEquals(Config.toString(RandomLayoutSettings.getStartY()),
                framework.getConfigVar(prefix + ".startY", false));

        Assert.assertEquals(Config.toString(RandomLayoutSettings.getRangeX()),
                framework.getConfigVar(prefix + ".rangeX", false));

        Assert.assertEquals(Config.toString(RandomLayoutSettings.getRangeY()),
                framework.getConfigVar(prefix + ".rangeY", false));
    }

}
