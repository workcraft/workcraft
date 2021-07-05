package org.workcraft.plugins.stg.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.IDGenerator;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.types.GeneralTwoWayMap;
import org.workcraft.types.Pair;
import org.workcraft.types.TwoWayMap;

import java.util.HashMap;
import java.util.Map;

public class InstanceManager {

    private final GeneralTwoWayMap<Node, Pair<String, Integer>> instances = new TwoWayMap<>();
    private final Map<String, IDGenerator> generators = new HashMap<>();

    private IDGenerator getGenerator(String label) {
        IDGenerator result = generators.get(label);
        if (result == null) {
            result = new IDGenerator();
            generators.put(label, result);
        }
        return result;
    }

    public boolean containsGenerator(String name) {
        IDGenerator generator = generators.get(name);
        return generator != null;
    }

    public boolean contains(Node node) {
        return instances.containsKey(node);
    }

    /**
     * Automatically assign a new name to <i>t</i>, taking the name from label getter and auto-generating instance number.
     */
    public void assign(Node node) {
        final Pair<String, Integer> assigned = instances.getValue(node);
        if (assigned != null) {
            throw new ArgumentException("Instance already assigned to '"
                    + getLabel(node) + "/" + assigned.getSecond() + "'");
        }
        final String label = getLabel(node);
        final Integer instance = getGenerator(label).getNextID();
        instances.put(node, Pair.of(label, instance));
    }

    /**
     * Manually assign a new name to <i>t</i>, auto-generating instance number.
     */
    public void assign(Node node, String name) {
        assign(node, Pair.of(name, null), false);
    }

    /**
     * Manually assign an instance number to <i>t</i>.
     */
    public void assign(Node node, int instance) {
        assign(node, Pair.of(getLabel(node), instance), true);
    }

    /**
     * Manually assign a full reference to <i>t</i>, either auto-generating (<i>forceInstance = false</i>)
     * or forcing (<i>forceInstance = true</i>) the instance number.
     */
    public void assign(Node node, Pair<String, Integer> reference, boolean forceInstance) {
        final Pair<String, Integer> assigned = instances.getValue(node);
        if ((reference.getSecond() == null) || !forceInstance) {
            if (assigned != null) {
                if (assigned.getFirst().equals(reference.getFirst())) {
                    // already registered with same name, just return
                    return;
                } else {
                    // release old instance
                    remove(node);
                }
            }
            IDGenerator generator = getGenerator(reference.getFirst());
            Pair<String, Integer> assignment = Pair.of(reference.getFirst(), generator.getNextID());
            instances.put(node, assignment);
        } else {
            // check if desired instance is already taken
            final Node refHolder = instances.getKey(reference);
            if (refHolder == node) {
                // requested instance already taken by t, do nothing
                return;
            } else if (refHolder != null) {
                // requested instance taken by somebody else
                throw new ArgumentException("Transition '" + reference.getFirst() + "' with the same instance number "
                        + reference.getSecond() + " already exists");
            } else if (assigned != null) {
                // release old instance
                remove(node);
            }
            instances.put(node, reference);
            getGenerator(reference.getFirst()).reserveID(reference.getSecond());
        }
    }

    public Pair<String, Integer> getInstance(Node node) {
        return instances.getValue(node);
    }

    public Node getObject(Pair<String, Integer> ref) {
        return instances.getKey(ref);
    }

    public void remove(Node node) {
        final Pair<String, Integer> assignment = instances.getValue(node);
        if (assignment == null) {
            throw new NotFoundException("Instance not assigned");
        }

        IDGenerator generator = generators.get(assignment.getFirst());
        generator.releaseID(assignment.getSecond());
        if (generator.isEmpty()) {
            generators.remove(assignment.getFirst());
        }

        instances.removeKey(node);
    }

    public String getLabel(Node node) {
        if (node instanceof SignalTransition) {
            SignalTransition st = (SignalTransition) node;
            return st.getSignalName() + st.getDirection();
        } else if (node instanceof DummyTransition) {
            DummyTransition dum = (DummyTransition) node;
            return dum.getName();
        } else {
            throw new RuntimeException("Unexpected class " + node.getClass().getName());
        }
    }

}
