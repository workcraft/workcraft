package org.workcraft.plugins.fsm;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.plugins.fsm.serialisation.EventDeserialiser;
import org.workcraft.plugins.fsm.serialisation.EventSerialiser;
import org.workcraft.plugins.fsm.tools.DeadlockCheckerTool;
import org.workcraft.plugins.fsm.tools.DeterminismCheckerTool;
import org.workcraft.plugins.fsm.tools.DgToFsmConverterTool;
import org.workcraft.plugins.fsm.tools.FsmToDgConverterTool;
import org.workcraft.plugins.fsm.tools.FsmToPnConverterTool;
import org.workcraft.plugins.fsm.tools.ReachabilityCheckerTool;
import org.workcraft.plugins.fsm.tools.ReversibilityCheckerTool;
import org.workcraft.plugins.fsm.tools.StateMergerTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmModule  implements Module {

    private final class FsmContractorTool extends AbstractContractorTool {
        @Override
        public boolean isApplicableTo(WorkspaceEntry we) {
            return we.getModelEntry().getMathModel() instanceof Fsm;
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

        pm.registerClass(Tool.class, FsmToDgConverterTool.class);
        pm.registerClass(Tool.class, DgToFsmConverterTool.class);
        pm.registerClass(Tool.class, FsmToPnConverterTool.class);
        pm.registerClass(Tool.class, DeadlockCheckerTool.class);
        pm.registerClass(Tool.class, DeterminismCheckerTool.class);
        pm.registerClass(Tool.class, ReachabilityCheckerTool.class);
        pm.registerClass(Tool.class, ReversibilityCheckerTool.class);
        pm.registerClass(Tool.class, StateMergerTool.class);

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new FsmContractorTool();
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
