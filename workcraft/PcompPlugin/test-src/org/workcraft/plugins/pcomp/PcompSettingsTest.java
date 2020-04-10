package org.workcraft.plugins.pcomp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class PcompSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void pcompSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.pcomp";

        Assert.assertEquals(Config.toString(PcompSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(PcompSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(PcompSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(PcompSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(PcompSettings.getSharedSignalMode()),
                framework.getConfigVar(prefix + ".sharedSignalMode", false));
    }

}
