package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class ToggleBubbleTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Toggle inversion of selected contacts and outputs of selected components";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Toggle inversion";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent)
                || ((node instanceof VisualFunctionContact)
                && (node.getParent() instanceof VisualFunctionComponent));
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        if (node instanceof VisualFunctionComponent) {
            FunctionComponent component = ((VisualFunctionComponent) node).getReferencedComponent();
            if (checkSetResetFunctions(component.getFunctionOutputs())) {
                return true;
            }
        }
        if (node instanceof VisualFunctionContact) {
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedComponent();
            if (checkSetResetFunctions(getDependantContacts(contact))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSetResetFunctions(Collection<FunctionContact> contacts) {
        for (FunctionContact contact : contacts) {
            if ((contact.getSetFunction() != null) || (contact.getResetFunction() != null)) {
                return true;
            }
        }
        return false;
    }

    private HashSet<FunctionContact> getDependantContacts(FunctionContact contact) {
        HashSet<FunctionContact> result = new HashSet<>();
        Node parent = contact.getParent();
        if (parent instanceof FunctionComponent) {
            if (contact.isOutput()) {
                result.add(contact);
            } else {
                FunctionComponent component = (FunctionComponent) parent;
                for (Contact outputContact: component.getOutputs()) {
                    if (outputContact instanceof FunctionContact) {
                        result.add((FunctionContact) outputContact);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> contacts = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            contacts.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionContact.class));
            Collection<VisualNode> selection = new LinkedList<>(circuit.getSelection());
            for (Node node: new LinkedList<>(selection)) {
                if (node instanceof VisualFunctionComponent) {
                    VisualFunctionComponent component = (VisualFunctionComponent) node;
                    selection.addAll(component.getVisualOutputs());
                }
            }
            contacts.retainAll(selection);
        }
        return contacts;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionContact)) {
            VisualFunctionComponent component = null;
            boolean isZeroDelayComponent = false;
            Node parent = node.getParent();
            if (parent instanceof VisualFunctionComponent) {
                component = (VisualFunctionComponent) parent;
                isZeroDelayComponent = component.getIsZeroDelay();
            }
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedComponent();
            if (contact.isOutput()) {
                BooleanFormula setFunction = contact.getSetFunction();
                BooleanFormula resetFunction = contact.getResetFunction();
                if ((setFunction != null) && (resetFunction == null)) {
                    contact.setSetFunction(FormulaUtils.invert(setFunction));
                } else {
                    contact.setSetFunction(resetFunction);
                    contact.setResetFunction(setFunction);
                }
            } else {
                for (FunctionContact dependantContact: getDependantContacts(contact)) {
                    BooleanFormula setFunction = dependantContact.getSetFunction();
                    BooleanFormula notContact = FormulaUtils.invert(contact);
                    if (setFunction != null) {
                        BooleanFormula f = FormulaUtils.replace(setFunction, contact, notContact, CleverBooleanWorker.getInstance());
                        dependantContact.setSetFunction(f);
                    }
                    BooleanFormula resetFunction = dependantContact.getResetFunction();
                    if (resetFunction != null) {
                        BooleanFormula f = FormulaUtils.replace(resetFunction, contact, notContact, CleverBooleanWorker.getInstance());
                        dependantContact.setResetFunction(f);
                    }
                }
            }
            if (component != null) {
                String label = component.getLabel();
                if (!label.isEmpty()) {
                    VisualCircuit circuit = (VisualCircuit) model;
                    String ref = circuit.getMathModel().getComponentReference(component.getReferencedComponent());
                    LogUtils.logWarning("Label '" + label + "' is removed from component '" + ref + "'");
                    component.clearMapping();
                }
                component.setIsZeroDelay(isZeroDelayComponent);
                component.invalidateRenderingResult();
                model.addToSelection(component);
            }
        }
    }

}
