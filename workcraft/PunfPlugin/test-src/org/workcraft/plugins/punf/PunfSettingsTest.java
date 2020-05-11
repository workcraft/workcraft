package org.workcraft.plugins.punf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class PunfSettingsTest {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void punfSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.punf";

        Assertions.assertEquals(Config.toString(PunfSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getUseMciCsc()),
                framework.getConfigVar(prefix + ".useMciCsc", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getLtl2tgbaCommand()),
                framework.getConfigVar(prefix + ".ltl2tgbaCommand", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getShowSpotInMenu()),
                framework.getConfigVar(prefix + ".showSpotInMenu", false));

        Assertions.assertEquals(Config.toString(PunfSettings.getVerboseSyntaxCheck()),
                framework.getConfigVar(prefix + ".verboseSyntaxCheck", false));
    }

}
