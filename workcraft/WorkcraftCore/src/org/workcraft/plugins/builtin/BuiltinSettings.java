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
        pm.registerSettings(FavoriteCommonSettings.class);
        pm.registerSettings(EditorCommonSettings.class);
        pm.registerSettings(VisualCommonSettings.class);
        pm.registerSettings(CommentCommonSettings.class);
        pm.registerSettings(DebugCommonSettings.class);
        pm.registerSettings(LogCommonSettings.class);
        pm.registerSettings(SignalCommonSettings.class);
        // Decoration settings
        pm.registerSettings(SelectionDecorationSettings.class);
        pm.registerSettings(SimulationDecorationSettings.class);
        pm.registerSettings(AnalysisDecorationSettings.class);
        // Layout settings
        pm.registerSettings(DotLayoutSettings.class);
        pm.registerSettings(RandomLayoutSettings.class);
    }

}
