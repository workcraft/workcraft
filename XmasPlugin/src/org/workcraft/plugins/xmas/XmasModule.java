package org.workcraft.plugins.xmas;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.commands.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.xmas.commands.JsonExportCommand;
import org.workcraft.plugins.xmas.commands.XmasConfigureCommand;
import org.workcraft.plugins.xmas.commands.XmasPNetGenCommand;
import org.workcraft.plugins.xmas.commands.XmasSyncCommand;
import org.workcraft.plugins.xmas.commands.XmasToStgConversionCommand;
import org.workcraft.plugins.xmas.tools.XmasAnalysisTool;
import org.workcraft.plugins.xmas.tools.XmasQueryTool;
import org.workcraft.plugins.xmas.tools.XmasVerificationTool;

public class XmasModule implements Module {

    @Override
    public String getDescription() {
        return "xMAS Circuit";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, XmasDescriptor.class);
        pm.registerClass(Settings.class, XmasSettings.class);

        pm.registerClass(Command.class, JsonExportCommand.class);
        pm.registerClass(Command.class, XmasPNetGenCommand.class);
        pm.registerClass(Command.class, XmasSyncCommand.class);
        pm.registerClass(Command.class, XmasConfigureCommand.class);
        pm.registerClass(Command.class, XmasToStgConversionCommand.class);
        pm.registerClass(Command.class, XmasVerificationTool.class);
        pm.registerClass(Command.class, XmasAnalysisTool.class);
        pm.registerClass(Command.class, XmasQueryTool.class);
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
