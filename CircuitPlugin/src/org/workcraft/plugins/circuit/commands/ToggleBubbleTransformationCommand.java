package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.utils.BooleanUtils;
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
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedContact();
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
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> contacts = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            contacts.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionContact.class));
            Collection<Node> selection = new LinkedList<>(circuit.getSelection());
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
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionContact)) {
            FunctionContact contact = ((VisualFunctionContact) node).getReferencedContact();
            if (contact.isOutput()) {
                BooleanFormula setFunction = contact.getSetFunction();
                BooleanFormula resetFunction = contact.getResetFunction();
                if (resetFunction == null) {
                    contact.setSetFunctionQuiet(BooleanOperations.not(setFunction));
                } else {
                    contact.setSetFunctionQuiet(resetFunction);
                    contact.setResetFunctionQuiet(setFunction);
                }
            } else {
                for (FunctionContact dependantContact: getDependantContacts(contact)) {
                    BooleanFormula setFunction = dependantContact.getSetFunction();
                    BooleanFormula notContact = BooleanOperations.not(contact);
                    if (setFunction != null) {
                        dependantContact.setSetFunctionQuiet(BooleanUtils.replaceDumb(setFunction, contact, notContact));
                    }
                    BooleanFormula resetFunction = dependantContact.getResetFunction();
                    if (resetFunction != null) {
                        dependantContact.setResetFunctionQuiet(BooleanUtils.replaceDumb(resetFunction, contact, notContact));
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
                    component.getReferencedComponent().setModule("");
                }
                component.invalidateRenderingResult();
                model.addToSelection(component);
            }
        }
    }

}
