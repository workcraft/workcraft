package org.workcraft.plugins.cpog;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class CpogSettingsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void cpogSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CpogSettings";

        Assert.assertEquals(Config.toString(CpogSettings.getScencoCommand()),
                framework.getConfigVar(prefix + ".scencoCommand", false));

        Assert.assertEquals(Config.toString(CpogSettings.getEspressoCommand()),
                framework.getConfigVar(prefix + ".espressoCommand", false));

        Assert.assertEquals(Config.toString(CpogSettings.getAbcTool()),
                framework.getConfigVar(prefix + ".abcTool", false));

        Assert.assertEquals(Config.toString(CpogSettings.getSatSolver()),
                framework.getConfigVar(prefix + ".satSolver", false));

        Assert.assertEquals(Config.toString(CpogSettings.getClaspCommand()),
                framework.getConfigVar(prefix + ".claspCommand", false));

        Assert.assertEquals(Config.toString(CpogSettings.getMinisatCommand()),
                framework.getConfigVar(prefix + ".minisatCommand", false));
    }

}
