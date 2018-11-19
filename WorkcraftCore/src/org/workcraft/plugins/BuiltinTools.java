package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.plugins.layout.DotLayoutCommand;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.RandomLayoutCommand;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.shared.*;
import org.workcraft.plugins.statistics.BasicStatisticsCommand;
import org.workcraft.plugins.transform.AnonymiseTransformationCommand;
import org.workcraft.plugins.transform.StraightenConnectionTransformationCommand;

public class BuiltinTools implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerSettings(CommonFavoriteSettings.class);
        pm.registerSettings(CommonEditorSettings.class);
        pm.registerSettings(CommonVisualSettings.class);
        pm.registerSettings(CommonDecorationSettings.class);
        pm.registerSettings(CommonCommentSettings.class);
        pm.registerSettings(CommonDebugSettings.class);
        pm.registerSettings(CommonLogSettings.class);
        pm.registerSettings(CommonSignalSettings.class);
        pm.registerSettings(CommonSatSettings.class);

        ScriptableCommandUtils.register(AnonymiseTransformationCommand.class, "transformModelAnonymise",
                "transform the given 'work' by anonymising selected (or all) nodes");

        ScriptableCommandUtils.register(StraightenConnectionTransformationCommand.class, "transformModelStraightenConnection",
                "transform the given 'work' by straightening selected (or all) arcs");

        ScriptableCommandUtils.register(DotLayoutCommand.class, "layoutModelDot",
                "position nodes and shape the arcs using of the model ''work'' using //Graphviz// backend");
        pm.registerSettings(DotLayoutSettings.class);

        ScriptableCommandUtils.register(RandomLayoutCommand.class, "layoutModelRandom",
                "randomly position graph nodes of the model ''work'' and connect them by straight arcs");
        pm.registerSettings(RandomLayoutSettings.class);

        ScriptableCommandUtils.register(BasicStatisticsCommand.class, "statModel",
                "node and arc count for the model 'work' (all model types are supported)");
    }

    @Override
    public String getDescription() {
        return "Built-in tools";
    }

}
