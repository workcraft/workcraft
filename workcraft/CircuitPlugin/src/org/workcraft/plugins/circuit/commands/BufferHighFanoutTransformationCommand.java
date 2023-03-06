package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.plugins.circuit.utils.SpaceUtils;
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
            VisualContact driver = (VisualContact) node;
            insertForkGate(circuit, driver);
        }
    }

    private VisualFunctionComponent insertForkGate(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = null;
        Set<VisualContact> driven = CircuitUtils.findDriven(circuit, contact, false);
        int forkCount = driven.size();
        // Try to reuse existing buffer or inverter
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            if (component.isBuffer()) {
                result = component;
            }
        }
        String contactRef = circuit.getMathReference(contact);
        LogUtils.logMessage(PropertyHelper.BULLET_PREFIX + forkCount + "-way fork at " + contactRef + ": "
                + (result == null ? "insert new buffer" : "reuse existing buffer"));

        // Insert fork buffer if reuse did not work out
        if (result == null) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
            VisualFunctionContact gateOutput = result.getGateOutput();
            gateOutput.setInitToOne(contact.getInitToOne());
        }
        // Name gate according to its fanout
        circuit.setMathName(result, CircuitSettings.getForkBufferName(forkCount));
        return result;
    }

}
