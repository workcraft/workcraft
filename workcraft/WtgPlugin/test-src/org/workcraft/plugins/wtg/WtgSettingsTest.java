package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.dtd.DtdSettings;

public class WtgSettingsTest {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void dtdSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "DtdSettings";

        Assertions.assertEquals(Config.toString(DtdSettings.getVerticalSeparation()),
                framework.getConfigVar(prefix + ".verticalSeparation", false));

        Assertions.assertEquals(Config.toString(DtdSettings.getTransitionSeparation()),
                framework.getConfigVar(prefix + ".transitionSeparation", false));
    }

    @Test
    public void wtgSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "WtgSettings";

        Assertions.assertEquals(Config.toString(WtgSettings.getLowStateSuffix()),
                framework.getConfigVar(prefix + ".lowStateSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getHighStateSuffix()),
                framework.getConfigVar(prefix + ".highStateSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getStableStateSuffix()),
                framework.getConfigVar(prefix + ".stableStateSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getUnstableStateSuffix()),
                framework.getConfigVar(prefix + ".unstableStateSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getStabiliseEventSuffix()),
                framework.getConfigVar(prefix + ".stabiliseEventSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getDestabiliseEventSuffix()),
                framework.getConfigVar(prefix + ".destabiliseEventSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getEntryEventSuffix()),
                framework.getConfigVar(prefix + ".entryEventSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getExitEventSuffix()),
                framework.getConfigVar(prefix + ".exitEventSuffix", false));

        Assertions.assertEquals(Config.toString(WtgSettings.getInactivePlaceSuffix()),
                framework.getConfigVar(prefix + ".inactivePlaceSuffix", false));
    }

}
