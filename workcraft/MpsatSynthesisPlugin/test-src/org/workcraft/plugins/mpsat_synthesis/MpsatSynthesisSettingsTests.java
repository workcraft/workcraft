package org.workcraft.plugins.mpsat_synthesis;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class MpsatSynthesisSettingsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void mpsatSynthesisSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.mpsatSynthesis";

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(MpsatSynthesisSettings.getOpenSynthesisStg()),
                framework.getConfigVar(prefix + ".openSynthesisStg", false));
    }

}
