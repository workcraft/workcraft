package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.layout.DotLayoutCommand;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.RandomLayoutCommand;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.shared.CommonCommentSettings;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonLogSettings;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.statistics.BasicStatisticsCommand;
import org.workcraft.plugins.transform.CopyLabelTransformationCommand;
import org.workcraft.plugins.transform.StraightenConnectionTransformationCommand;

public class BuiltinTools implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, CommonEditorSettings.class);
        pm.registerClass(Settings.class, CommonVisualSettings.class);
        pm.registerClass(Settings.class, CommonSimulationSettings.class);
        pm.registerClass(Settings.class, CommonCommentSettings.class);
        pm.registerClass(Settings.class, CommonDebugSettings.class);
        pm.registerClass(Settings.class, CommonLogSettings.class);
        pm.registerClass(Settings.class, CommonSignalSettings.class);
        pm.registerClass(Settings.class, CommonSatSettings.class);

        pm.registerClass(Settings.class, DotLayoutSettings.class);
        pm.registerClass(Settings.class, RandomLayoutSettings.class);

        pm.registerClass(Command.class, DotLayoutCommand.class);
        pm.registerClass(Command.class, RandomLayoutCommand.class);

        pm.registerClass(Command.class, CopyLabelTransformationCommand.class);
        pm.registerClass(Command.class, StraightenConnectionTransformationCommand.class);
        pm.registerClass(Command.class, BasicStatisticsCommand.class);
    }

    @Override
    public String getDescription() {
        return "Built-in tools";
    }

}
