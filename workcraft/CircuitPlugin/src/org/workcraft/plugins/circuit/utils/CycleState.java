package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.utils.SortUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CycleState {

    private final Circuit circuit;
    private final List<Contact> driverPins;
    public final List<Contact> drivenPins;
    private final Set<Contact> cycleContacts;
    private final Set<FunctionComponent> cycleComponents;

    public CycleState(Circuit circuit) {
        this.circuit = circuit;

        cycleContacts = CycleUtils.getCycledDrivers(circuit);
        // Add components to "cycle" set if they have pins on a cycle
        cycleComponents = new HashSet<>();
        for (Contact contact : cycleContacts) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                cycleComponents.add((FunctionComponent) parent);
            }
        }
        // Add zero delay gates and their pins to "cycle"sets if they are between components on a cycle
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                boolean inputOnCycle = false;
                boolean outputOnCycle = false;
                for (Contact input : component.getInputs()) {
                    Contact driver = CycleUtils.findUnbrokenPathDriverPin(circuit, input);
                    if (driver != null) {
                        inputOnCycle |= cycleComponents.contains(driver.getParent());
                    }
                }
                for (Contact output : component.getOutputs()) {
                    for (Contact driven : CycleUtils.findUnbrokenPathDrivenPins(circuit, output)) {
                        outputOnCycle |= cycleComponents.contains(driven.getParent());
                    }
                }
                if (inputOnCycle && outputOnCycle) {
                    cycleComponents.add(component);
                    cycleContacts.addAll(component.getContacts());
                }
            }
        }
        // Extend the set of cycled pins by input pins in cycled components that are driven by other cycled pins
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getInputs()) {
                if (!contact.getPathBreaker() && cycleComponents.contains(component)) {
                    Contact driver = CycleUtils.findUnbrokenPathDriverPin(circuit, contact);
                    if (cycleContacts.contains(driver)) {
                        cycleContacts.add(contact);
                    }
                }
            }
        }
        // Driver and driven pins
        driverPins = new ArrayList<>();
        drivenPins = new ArrayList<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay()) {
                driverPins.addAll(component.getOutputs());
            }
            drivenPins.addAll(component.getInputs());
        }
        SortUtils.sortNatural(driverPins, circuit::getNodeReference);
        SortUtils.sortNatural(drivenPins, circuit::getNodeReference);
    }

    public String getContactReference(FunctionContact contact) {
        return circuit.getNodeReference(contact);
    }

    public int getDriverPinCount() {
        return driverPins.size();
    }

    public int getDrivenPinCount() {
        return drivenPins.size();
    }

    public Contact getDriverPin(int index) {
        return (index >= 0) && (index < driverPins.size()) ? driverPins.get(index) : null;
    }

    public Contact getDrivenPin(int index) {
        return (index >= 0) && (index < drivenPins.size()) ? drivenPins.get(index) : null;
    }

    public boolean isInCycle(Contact contact) {
        return cycleContacts.contains(contact);
    }

    public boolean isInCycle(FunctionComponent component) {
        return cycleComponents.contains(component);
    }

    public Set<Contact> getBreakerDriverPins() {
        return driverPins.stream()
                .filter(Contact::getPathBreaker)
                .collect(Collectors.toSet());
    }

    public Set<Contact> getBreakerDrivenPins() {
        return drivenPins.stream()
                .filter(Contact::getPathBreaker)
                .collect(Collectors.toSet());
    }

    public Set<Contact> getCycleDriverPins() {
        Set<Contact> result = new HashSet<>(cycleContacts);
        result.retainAll(driverPins);
        return result;
    }

    public Set<Contact> getCycleDrivenPins() {
        Set<Contact> result = new HashSet<>(cycleContacts);
        result.retainAll(drivenPins);
        return result;
    }

    public boolean isRedundantPathBreaker(FunctionContact contact) {
        return false;
    }

}
