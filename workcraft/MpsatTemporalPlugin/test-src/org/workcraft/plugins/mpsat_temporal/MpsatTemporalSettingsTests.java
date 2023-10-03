package org.workcraft.plugins.mpsat_temporal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class MpsatTemporalSettingsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void mpsatTemporalSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.mpsatTemporal";

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getThreadCount()),
                framework.getConfigVar(prefix + ".threadCount", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getLtl2tgbaCommand()),
                framework.getConfigVar(prefix + ".ltl2tgbaCommand", false));

        Assertions.assertEquals(Config.toString(MpsatTemporalSettings.getShowSpotInMenu()),
                framework.getConfigVar(prefix + ".showSpotInMenu", false));
    }

}
