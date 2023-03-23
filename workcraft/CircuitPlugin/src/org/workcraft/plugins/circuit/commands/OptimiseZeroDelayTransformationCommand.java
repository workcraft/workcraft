package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.tasks.CheckTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;

public class OptimiseZeroDelayTransformationCommand extends AbstractTransformationCommand {

    public static final String TITLE = "Zero delay optimisation";

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
    public void transform(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return;
        }
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        Collection<VisualFunctionComponent> components = collectNodes(circuit);
        boolean noSelection = circuit.getSelection().isEmpty();
        if (components.isEmpty()) {
            if (noSelection) {
                DialogUtils.showError("No zero delay components in the circuit.");
            } else {
                DialogUtils.showError("No zero delay components were selected.");
            }
            return;
        }
        boolean checkConformation = true;
        boolean checkPersistency = true;
        Circuit mathCircuit = circuit.getMathModel();
        File envFile = mathCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadOrImportStg(envFile);
        if (envStg == null) {
            String message = "Environment STG is missing, so conformation cannot be checked during optimisation";
            String question = ".\n\nProceed checking output persistency only?";
            if (!DialogUtils.showConfirmWarning(message, question, TITLE, true)) {
                return;
            }
            checkConformation = false;
        } else {
            if (!VerificationUtils.checkInterfaceConstrains(we, true)) {
                return;
            }
            if (!envStg.getDummyTransitions().isEmpty()) {
                String message = "Environment STG has dummies, so output persistency cannot be checked during optimisation";
                String question = ".\n\nProceed checking conformation only?";
                if (!DialogUtils.showConfirmWarning(message, question, TITLE, true)) {
                    return;
                }
                checkPersistency = false;
            }
        }
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        Boolean isGoodInitial = checkSpeedIndependence(we, description, checkConformation, checkPersistency);
        if (isGoodInitial == null) {
            return;
        }
        if (!isGoodInitial) {
            String msg = (checkConformation && checkPersistency
                    ? "Conformantion and output persistency"
                    : checkConformation ? "Conformation" : "Output persistence")
                    + " must hold before optimising zero delay components.";
            DialogUtils.showError(msg);
            return;
        }
        we.captureMemento();
        Collection<String> refs = new ArrayList<>();
        for (VisualFunctionComponent component : components) {
            if (component.getIsZeroDelay()) {
                component.setIsZeroDelay(false);
                String ref = Identifier.truncateNamespaceSeparator(circuit.getMathReference(component));
                String descriptionIteration = MpsatUtils.getToolchainDescription("zero delay '" + ref + "'");
                Boolean isGoodIteration = checkSpeedIndependence(we, descriptionIteration, checkConformation, checkPersistency);
                if (isGoodIteration == null) {
                    we.cancelMemento();
                    return;
                }
                if (isGoodIteration) {
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
            String msg = TextUtils.wrapMessageWithItems(
                    "Zero delay assumption is removed for component", refs);

            String question = refs.size() > 1
                    ? "\n\nUpdate these components names to default values?"
                    : "\n\nUpdate the component name to default value?";

            if (DialogUtils.showConfirmInfo(msg, question)) {
                renameOptimisedComponents(mathCircuit, refs);
            }
            we.saveMemento();
        }
    }

    private void renameOptimisedComponents(Circuit mathModel, Collection<String> refs) {
        HierarchyReferenceManager refManager = mathModel.getReferenceManager();
        for (String ref : refs) {
            MathNode node = mathModel.getNodeByReference(ref);
            if (node instanceof CircuitComponent) {
                NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(node);
                NameManager nameManager = refManager.getNameManager(namespaceProvider);
                nameManager.setDefaultName(node);
                String newRef = mathModel.getNodeReference(node);
                LogUtils.logInfo("Component '" + ref + "' was renamed to '" + newRef + "'.");
            }
        }
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we) && VerificationUtils.checkInterfaceInitialState(we);
    }

    private Boolean checkSpeedIndependence(WorkspaceEntry we, String description,
            boolean checkConformation, boolean checkPersistence) {

        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckTask task = new CheckTask(we, checkConformation, false, checkPersistence);
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        monitor.setInteractive(false);
        // FIXME: Execute synchronously (asynchronous queueing blocks when called from the main menu)
        manager.execute(task, description, monitor);
        return monitor.waitForHandledResult();
    }

    @Override
    public Collection<VisualFunctionComponent> collectNodes(VisualModel model) {
        List<VisualFunctionComponent> result = new ArrayList<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(),
                    VisualFunctionComponent.class, VisualFunctionComponent::getIsZeroDelay));

            Collection<VisualNode> selection = model.getSelection();
            if (!selection.isEmpty()) {
                result.retainAll(selection);
            }
            Map<VisualFunctionComponent, Integer> forkCountMap = new HashMap<>();
            for (VisualFunctionComponent component : result) {
                forkCountMap.put(component, getForkCount(circuit, component));
            }
            result.sort(Comparator.comparingInt(forkCountMap::get));
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

}
