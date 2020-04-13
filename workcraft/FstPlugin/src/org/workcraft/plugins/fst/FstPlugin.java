package org.workcraft.plugins.fst;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.fst.commands.FsmToFstConversionCommand;
import org.workcraft.plugins.fst.commands.FstToFsmConversionCommand;
import org.workcraft.plugins.fst.commands.FstToStgConversionCommand;
import org.workcraft.plugins.fst.interop.SgExporter;
import org.workcraft.plugins.fst.interop.SgImporter;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class FstPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Finite State Transducer plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(FstDescriptor.class);

        pm.registerExporter(SgExporter.class);
        pm.registerImporter(SgImporter.class);

        ScriptableCommandUtils.registerCommand(FstToStgConversionCommand.class, "convertFstToStg",
                "convert the FST 'work' into a new STG work");
        ScriptableCommandUtils.registerCommand(FsmToFstConversionCommand.class, "convertFsmToFst",
                "convert the FSM 'work' into a new FST work");
        ScriptableCommandUtils.registerCommand(FstToFsmConversionCommand.class, "convertFstToFsm",
                "convert the FST 'work' into a new FSM work");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.fst.FstModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.fst.FstDescriptor\"/>");
    }

}
