package org.workcraft.plugins.punf;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class PunfSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void petrifySettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.punf";

        Assert.assertEquals(Config.toString(PunfSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(PunfSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(PunfSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(PunfSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(PunfSettings.getUseMciCsc()),
                framework.getConfigVar(prefix + ".useMciCsc", false));

        Assert.assertEquals(Config.toString(PunfSettings.getLtl2tgbaCommand()),
                framework.getConfigVar(prefix + ".ltl2tgbaCommand", false));

        Assert.assertEquals(Config.toString(PunfSettings.getShowSpotInMenu()),
                framework.getConfigVar(prefix + ".showSpotInMenu", false));
    }

}
