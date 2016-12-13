package org.workcraft.plugins.xmas;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.xmas.tools.JsonExportCommand;
import org.workcraft.plugins.xmas.tools.PNetGen;
import org.workcraft.plugins.xmas.tools.XmasToStgConversionCommand;
import org.workcraft.plugins.xmas.tools.SyncTool;
import org.workcraft.plugins.xmas.tools.VerAnalysis;
import org.workcraft.plugins.xmas.tools.VerConfTool;
import org.workcraft.plugins.xmas.tools.VerQuery;
import org.workcraft.plugins.xmas.tools.VerTool;

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
        pm.registerClass(Command.class, PNetGen.class);
        pm.registerClass(Command.class, SyncTool.class);
        pm.registerClass(Command.class, VerConfTool.class);
        pm.registerClass(Command.class, XmasToStgConversionCommand.class);
        pm.registerClass(Command.class, VerTool.class);
        pm.registerClass(Command.class, VerAnalysis.class);
        pm.registerClass(Command.class, VerQuery.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasDescriptor\"/>");
    }

}
