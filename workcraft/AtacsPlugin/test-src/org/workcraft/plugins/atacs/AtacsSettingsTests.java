package org.workcraft.plugins.atacs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class AtacsSettingsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void atacsSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.atacs";

        Assertions.assertEquals(Config.toString(AtacsSettings.getShowInMenu()),
                framework.getConfigVar(prefix + ".showInMenu", false));

        Assertions.assertEquals(Config.toString(AtacsSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(AtacsSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(AtacsSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assertions.assertEquals(Config.toString(AtacsSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(AtacsSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));
    }

}
