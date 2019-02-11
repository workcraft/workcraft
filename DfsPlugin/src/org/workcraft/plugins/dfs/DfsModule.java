package org.workcraft.plugins.dfs;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.plugins.dfs.commands.*;
import org.workcraft.plugins.dfs.interop.VerilogExporter;

public class DfsModule implements org.workcraft.Module {

    @Override
    public String getDescription() {
        return "Dataflow Structure";
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

        ScriptableCommandUtils.register(DfsToStgConversionCommand.class, "convertDfsToStg",
                "convert the given DFS 'work' into a new STG work");

        ScriptableCommandUtils.register(DfsDeadlockFreenessVerificationCommand.class, "checkDfsDeadlockFreeness",
                "check the DFS 'work' for deadlock freeness");
        ScriptableCommandUtils.register(DfsOutputPersisitencyVerificationCommand.class, "checkDfsOutputPersistency",
                "check the DFS 'work' for output persistency");
        ScriptableCommandUtils.register(DfsCombinedVerificationCommand.class, "checkDfsCombined",
                " combined check of the DFS 'work' for deadlock freeness and output persistency");

        ScriptableCommandUtils.register(DfsMergeComponentTransformationCommand.class, "transformDfsMergeComponent",
                " transform the given DFS 'work' by merging selected components");
        ScriptableCommandUtils.register(DfsContractComponentTransformationCommand.class, "transformDfsContractComponent",
                " transform the given DFS 'work' by contracting selected components");
        ScriptableCommandUtils.register(WaggingGenerator2WayCommand.class, "transformDfsWagging2Way",
                "transform the given DFS 'work' by applying 2-way wagging to the selected pipeline section");
        ScriptableCommandUtils.register(WaggingGenerator3WayCommand.class, "transformDfsWagging3Way",
                "transform the given DFS 'work' by applying 3-way wagging to the selected pipeline section");
        ScriptableCommandUtils.register(WaggingGenerator4WayCommand.class, "transformDfsWagging4Way",
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
