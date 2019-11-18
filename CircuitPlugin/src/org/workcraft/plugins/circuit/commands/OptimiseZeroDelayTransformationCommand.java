package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.tasks.CheckTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class OptimiseZeroDelayTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Optimise zero delay of buffers and inverters (selected or all)";
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
    public Void execute(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        Collection<VisualFunctionComponent> components = collect(circuit);
        boolean noSelection = circuit.getSelection().isEmpty();
        if (components.isEmpty()) {
            if (noSelection) {
                DialogUtils.showError("No zero delay components in the circuit.");
            } else {
                DialogUtils.showError("No zero delay components were selected.");
            }
            return null;
        }
        if (!checkSpeedIndependence(we, MpsatUtils.getToolchainDescription(we.getTitle()))) {
            DialogUtils.showError("Conformantion and output persistency"
                    + " must hold before optimising zero delay components.");

            return null;
        }
        we.saveMemento();
        Collection<String> refs = new ArrayList<>();
        for (VisualFunctionComponent component : components) {
            if (component.getIsZeroDelay()) {
                component.setIsZeroDelay(false);
                String ref = circuit.getMathReference(component);
                if (checkSpeedIndependence(we, MpsatUtils.getToolchainDescription("zero delay '" + ref + "'"))) {
                    refs.add(ref);
                } else {
                    component.setIsZeroDelay(true);
                }
            }
        }
        if (refs.isEmpty()) {
            if (noSelection) {
                DialogUtils.showInfo("All zero delay assumptions in the circuit are necessary.");
            } else {
                DialogUtils.showInfo("All zero delay assumptions for the selected components are necessary.");
            }
        } else {
            DialogUtils.showInfo(ReferenceHelper.getTextWithReferences("Zero delay assumption is removed for component", refs));
        }
        return null;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
                && VerificationUtils.checkCircuitHasComponents(we)
                && VerificationUtils.checkInterfaceInitialState(we)
                && VerificationUtils.checkInterfaceConstrains(we, true);
    }

    private Boolean checkSpeedIndependence(WorkspaceEntry we, String description) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckTask task = new CheckTask(we, true, false, true);
        Result<? extends VerificationChainOutput> result = manager.execute(task, description);
        return MpsatUtils.getChainOutcome(result);
    }

    @Override
    public Collection<VisualFunctionComponent> collect(VisualModel model) {
        List<VisualFunctionComponent> result = new ArrayList<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(),
                    VisualFunctionComponent.class, component -> component.getIsZeroDelay()));

            Collection<VisualNode> selection = model.getSelection();
            if (!selection.isEmpty()) {
                result.retainAll(selection);
            }
            Map<VisualFunctionComponent, Integer> forkCountMap = new HashMap<>();
            for (VisualFunctionComponent component : result) {
                forkCountMap.put(component, getForkCount(circuit, component));
            }
            Collections.sort(result, Comparator.comparingInt(forkCountMap::get));
        }
        return result;
    }

    private int getForkCount(VisualCircuit circuit, VisualFunctionComponent component) {
        int result = 0;
        for (VisualContact contact : component.getVisualInputs()) {
            VisualContact driver = CircuitUtils.findDriver(circuit, contact, false);
            if (driver != null) {
                Collection<VisualContact> driven = CircuitUtils.findDriven(circuit, driver, false);
                result += driven.size();
            }
        }
        return result;
    }


    @Override
    public void transform(VisualModel model, VisualNode node) {
    }

}
