package org.workcraft.plugins.cpog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class CpogSettingsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void cpogSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CpogSettings";

        Assertions.assertEquals(Config.toString(CpogSettings.getScencoCommand()),
                framework.getConfigVar(prefix + ".scencoCommand", false));

        Assertions.assertEquals(Config.toString(CpogSettings.getEspressoCommand()),
                framework.getConfigVar(prefix + ".espressoCommand", false));

        Assertions.assertEquals(Config.toString(CpogSettings.getAbcTool()),
                framework.getConfigVar(prefix + ".abcTool", false));

        Assertions.assertEquals(Config.toString(CpogSettings.getSatSolver()),
                framework.getConfigVar(prefix + ".satSolver", false));

        Assertions.assertEquals(Config.toString(CpogSettings.getClaspCommand()),
                framework.getConfigVar(prefix + ".claspCommand", false));

        Assertions.assertEquals(Config.toString(CpogSettings.getMinisatCommand()),
                framework.getConfigVar(prefix + ".minisatCommand", false));
    }

}
