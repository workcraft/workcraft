package org.workcraft.plugins.builtin;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.settings.*;

@SuppressWarnings("unused")
public class BuiltinSettings implements Plugin {

    @Override
    public String getDescription() {
        return "Built-in settings";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        // Common settings
        pm.registerSettings(CommonFavoriteSettings.class);
        pm.registerSettings(CommonEditorSettings.class);
        pm.registerSettings(CommonVisualSettings.class);
        pm.registerSettings(CommonCommentSettings.class);
        pm.registerSettings(CommonDebugSettings.class);
        pm.registerSettings(CommonLogSettings.class);
        pm.registerSettings(CommonSignalSettings.class);
        // Decoration settings
        pm.registerSettings(SelectionDecorationSettings.class);
        pm.registerSettings(SimulationDecorationSettings.class);
        pm.registerSettings(AnalysisDecorationSettings.class);
        // Layout settings
        pm.registerSettings(DotLayoutSettings.class);
        pm.registerSettings(RandomLayoutSettings.class);
    }

}
