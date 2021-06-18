package org.workcraft.plugins.pcomp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class PcompSettingsTest {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void pcompSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.pcomp";

        Assertions.assertEquals(Config.toString(PcompSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(PcompSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(PcompSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(PcompSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(PcompSettings.getSharedSignalMode()),
                framework.getConfigVar(prefix + ".sharedSignalMode", false));
    }

}
