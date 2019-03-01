package org.workcraft.plugins.xmas;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.plugins.xmas.commands.*;
import org.workcraft.plugins.xmas.tools.XmasAnalysisTool;
import org.workcraft.plugins.xmas.tools.XmasQueryTool;
import org.workcraft.plugins.xmas.tools.XmasVerificationTool;

@SuppressWarnings("unused")
public class XmasPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "xMAS Circuit plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(XmasDescriptor.class);
        pm.registerSettings(XmasSettings.class);

        pm.registerCommand(JsonExportCommand.class);
        pm.registerCommand(XmasPNetGenCommand.class);
        pm.registerCommand(XmasSyncCommand.class);
        pm.registerCommand(XmasConfigureCommand.class);
        pm.registerCommand(XmasToStgConversionCommand.class);
        pm.registerCommand(XmasVerificationTool.class);
        pm.registerCommand(XmasAnalysisTool.class);
        pm.registerCommand(XmasQueryTool.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasDescriptor\"/>");
    }

}
