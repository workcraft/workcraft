package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.utils.SortUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class MatchingUtils {

    private MatchingUtils() {
    }

    public static String getSignalWithBusSuffix(String name, Integer index) {
        return (name == null) ? null : (name + getProcessedBusSuffix(index));
    }

    private static String getProcessedBusSuffix(Integer index) {
        return (index == null) ? "" : CircuitSettings.getProcessedBusSuffix(Integer.toString(index));
    }

    public static boolean isMatchingExactOrBus(String name, String exactOrBusName) {
        return isMatchingExact(name, exactOrBusName) || isMatchingBus(name, exactOrBusName);
    }

    public static boolean isMatchingExact(String name, String exactName) {
        return (exactName != null) && exactName.equals(name);
    }

    public static boolean isMatchingBus(String name, String busName) {
        return (busName != null) && CircuitSettings.getBusSignalPattern(busName).matcher(name).matches();
    }

    public static List<VisualContact> getSortedMatchingInputPins(VisualFunctionComponent component, String exactOrBusName) {
        return component.getVisualInputs().stream()
                .filter(contact -> isMatchingExactOrBus(contact.getName(), exactOrBusName))
                .sorted((c1, c2) -> SortUtils.compareNatural(c1, c2, VisualContact::getName))
                .collect(Collectors.toList());
    }

    public static List<VisualContact> getSortedMatchingOutputPins(VisualFunctionComponent component, String exactOrBusName) {
        return component.getVisualOutputs().stream()
                .filter(contact -> isMatchingExactOrBus(contact.getName(), exactOrBusName))
                .sorted((c1, c2) -> SortUtils.compareNatural(c1, c2, VisualContact::getName))
                .collect(Collectors.toList());
    }

}
