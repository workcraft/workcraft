package org.workcraft.plugins.xmas;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.xmas.tools.JsonExport;
import org.workcraft.plugins.xmas.tools.PNetGen;
import org.workcraft.plugins.xmas.tools.StgGeneratorTool;
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

        pm.registerClass(Tool.class, JsonExport.class);
        pm.registerClass(Tool.class, PNetGen.class);
        pm.registerClass(Tool.class, SyncTool.class);
        pm.registerClass(Tool.class, VerConfTool.class);
        pm.registerClass(Tool.class, StgGeneratorTool.class);
        pm.registerClass(Tool.class, VerTool.class);
        pm.registerClass(Tool.class, VerAnalysis.class);
        pm.registerClass(Tool.class, VerQuery.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.xmas.XmasDescriptor\"/>");
    }

}
