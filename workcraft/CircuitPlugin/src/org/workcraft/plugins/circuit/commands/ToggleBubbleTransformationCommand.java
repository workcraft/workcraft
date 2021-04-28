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
    public String getPopupName() {
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
        boolean result = false;
        if (node instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            if (!component.getVisualOutputs().isEmpty()) {
                result = true;
            }
        } else if (node instanceof VisualFunctionContact) {
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedComponent();
            for (FunctionContact dependantContact: getDependantContacts(contact)) {
                if ((dependantContact.getSetFunction() != null) || (dependantContact.getResetFunction() != null)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
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
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedComponent();
            if (contact.isOutput()) {
                BooleanFormula setFunction = contact.getSetFunction();
                BooleanFormula resetFunction = contact.getResetFunction();
                if (resetFunction == null) {
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
            Node parent = node.getParent();
            if (parent instanceof VisualFunctionComponent) {
                VisualFunctionComponent component = (VisualFunctionComponent) parent;
                String label = component.getLabel();
                if (!label.isEmpty()) {
                    String ref = model.getMathReference(node);
                    LogUtils.logWarning("Label '" + label + "' is removed from component '" + ref + "'.");
                    component.clearMapping();
                }
                component.invalidateRenderingResult();
                model.addToSelection(component);
            }
        }
    }

}
