package org.workcraft.plugins.dfs;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.dfs.commands.*;
import org.workcraft.plugins.dfs.interop.VerilogExporter;

@SuppressWarnings("unused")
public class DfsPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Dataflow Structure plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(DfsDescriptor.class);
        pm.registerExporter(VerilogExporter.class);
        pm.registerSettings(DfsSettings.class);

        ScriptableCommandUtils.registerCommand(DfsToStgConversionCommand.class, "convertDfsToStg",
                "convert the given DFS 'work' into a new STG work");

        ScriptableCommandUtils.registerCommand(DeadlockFreenessVerificationCommand.class, "checkDfsDeadlockFreeness",
                "check the DFS 'work' for deadlock freeness");
        ScriptableCommandUtils.registerCommand(OutputPersisitencyVerificationCommand.class, "checkDfsOutputPersistency",
                "check the DFS 'work' for output persistency");
        ScriptableCommandUtils.registerCommand(CombinedVerificationCommand.class, "checkDfsCombined",
                " combined check of the DFS 'work' for deadlock freeness and output persistency");

        ScriptableCommandUtils.registerCommand(MergeComponentTransformationCommand.class, "transformDfsMergeComponent",
                " transform the given DFS 'work' by merging selected components");
        ScriptableCommandUtils.registerCommand(ContractComponentTransformationCommand.class, "transformDfsContractComponent",
                " transform the given DFS 'work' by contracting selected components");
        ScriptableCommandUtils.registerCommand(WaggingGenerator2WayCommand.class, "transformDfsWagging2Way",
                "transform the given DFS 'work' by applying 2-way wagging to the selected pipeline section");
        ScriptableCommandUtils.registerCommand(WaggingGenerator3WayCommand.class, "transformDfsWagging3Way",
                "transform the given DFS 'work' by applying 3-way wagging to the selected pipeline section");
        ScriptableCommandUtils.registerCommand(WaggingGenerator4WayCommand.class, "transformDfsWagging4Way",
                "transform the given DFS 'work' by applying 4-way wagging to the selected pipeline section");
        // Do not register generic WaggingGeneratorCommand as it requires user input
        pm.registerCommand(WaggingGeneratorCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsDescriptor\"/>");
    }

}
