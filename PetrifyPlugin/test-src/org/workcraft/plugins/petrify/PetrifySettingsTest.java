package org.workcraft.plugins.petrify;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class PetrifySettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void petrifySettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.petrify";

        Assert.assertEquals(Config.toString(PetrifySettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getWriteLog()),
                framework.getConfigVar(prefix + ".writeLog", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getWriteStg()),
                framework.getConfigVar(prefix + ".writeStg", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getWriteEqn()),
                framework.getConfigVar(prefix + ".writeEqn", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(PetrifySettings.getOpenSynthesisStg()),
                framework.getConfigVar(prefix + ".openSynthesisStg", false));
    }

}
