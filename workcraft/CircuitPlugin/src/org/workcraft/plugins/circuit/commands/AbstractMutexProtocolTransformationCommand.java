package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.utils.SortUtils;
import org.workcraft.workspace.ModelEntry;

import java.awt.geom.Point2D;
import java.util.*;

public abstract class AbstractMutexProtocolTransformationCommand extends AbstractComponentTransformationCommand {

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return (node instanceof VisualFunctionComponent)
                && ArbitrationUtils.hasMutexInterface((VisualFunctionComponent) node);
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        if (model.getSelection().size() == 1) {
            return super.collectNodes(model);
        }
        Collection<VisualNode> result = new HashSet<>();
        for (VisualNode node : super.collectNodes(model)) {
            if ((node instanceof VisualFunctionComponent component)
                    && (component.getReferencedComponent().getIsArbitrationPrimitive())) {

                result.add(component);
            }
        }
        return result;
    }

    @Override
    public void transformComponent(VisualCircuit circuit, VisualFunctionComponent component) {

        Map<VisualContact, Point2D> pinToPositionMap = new HashMap<>();
        // Save pin positions
        for (VisualContact pin : component.getVisualContacts()) {
            pinToPositionMap.put(pin, pin.getPosition());
        }
        transformComponent(circuit.getMathModel(), component.getReferencedComponent());
        // Restore pin positions
        for (VisualContact pin : component.getVisualContacts()) {
            Point2D pos = pinToPositionMap.get(pin);
            if (pos != null) {
                pin.setPosition(pos);
            }
        }
    }

    private void transformComponent(Circuit circuit, FunctionComponent component) {
        Collection<FunctionContact> inputPins = component.getFunctionInputs();
        Collection<FunctionContact> outputPins = component.getFunctionOutputs();
        Mutex.Protocol protocol = getMutexProtocol();
        Mutex mutexModule = CircuitSettings.parseMutexData(protocol);
        if ((inputPins.size() == 2) && (outputPins.size() == 2) && (mutexModule != null)) {
            List<FunctionContact> orderedInputPins = SortUtils.getSortedNatural(inputPins, FunctionContact::getName);
            List<FunctionContact> orderedOutputPins = SortUtils.getSortedNatural(outputPins, FunctionContact::getName);
            FunctionContact r1Pin = orderedInputPins.get(0);
            FunctionContact g1Pin = orderedOutputPins.get(0);
            FunctionContact r2Pin = orderedInputPins.get(1);
            FunctionContact g2Pin = orderedOutputPins.get(1);
            ArbitrationUtils.setMutexFunctions(protocol, r1Pin, g1Pin, r2Pin, g2Pin);
            component.setRefinement(null);
            component.setIsArbitrationPrimitive(true);
            circuit.setName(r1Pin, mutexModule.r1.name);
            circuit.setName(g1Pin, mutexModule.g1.name);
            circuit.setName(r2Pin, mutexModule.r2.name);
            circuit.setName(g2Pin, mutexModule.g2.name);
            component.setModule(mutexModule.name);
        }
    }

    abstract Mutex.Protocol getMutexProtocol();

}
