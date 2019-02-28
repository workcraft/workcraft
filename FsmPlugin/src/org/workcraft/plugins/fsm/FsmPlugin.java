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

        ScriptableCommandUtils.register(FsmToGraphConversionCommand.class, "convertFsmToGraph",
                "convert the given FSM 'work' into a new Graph work");
        ScriptableCommandUtils.register(GraphToFsmConversionCommand.class, "convertGraphToFsm",
                "convert the given Graph 'work' into a new FSM work");
        ScriptableCommandUtils.register(FsmToPetriConversionCommand.class, "convertFsmToPetri",
                "convert the given FSM 'work' into a new Petri net work");

        ScriptableCommandUtils.register(FsmDeadlockFreenessVerificationCommand.class, "checkFsmDeadlockFreeness",
                "check the FSM or FST 'work' for deadlock freeness");
        ScriptableCommandUtils.register(FsmDeterminismVerificationCommand.class, "checkFsmDeterminism",
                "check the FSM or FST 'work' for determinism");
        ScriptableCommandUtils.register(FsmReachabilityVerificationCommand.class, "checkFsmReachability",
                "check the FSM or FST 'work' for reachability of all states");
        ScriptableCommandUtils.register(FsmReversibilityVerificationCommand.class, "checkFsmReversibility",
                "check the FSM or FST 'work' for reversibility of all states");

        ScriptableCommandUtils.register(FsmMergeStateTransformationCommand.class, "transformFsmMergeState",
                "transform the given FSM or FST 'work' by merging selected states");
        ScriptableCommandUtils.register(FsmContractStateTransformationCommand.class, "transformFsmContractState",
                "transform the given FSM or FST 'work' by contracting selected states");
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
