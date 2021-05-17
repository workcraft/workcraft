package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class MpsatVerificationSettingsTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void mpsatVerificationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.mpsatVerification";

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getReplicateSelfloopPlaces()),
                framework.getConfigVar(prefix + ".replicateSelfloopPlaces", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getSolutionMode()),
                framework.getConfigVar(prefix + ".solutionMode", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getDebugReach()),
                framework.getConfigVar(prefix + ".debugReach", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getDebugCores()),
                framework.getConfigVar(prefix + ".debugCores", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getConformationReportStyle()),
                framework.getConfigVar(prefix + ".conformationReportStyle", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getLtl2tgbaCommand()),
                framework.getConfigVar(prefix + ".ltl2tgbaCommand", false));

        Assertions.assertEquals(Config.toString(MpsatVerificationSettings.getShowSpotInMenu()),
                framework.getConfigVar(prefix + ".showSpotInMenu", false));
    }

}
