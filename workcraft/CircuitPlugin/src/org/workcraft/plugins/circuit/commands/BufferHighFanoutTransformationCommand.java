package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BufferHighFanoutTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Buffer high fanout";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualContact> collectNodes(VisualModel model) {
        Collection<VisualContact> result = new HashSet<>();
        int forkHighFanout = CircuitSettings.getForkHighFanout();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            for (VisualContact driver : circuit.getVisualDrivers()) {
                Set<VisualContact> driven = CircuitUtils.findDriven(circuit, driver, false);
                if (driven.size() >= forkHighFanout) {
                    result.add(driver);
                }
            }
        }

        String highFanoutForksDetail = " forks with high fanout (" + forkHighFanout + " and above)";
        if (result.isEmpty()) {
            LogUtils.logInfo("No" + highFanoutForksDetail);
        } else {
            LogUtils.logInfo("Buffering " + result.size() + highFanoutForksDetail);
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualContact)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualContact contact = (VisualContact) node;
            VisualFunctionComponent buffer = GateUtils.insertOrReuseBuffer(circuit, contact);
            // Name gate according to its fanout and report buffering of the high fanout fork
            Set<VisualContact> driven = CircuitUtils.findDriven(circuit, contact, false);
            int fanout = driven.size();
            circuit.setMathName(buffer, CircuitSettings.getForkBufferName(fanout));
            String contactRef = circuit.getMathReference(contact);
            boolean isReused = buffer.getGateOutput() == contact;
            LogUtils.logMessage(PropertyHelper.BULLET_PREFIX + fanout + "-way fork at " + contactRef + ": "
                    + (isReused ? "reusing buffer" : "inserting new buffer"));
        }
    }

}
