package org.workcraft.plugins.fsm;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractTransformationCommand;
import org.workcraft.plugins.fsm.serialisation.EventDeserialiser;
import org.workcraft.plugins.fsm.serialisation.EventSerialiser;
import org.workcraft.plugins.fsm.tools.FsmDeadlockVerificationCommand;
import org.workcraft.plugins.fsm.tools.FsmDeterminismVerificationCommand;
import org.workcraft.plugins.fsm.tools.FsmToGraphConversionCommand;
import org.workcraft.plugins.fsm.tools.FsmToPetriConversionCommand;
import org.workcraft.plugins.fsm.tools.GraphToFsmConversionCommand;
import org.workcraft.plugins.fsm.tools.FsmReachabilityVerificationCommand;
import org.workcraft.plugins.fsm.tools.FsmReversibilityVerificationCommand;
import org.workcraft.plugins.fsm.tools.MergeStateTransformationCommand;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FsmModule  implements Module {

    private final class ContractStateTransformationCommand extends AbstractContractTransformationCommand {
        @Override
        public boolean isApplicableTo(ModelEntry me) {
            return WorkspaceUtils.isApplicable(me, Fsm.class);
        }
    }

    @Override
    public String getDescription() {
        return "Finite State Machine";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, FsmDescriptor.class);

        pm.registerClass(XMLSerialiser.class, EventSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, EventDeserialiser.class);

        pm.registerClass(Command.class, FsmToGraphConversionCommand.class);
        pm.registerClass(Command.class, GraphToFsmConversionCommand.class);
        pm.registerClass(Command.class, FsmToPetriConversionCommand.class);
        pm.registerClass(Command.class, FsmDeadlockVerificationCommand.class);
        pm.registerClass(Command.class, FsmDeterminismVerificationCommand.class);
        pm.registerClass(Command.class, FsmReachabilityVerificationCommand.class);
        pm.registerClass(Command.class, FsmReversibilityVerificationCommand.class);
        pm.registerClass(Command.class, MergeStateTransformationCommand.class);

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new ContractStateTransformationCommand();
            }
        });
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.fsm.FsmModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.fsm.FsmDescriptor\"/>");
    }

}
