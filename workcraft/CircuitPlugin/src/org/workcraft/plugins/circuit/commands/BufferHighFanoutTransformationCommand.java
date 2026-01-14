package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
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
        if (model instanceof VisualCircuit circuit) {
            for (VisualContact driver : circuit.getVisualDrivers()) {
                // Skip forks driven by constant
                if (CircuitUtils.isConstant(driver.getReferencedComponent())) continue;
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
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualContact contact)) {
            VisualFunctionComponent buffer = GateUtils.insertOrReuseBuffer(circuit, contact);
            // Name gate according to its fanout and report buffering of the high fanout fork
            VisualFunctionContact bufferOutput = buffer.getGateOutput();
            Set<VisualContact> driven = CircuitUtils.findDriven(circuit, bufferOutput, false);
            int fanout = driven.size();
            circuit.setMathName(buffer, CircuitSettings.getForkBufferName(fanout));
            String contactRef = circuit.getMathReference(contact);
            String prefix = fanout + "-way fork at " + contactRef + ": ";
            String suffix = bufferOutput == contact ? "reusing buffer" : "inserting new buffer";
            LogUtils.logMessage(TextUtils.getBullet(prefix + suffix));
        }
    }

}
