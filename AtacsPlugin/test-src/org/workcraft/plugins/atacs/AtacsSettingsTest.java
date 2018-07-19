package org.workcraft.plugins.atacs;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class AtacsSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void atacsSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.atacs";

        Assert.assertEquals(Config.toString(AtacsSettings.getShowInMenu()),
                framework.getConfigVar(prefix + ".showInMenu", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(AtacsSettings.getOpenSynthesisResult()),
                framework.getConfigVar(prefix + ".openSynthesisResult", false));
    }

}
