package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;

public final class ZeroDelayUtils {

    private ZeroDelayUtils() {
    }

    public static Set<FunctionComponent> getPrecedingZeroDelayComponents(Circuit circuit, FunctionComponent component) {
        Set<FunctionComponent> result = new HashSet<>();
        for (Contact inputPin : component.getInputs()) {
            Contact driverContact = CircuitUtils.findDriver(circuit, inputPin, false);
            if ((driverContact != null) && (driverContact.getParent() instanceof FunctionComponent driverComponent)
                    && driverComponent.getIsZeroDelay()) {

                result.add(driverComponent);
            }
        }
        return result;
    }

    public static void makeZeroDelayIfValid(Circuit circuit, Collection<FunctionComponent> components) {
        List<String> skippedComponentRefs = new ArrayList<>();
        for (FunctionComponent component : components) {
            if (isValidZeroDelayComponent(circuit, component)) {
                component.setIsZeroDelay(true);
            } else {
                skippedComponentRefs.add(circuit.getComponentReference(component));
            }
        }
        if (!skippedComponentRefs.isEmpty()) {
            SortUtils.sortNatural(skippedComponentRefs);
            DialogUtils.showWarning(TextUtils.wrapMessageWithItems(
                    "Zero delay property is removed from component", skippedComponentRefs));
        }
    }

    public static boolean isValidZeroDelayComponent(Circuit circuit, FunctionComponent component) {
        return ((component.isBuffer() || component.isInverter())
                && !StructureUtils.hasSelfLoop(circuit, component)
                && !hasAdjacentOtherZeroDelayComponent(circuit, component)
                && !hasDrivenHierarchicalComponent(circuit, component)
                && StructureUtils.getPostsetPorts(circuit, component).isEmpty()
                && CircuitUtils.findDriven(circuit, component.getGateOutput(), false).size() < 2);
    }

    public static boolean hasAdjacentOtherZeroDelayComponent(Circuit circuit, FunctionComponent component) {
        for (FunctionComponent adjacentComponent : StructureUtils.getAdjacentComponents(circuit, component)) {
            if (adjacentComponent.getIsZeroDelay()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDrivenHierarchicalComponent(Circuit circuit, FunctionComponent component) {
        for (FunctionComponent succComponent : StructureUtils.getPostsetComponents(circuit, component)) {
            if (succComponent.isBlackbox()) {
                return true;
            }
        }
        return false;
    }

}
