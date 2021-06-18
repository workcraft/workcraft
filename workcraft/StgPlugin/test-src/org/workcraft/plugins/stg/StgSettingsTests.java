package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class StgSettingsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void stgSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "StgSettings";

        Assertions.assertEquals(Config.toString(StgSettings.getDensityMapLevelLimit()),
                framework.getConfigVar(prefix + ".densityMapLevelLimit", false));

        Assertions.assertEquals(Config.toString(StgSettings.getLowLevelSuffix()),
                framework.getConfigVar(prefix + ".lowLevelSuffix", false));

        Assertions.assertEquals(Config.toString(StgSettings.getHighLevelSuffix()),
                framework.getConfigVar(prefix + ".highLevelSuffix", false));

        Assertions.assertEquals(Config.toString(StgSettings.getGroupSignalConversion()),
                framework.getConfigVar(prefix + ".groupSignalConversion", false));

        Assertions.assertEquals(Config.toString(StgSettings.getShowTransitionInstance()),
                framework.getConfigVar(prefix + ".showTransitionInstance", false));

        Assertions.assertEquals(Config.toString(StgSettings.getMutexProtocol()),
                framework.getConfigVar(prefix + ".mutexProtocol", false));

        Assertions.assertEquals(Config.toString(StgSettings.getTransitionFontSize()),
                framework.getConfigVar(prefix + ".transitionFontSize", false));
    }

}
