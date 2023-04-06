package org.workcraft.plugins.circuit.genlib;

import java.util.*;
import java.util.stream.Collectors;

public class Library {

    private final LinkedHashMap<String, Gate> gates = new LinkedHashMap<>();

    public Library() {
    }

    public Library(Collection<Gate> gates) {
        addAll(gates);
    }

    public final void add(Gate gate) {
        gates.put(gate.name, gate);
    }

    public final void addAll(Collection<Gate> gates) {
        for (Gate gate: gates) {
            add(gate);
        }
    }

    public void remove(String name) {
        gates.remove(name);
    }

    public Gate get(String name) {
        return gates.get(name);
    }

    public void clear() {
        gates.clear();
    }

    public Set<String> getNames() {
        return gates.keySet();
    }

    public List<Gate> getGatesOrderedBySize() {
        return gates.values().stream()
                .sorted(Comparator.comparingDouble(gate -> gate.size))
                .collect(Collectors.toList());
    }

}
