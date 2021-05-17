package org.workcraft.plugins.mpsat_synthesis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class MpsatSynthesisSettingsTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void mpsatSynthesisSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "Tools.mpsatSynthesis";

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getCommand()),
                framework.getConfigVar(prefix + ".command", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getReplicateSelfloopPlaces()),
                framework.getConfigVar(prefix + ".replicateSelfloopPlaces", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getArgs()),
                framework.getConfigVar(prefix + ".args", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getAdvancedMode()),
                framework.getConfigVar(prefix + ".advancedMode", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getPrintStdout()),
                framework.getConfigVar(prefix + ".printStdout", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getPrintStderr()),
                framework.getConfigVar(prefix + ".printStderr", false));

        Assertions.assertEquals(Config.toString(MpsatSynthesisSettings.getOpenSynthesisStg()),
                framework.getConfigVar(prefix + ".openSynthesisStg", false));
    }

}
