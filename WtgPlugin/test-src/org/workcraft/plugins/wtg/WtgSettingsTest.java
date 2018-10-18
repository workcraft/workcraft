package org.workcraft.plugins.wtg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.dtd.DtdSettings;

public class WtgSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void dtdSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "DtdSettings";

        Assert.assertEquals(Config.toString(DtdSettings.getVerticalSeparation()),
                framework.getConfigVar(prefix + ".verticalSeparation", false));

        Assert.assertEquals(Config.toString(DtdSettings.getTransitionSeparation()),
                framework.getConfigVar(prefix + ".transitionSeparation", false));
    }

    @Test
    public void wtgSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "WtgSettings";

        Assert.assertEquals(Config.toString(WtgSettings.getLowStateSuffix()),
                framework.getConfigVar(prefix + ".lowStateSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getHighStateSuffix()),
                framework.getConfigVar(prefix + ".highStateSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getStableStateSuffix()),
                framework.getConfigVar(prefix + ".stableStateSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getUnstableStateSuffix()),
                framework.getConfigVar(prefix + ".unstableStateSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getStabiliseEventSuffix()),
                framework.getConfigVar(prefix + ".stabiliseEventSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getDestabiliseEventSuffix()),
                framework.getConfigVar(prefix + ".destabiliseEventSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getEntryEventSuffix()),
                framework.getConfigVar(prefix + ".entryEventSuffix", false));

        Assert.assertEquals(Config.toString(WtgSettings.getExitEventSuffix()),
                framework.getConfigVar(prefix + ".exitEventSuffix", false));
    }

}
