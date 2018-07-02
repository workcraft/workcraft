package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.layout.DotLayoutCommand;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.RandomLayoutCommand;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.shared.*;
import org.workcraft.plugins.statistics.BasicStatisticsCommand;
import org.workcraft.plugins.transform.CopyLabelTransformationCommand;
import org.workcraft.plugins.transform.StraightenConnectionTransformationCommand;

public class BuiltinTools implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, CommonPluginSettings.class);
        pm.registerClass(Settings.class, CommonEditorSettings.class);
        pm.registerClass(Settings.class, CommonVisualSettings.class);
        pm.registerClass(Settings.class, CommonDecorationSettings.class);
        pm.registerClass(Settings.class, CommonCommentSettings.class);
        pm.registerClass(Settings.class, CommonDebugSettings.class);
        pm.registerClass(Settings.class, CommonLogSettings.class);
        pm.registerClass(Settings.class, CommonSignalSettings.class);
        pm.registerClass(Settings.class, CommonSatSettings.class);

        ScriptableCommandUtils.register(CopyLabelTransformationCommand.class, "transformModelCopyLabel",
                "transform the given 'work' by copying unique names of the selected (or all) nodes into their labels");
        ScriptableCommandUtils.register(StraightenConnectionTransformationCommand.class, "transformModelStraightenConnection",
                "transform the given 'work' by straightening selected (or all) arcs");

        ScriptableCommandUtils.register(DotLayoutCommand.class, "layoutModelDot",
                "position nodes and shape the arcs using of the model ''work'' using //Graphviz// backend");
        pm.registerClass(Settings.class, DotLayoutSettings.class);

        ScriptableCommandUtils.register(RandomLayoutCommand.class, "layoutModelRandom",
                "randomly position graph nodes of the model ''work'' and connect them by straight arcs");
        pm.registerClass(Settings.class, RandomLayoutSettings.class);

        ScriptableCommandUtils.register(BasicStatisticsCommand.class, "statModel",
                "node and arc count for the model 'work' (all model types are supported)");
    }

    @Override
    public String getDescription() {
        return "Built-in tools";
    }

}
