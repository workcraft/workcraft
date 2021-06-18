package org.workcraft.plugins.petrify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class PetrifySettingsTest {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void petrifySettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.petrify";

        Assertions.assertEquals(Config.toString(PetrifySettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getWriteLog()),
                framework.getConfigVar(prefix + ".writeLog", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getWriteEqn()),
                framework.getConfigVar(prefix + ".writeEqn", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(PetrifySettings.getOpenSynthesisStg()),
                framework.getConfigVar(prefix + ".openSynthesisStg", false));
    }

}
