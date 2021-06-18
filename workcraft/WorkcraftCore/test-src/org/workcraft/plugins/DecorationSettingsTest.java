package org.workcraft.plugins;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.builtin.settings.SelectionDecorationSettings;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;

class DecorationSettingsTest {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void selectionDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "SelectionDecorationSettings";

        Assertions.assertEquals(Config.toString(SelectionDecorationSettings.getHighlightingColor()),
                framework.getConfigVar(prefix + ".highlightingColor", false));

        Assertions.assertEquals(Config.toString(SelectionDecorationSettings.getSelectionColor()),
                framework.getConfigVar(prefix + ".selectionColor", false));

        Assertions.assertEquals(Config.toString(SelectionDecorationSettings.getShadingColor()),
                framework.getConfigVar(prefix + ".shadingColor", false));
    }

    @Test
    void simulationDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "SimulationDecorationSettings";

        Assertions.assertEquals(Config.toString(SimulationDecorationSettings.getExcitedComponentColor()),
                framework.getConfigVar(prefix + ".excitedComponentColor", false));

        Assertions.assertEquals(Config.toString(SimulationDecorationSettings.getSuggestedComponentColor()),
                framework.getConfigVar(prefix + ".suggestedComponentColor", false));
    }

    @Test
    void analysisDecorationSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "AnalysisDecorationSettings";

        Assertions.assertEquals(Config.toString(AnalysisDecorationSettings.getDontTouchColor()),
                framework.getConfigVar(prefix + ".dontTouchColor", false));

        Assertions.assertEquals(Config.toString(AnalysisDecorationSettings.getProblemColor()),
                framework.getConfigVar(prefix + ".problematicColor", false));

        Assertions.assertEquals(Config.toString(AnalysisDecorationSettings.getFixerColor()),
                framework.getConfigVar(prefix + ".problemFixerColor", false));

        Assertions.assertEquals(Config.toString(AnalysisDecorationSettings.getClearColor()),
                framework.getConfigVar(prefix + ".problemFreeColor", false));
    }

}
