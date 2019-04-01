package org.workcraft.plugins.stg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class StgSettingsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void stgSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "StgSettings";

        Assert.assertEquals(Config.toString(StgSettings.getDensityMapLevelLimit()),
                framework.getConfigVar(prefix + ".densityMapLevelLimit", false));

        Assert.assertEquals(Config.toString(StgSettings.getLowLevelSuffix()),
                framework.getConfigVar(prefix + ".lowLevelSuffix", false));

        Assert.assertEquals(Config.toString(StgSettings.getHighLevelSuffix()),
                framework.getConfigVar(prefix + ".highLevelSuffix", false));

        Assert.assertEquals(Config.toString(StgSettings.getGroupSignalConversion()),
                framework.getConfigVar(prefix + ".groupSignalConversion", false));

        Assert.assertEquals(Config.toString(StgSettings.getShowTransitionInstance()),
                framework.getConfigVar(prefix + ".showTransitionInstance", false));

        Assert.assertEquals(Config.toString(StgSettings.getMutexProtocol()),
                framework.getConfigVar(prefix + ".mutexProtocol", false));
    }

}
