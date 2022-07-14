package org.workcraft.plugins.dfs;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.dfs.commands.*;
import org.workcraft.plugins.dfs.interop.VerilogExporter;
import org.workcraft.utils.ScriptableCommandUtils;

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
                "convert the DFS 'work' into a new STG work");

        ScriptableCommandUtils.registerCommand(DeadlockFreenessVerificationCommand.class, "checkDfsDeadlockFreeness",
                "check the DFS 'work' for deadlock freeness");
        ScriptableCommandUtils.registerCommand(OutputPersistencyVerificationCommand.class, "checkDfsOutputPersistency",
                "check the DFS 'work' for output persistency");
        ScriptableCommandUtils.registerCommand(CombinedVerificationCommand.class, "checkDfsCombined",
                " combined check of the DFS 'work' for deadlock freeness and output persistency");

        ScriptableCommandUtils.registerCommand(InsertLogicTransformationCommand.class, "transformDfsInsertLogic",
                "transform the DFS 'work' by inserting logic nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertRegisterTransformationCommand.class, "transformDfsInsertRegister",
                "transform the DFS 'work' by inserting register nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertControlRegisterTransformationCommand.class, "transformDfsInsertControlRegister",
                "transform the DFS 'work' by inserting control register nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertPushRegisterTransformationCommand.class, "transformDfsInsertPushRegister",
                "transform the DFS 'work' by inserting push register nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertPopRegisterTransformationCommand.class, "transformDfsInsertPopRegister",
                "transform the DFS 'work' by inserting pop register nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertCounterflowLogicTransformationCommand.class, "transformDfsInsertCounterflowLogic",
                "transform the DFS 'work' by inserting counterflow logic nodes into selected arcs");
        ScriptableCommandUtils.registerCommand(InsertCounterflowRegisterTransformationCommand.class, "transformDfsInsertCounterflowRegister",
                "transform the DFS 'work' by inserting counterflow register nodes into selected arcs");

        ScriptableCommandUtils.registerCommand(MergeComponentTransformationCommand.class, "transformDfsMergeComponent",
                " transform the DFS 'work' by merging selected components");
        ScriptableCommandUtils.registerCommand(ContractComponentTransformationCommand.class, "transformDfsContractComponent",
                " transform the DFS 'work' by contracting selected components");
        ScriptableCommandUtils.registerCommand(WaggingGenerator2WayCommand.class, "transformDfsWagging2Way",
                "transform the DFS 'work' by applying 2-way wagging to the selected pipeline section");
        ScriptableCommandUtils.registerCommand(WaggingGenerator3WayCommand.class, "transformDfsWagging3Way",
                "transform the DFS 'work' by applying 3-way wagging to the selected pipeline section");
        ScriptableCommandUtils.registerCommand(WaggingGenerator4WayCommand.class, "transformDfsWagging4Way",
                "transform the DFS 'work' by applying 4-way wagging to the selected pipeline section");
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
