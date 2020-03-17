package org.workcraft.plugins;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.builtin.settings.SelectionDecorationSettings;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;

public class DecorationSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void selectionDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "SelectionDecorationSettings";

        Assert.assertEquals(Config.toString(SelectionDecorationSettings.getHighlightingColor()),
                framework.getConfigVar(prefix + ".highlightingColor", false));

        Assert.assertEquals(Config.toString(SelectionDecorationSettings.getSelectionColor()),
                framework.getConfigVar(prefix + ".selectionColor", false));

        Assert.assertEquals(Config.toString(SelectionDecorationSettings.getShadingColor()),
                framework.getConfigVar(prefix + ".shadingColor", false));
    }

    @Test
    public void simulationDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "SimulationDecorationSettings";

        Assert.assertEquals(Config.toString(SimulationDecorationSettings.getExcitedComponentColor()),
                framework.getConfigVar(prefix + ".excitedComponentColor", false));

        Assert.assertEquals(Config.toString(SimulationDecorationSettings.getSuggestedComponentColor()),
                framework.getConfigVar(prefix + ".suggestedComponentColor", false));
    }

    @Test
    public void analysisDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "AnalysisDecorationSettings";

        Assert.assertEquals(Config.toString(AnalysisDecorationSettings.getDontTouchColor()),
                framework.getConfigVar(prefix + ".dontTouchColor", false));

        Assert.assertEquals(Config.toString(AnalysisDecorationSettings.getProblemColor()),
                framework.getConfigVar(prefix + ".problematicColor", false));

        Assert.assertEquals(Config.toString(AnalysisDecorationSettings.getFixerColor()),
                framework.getConfigVar(prefix + ".problemFixerColor", false));

        Assert.assertEquals(Config.toString(AnalysisDecorationSettings.getClearColor()),
                framework.getConfigVar(prefix + ".problemFreeColor", false));
    }

}
