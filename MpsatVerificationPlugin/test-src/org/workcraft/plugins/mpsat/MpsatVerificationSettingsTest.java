package org.workcraft.plugins.mpsat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class MpsatVerificationSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void mpsatVerificationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.mpsatVerification";

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getSolutionMode()),
                framework.getConfigVar(prefix + ".solutionMode", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getDebugReach()),
                framework.getConfigVar(prefix + ".debugReach", false));

        Assert.assertEquals(Config.toString(MpsatVerificationSettings.getDebugCores()),
                framework.getConfigVar(prefix + ".debugCores", false));
    }

}
