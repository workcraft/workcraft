package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.circuit.*;
import org.workcraft.types.Func;
import org.workcraft.utils.DirectedGraphUtils;
import org.workcraft.utils.LogUtils;

import java.util.*;
import java.util.stream.Collectors;

public class LoopUtils {

    public static Set<FunctionComponent> clearPathBreakerComponents(Circuit circuit) {
        Set<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) {
                result.add(component);
                component.setPathBreaker(false);
            }
        }
        return result;
    }

    public static Set<FunctionContact> clearPathBreakerContacts(Circuit circuit) {
        Set<FunctionContact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionContacts()) {
                if (contact.getPathBreaker()) {
                    result.add(contact);
                    contact.setPathBreaker(false);
                }
            }
        }
        return result;
    }

    public static Collection<VisualFunctionComponent> insertLoopBreakerBuffers(VisualCircuit circuit) {
        clearPathBreakerContacts(circuit.getMathModel());
        Set<Contact> loopbreakContacts = LoopUtils.getLoopbreakContacts(circuit.getMathModel());
        return insertLoopBreakerBuffers(circuit, loopbreakContacts);
    }

    public static Collection<VisualFunctionComponent> insertLoopBreakerBuffers(VisualCircuit circuit, Set<Contact> loopbreakContacts) {
        Collection<VisualFunctionComponent> result = new HashSet<>();
        for (Contact loopbreakContact : loopbreakContacts) {
            VisualContact contact = circuit.getVisualComponent(loopbreakContact, VisualContact.class);
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            VisualFunctionComponent loopbreakGate = GateUtils.createBufferGate(circuit);
            loopbreakGate.getReferencedComponent().setPathBreaker(true);
            result.add(loopbreakGate);
            GateUtils.insertGateAfter(circuit, loopbreakGate, contact);
            GateUtils.propagateInitialState(circuit, loopbreakGate);
        }
        return result;
    }

    public static Set<Contact> getLoopbreakContacts(Circuit circuit) {
        Set<Contact> result = new HashSet<>();

        Map<Contact, Set<Contact>> graph = buildGraph(circuit);

        Set<Contact> selfloopVertices = DirectedGraphUtils.findSelfloopVertices(graph);
        for (Contact contact : selfloopVertices) {
            LogUtils.logMessage("Self-loop found: " + circuit.getNodeReference(contact));
        }
        result.addAll(selfloopVertices);

        Set<Contact> vertices = new HashSet<>(graph.keySet());
        vertices.removeAll(selfloopVertices);
        graph = DirectedGraphUtils.project(graph, vertices);

        Set<List<Contact>> cycles = DirectedGraphUtils.findSimpleCycles(graph);
        for (List<Contact> cycle : cycles) {
            Collections.reverse(cycle);
            String str = cycle.stream().map(c -> circuit.getNodeReference(c)).collect(Collectors.joining(" - "));
            LogUtils.logMessage("Simple cycle found: " + str);
        }

        Set<Contact> loopbreakVertices = findLoopbreakerVertices(cycles, v -> circuit.getNodeReference(v));
        result.addAll(loopbreakVertices);

        return result;
    }

    private static Map<Contact, Set<Contact>> buildGraph(Circuit circuit) {
        Map<Contact, Set<Contact>> contactToDriversMap = new HashMap<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) continue;
            HashSet<Contact> drivers = new HashSet<>();
            for (Contact contact : component.getInputs()) {
                Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                if (driver.isPin()) {
                    drivers.add(driver);
                }
            }
            for (Contact contact : component.getOutputs()) {
                contactToDriversMap.put(contact, drivers);
            }
        }
        return contactToDriversMap;
    }

    private static Set<Contact> findLoopbreakerVertices(Set<List<Contact>> cycles, Func<Contact, String> a) {
        Set<Contact> result = new HashSet<>();
        Map<Contact, Set<List<Contact>>> vertexCyclesMap = buildVertexCycleMap(cycles);

        while (!vertexCyclesMap.isEmpty()) {
            Contact bestVertex = null;
            int bestCycleCount = 0;
            for (Contact vertex : vertexCyclesMap.keySet()) {
                Set<List<Contact>> vertexCycles = vertexCyclesMap.get(vertex);
                int cycleCount = vertexCycles.size();
                if (cycleCount > bestCycleCount) {
                    bestCycleCount = cycleCount;
                    bestVertex = vertex;
                }
            }
            if (bestVertex != null) {
                result.add(bestVertex);
                LogUtils.logMessage("Contact  " + a.eval(bestVertex) + " breaks " + bestCycleCount + " cycle(s)");
                Set<List<Contact>> bestContactCycles = vertexCyclesMap.get(bestVertex);
                Map<Contact, Set<List<Contact>>> newContactCyclesMap = new HashMap<>();
                for (Contact contact : vertexCyclesMap.keySet()) {
                    Set<List<Contact>> contactCycles = new HashSet<>(vertexCyclesMap.get(contact));
                    contactCycles.removeAll(bestContactCycles);
                    if (!contactCycles.isEmpty()) {
                        newContactCyclesMap.put(contact, contactCycles);
                    }
                }
                vertexCyclesMap = newContactCyclesMap;
            }
        }
        return result;
    }

    private static Map<Contact, Set<List<Contact>>> buildVertexCycleMap(Set<List<Contact>> cycles) {
        Map<Contact, Set<List<Contact>>> vertexCyclesMap = new HashMap<>();
        for (List<Contact> cycle : cycles) {
            for (Contact vertex : cycle) {
                Set<List<Contact>> vertexCycles = vertexCyclesMap.get(vertex);
                if (vertexCycles == null) {
                    vertexCycles = new HashSet<>();
                    vertexCyclesMap.put(vertex, vertexCycles);
                }
                vertexCycles.add(cycle);
            }
        }
        return vertexCyclesMap;
    }

}
