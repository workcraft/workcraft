package org.workcraft.plugins.fsm;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.fsm.commands.*;
import org.workcraft.plugins.fsm.serialisation.EventDeserialiser;
import org.workcraft.plugins.fsm.serialisation.EventSerialiser;

@SuppressWarnings("unused")
public class FsmPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Finite State Machine plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(FsmDescriptor.class);

        pm.registerXmlSerialiser(EventSerialiser.class);
        pm.registerXmlDeserialiser(EventDeserialiser.class);

        ScriptableCommandUtils.registerCommand(FsmToGraphConversionCommand.class, "convertFsmToGraph",
                "convert the FSM 'work' into a new Graph work");
        ScriptableCommandUtils.registerCommand(GraphToFsmConversionCommand.class, "convertGraphToFsm",
                "convert the Graph 'work' into a new FSM work");
        ScriptableCommandUtils.registerCommand(FsmToPetriConversionCommand.class, "convertFsmToPetri",
                "convert the FSM 'work' into a new Petri net work");

        ScriptableCommandUtils.registerCommand(DeadlockFreenessVerificationCommand.class, "checkFsmDeadlockFreeness",
                "check the FSM/FST 'work' for deadlock freeness");
        ScriptableCommandUtils.registerCommand(DeterminismVerificationCommand.class, "checkFsmDeterminism",
                "check the FSM/FST 'work' for determinism");
        ScriptableCommandUtils.registerCommand(ReachabilityVerificationCommand.class, "checkFsmReachability",
                "check the FSM/FST 'work' for reachability of all states");
        ScriptableCommandUtils.registerCommand(ReversibilityVerificationCommand.class, "checkFsmReversibility",
                "check the FSM/FST 'work' for reversibility of all states");

        ScriptableCommandUtils.registerCommand(ContractStateTransformationCommand.class, "transformFsmContractState",
                "transform the FSM/FST 'work' by contracting selected states");
        ScriptableCommandUtils.registerCommand(MergeStateTransformationCommand.class, "transformFsmMergeState",
                "transform the FSM/FST 'work' by merging selected states");
        ScriptableCommandUtils.registerCommand(SplitStateTransformationCommand.class, "transformFsmSplitState",
                "transform the FSM/FST 'work' by splitting selected states");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.fsm.FsmModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.fsm.FsmDescriptor\"/>");
    }

}
