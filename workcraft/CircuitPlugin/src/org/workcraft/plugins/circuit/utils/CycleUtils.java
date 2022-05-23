package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.tools.CycleAnalyserTool;
import org.workcraft.utils.*;

import java.util.*;

public final class CycleUtils {

    private static final String VERIFICATION_RESULT_TITLE = "Verification result";

    private CycleUtils() {
    }

    public static Set<Contact> tagPathBreakerClearAll(Circuit circuit) {
        Collection<Contact> contacts = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                Contact.class, Contact::isPin);

        return setPathBreaker(contacts, false);
    }

    public static Set<Contact> tagPathBreakerSelfloopPins(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        Set<Contact> contacts = DirectedGraphUtils.findSelfloopVertices(graph);
        return setPathBreaker(contacts, true);
    }

    public static Set<Contact> tagPathBreakerAutoAppend(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        Set<Contact> contacts = DirectedGraphUtils.findFeedbackVertices(graph);
        return setPathBreaker(contacts, true);
    }

    private static Set<Contact> setPathBreaker(Collection<? extends Contact> contacts, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            if (contact.getPathBreaker() != value) {
                contact.setPathBreaker(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static Set<Contact> tagPathBreakerAutoDiscard(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        boolean progress = true;
        int initCount = getCycledDrivers(circuit).size();
        while (progress) {
            progress = false;
            for (Contact contact : getPathBreakerDrivers(circuit)) {
                contact.setPathBreaker(false);
                int curCount = getCycledDrivers(circuit).size();
                if (curCount == initCount) {
                    result.add(contact);
                    progress = true;
                } else {
                    contact.setPathBreaker(true);
                }
            }
        }
        return result;
    }

    public static Set<Contact> getPathBreakerDrivers(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getOutputs()) {
                if (contact.getPathBreaker()) {
                    result.add(contact);
                }
            }
        }
        return result;
    }

    public static Set<Contact> getCycledDrivers(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        return DirectedGraphUtils.findLoopedVertices(graph);
    }

    private static Map<Contact, Set<Contact>> buildGraph(Circuit circuit) {
        Map<Contact, Set<Contact>> result = new HashMap<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            HashSet<Contact> drivers = new HashSet<>();
            for (Contact input : component.getInputs()) {
                Contact driver = findUnbrokenPathDriverPin(circuit, input);
                if (driver != null) {
                    drivers.add(driver);
                }
            }
            for (Contact output : component.getOutputs()) {
                if (!output.getPathBreaker()) {
                    result.put(output, drivers);
                }
            }
        }
        return result;
    }

    public static Contact findUnbrokenPathDriverPin(Circuit circuit, Contact contact) {
        if (!contact.getPathBreaker()) {
            Contact driver = CircuitUtils.findDriver(circuit, contact, false);
            if ((driver != null) && driver.isPin() && !driver.getPathBreaker()) {
                if (!driver.isZeroDelayPin()) {
                    return driver;
                } else {
                    FunctionComponent zeroComponent = (FunctionComponent) driver.getParent();
                    return findUnbrokenPathDriverPin(circuit, zeroComponent.getFirstInput());
                }
            }
        }
        return null;
    }

    public static Set<Contact> findUnbrokenPathDrivenPins(Circuit circuit, Contact contact) {
        Set<Contact> result = new HashSet<>();
        if (!contact.getPathBreaker()) {
            for (Contact driven : CircuitUtils.findDriven(circuit, contact, false)) {
                if ((driven != null) && driven.isPin() && !driven.getPathBreaker()) {
                    if (!driven.isZeroDelayPin()) {
                        result.add(driven);
                    } else {
                        FunctionComponent zeroComponent = (FunctionComponent) driven.getParent();
                        result.addAll(findUnbrokenPathDrivenPins(circuit, zeroComponent.getFirstOutput()));
                    }
                }
            }
        }
        return result;
    }

    public static Boolean checkCycleAbsence(Circuit circuit, boolean useAnalysisTool) {
        if (!VerificationUtils.checkBlackboxComponents(circuit)) {
            return null;
        }

        Set<Contact> pathBreakerPins = getPathBreakerDrivers(circuit);
        List<String> pathBreakerPinRefs = ReferenceHelper.getReferenceList(circuit, pathBreakerPins);
        String pathBreakerText = "\n\n" + PropertyHelper.BULLET_PREFIX + (pathBreakerPinRefs.isEmpty()
                ? "No path breaker pins"
                : TextUtils.wrapMessageWithItems("Path breaker pin", pathBreakerPinRefs));

        List<String> cycleComponentRefs = new ArrayList<>();
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        for (Contact contact : DirectedGraphUtils.findLoopedVertices(graph)) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                String componentRef = circuit.getNodeReference(parent);
                cycleComponentRefs.add(Identifier.truncateNamespaceSeparator(componentRef));
            }
        }

        if (cycleComponentRefs.isEmpty()) {
            DialogUtils.showInfo("Circuit is free of unbroken cycles."
                    + pathBreakerText, VERIFICATION_RESULT_TITLE);

            return true;
        }

        if (useAnalysisTool) {
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()) {
                Toolbox toolbox = framework.getMainWindow().getCurrentToolbox();
                toolbox.selectTool(toolbox.getToolInstance(CycleAnalyserTool.class));
            }
        }

        SortUtils.sortNatural(cycleComponentRefs);
        String cycleComponentText = "\n\n" + PropertyHelper.BULLET_PREFIX
                + TextUtils.wrapMessageWithItems("Cycle component", cycleComponentRefs);

        DialogUtils.showError("Circuit has unbroken cycles."
                + pathBreakerText + cycleComponentText, VERIFICATION_RESULT_TITLE);

        return false;
    }

}
