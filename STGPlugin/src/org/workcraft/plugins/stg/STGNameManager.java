package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Identifier;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class STGNameManager extends UniqueNameManager {
    private InstanceManager instancedNameManager = new InstanceManager();
    private ListMap<String, SignalTransition> signalTransitions = new ListMap<String, SignalTransition>();
    private ListMap<String, DummyTransition> dummyTransitions = new ListMap<String, DummyTransition>();

    public int getInstanceNumber(Node node) {
        return instancedNameManager.getInstance(node).getSecond();
    }

    public void setInstanceNumber(Node node, int number) {
        instancedNameManager.assign(node, number);
    }

    public Collection<SignalTransition> getSignalTransitions(String signalName) {
        return signalTransitions.get(signalName);
    }

    public Collection<DummyTransition> getDummyTransitions(String name) {
        return dummyTransitions.get(name);
    }

    private void renameSignalTransition(SignalTransition t, String signalName) {
        signalTransitions.remove(t.getSignalName(), t);
        t.setSignalName(signalName);
        signalTransitions.put(t.getSignalName(), t);
    }

    private void renameDummyTransition(DummyTransition t, String name) {
        dummyTransitions.remove(t.getName(), t);
        t.setName(name);
        dummyTransitions.put(t.getName(), t);
    }

    private void setSignalTransitionName(SignalTransition st, String name, boolean forceInstance) {
        String signalName = st.getSignalName();
        Direction direction = st.getDirection();
        Integer instance = 0;
        final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(name);
        if (r != null) {
            signalName = r.getFirst();
            direction = r.getSecond();
            instance = r.getThird();
        } else if (Identifier.isValid(name)) {
            signalName = name;
        } else {
            throw new ArgumentException("Name '" + name + "' is not a valid signal transition label.");
        }
        if (isUnusedName(signalName) || !getSignalTransitions(signalName).isEmpty()) {
            instancedNameManager.assign(st, Pair.of(signalName + direction, instance), forceInstance);
            st.setDirection(direction);
            renameSignalTransition(st, signalName);
        } else {
            throw new ArgumentException("Name '" + name + "' is unavailable.");
        }
    }

    private void setDummyTransitionName(DummyTransition dt, String name, boolean forceInstance) {
        final Pair<String,Integer> r = LabelParser.parseDummyTransition(name);
        if (r != null) {
            String dummyName = r.getFirst();
            if (isUnusedName(dummyName) || !getDummyTransitions(dummyName).isEmpty()) {
                instancedNameManager.assign(dt, r, forceInstance);
                renameDummyTransition(dt, dummyName);
            } else {
                throw new ArgumentException("Name '" + name + "' is unavailable.");
            }
        } else {
            throw new ArgumentException("Name '" + name + "' is not a valid dummy label.");
        }
    }

    public void setName(Node node, String name, boolean forceInstance) {
        if ((node instanceof STGPlace) && ((STGPlace)node).isImplicit()) {
            // Skip implicit places
        } else if (node instanceof SignalTransition) {
            setSignalTransitionName((SignalTransition)node, name, forceInstance);
        } else if (node instanceof DummyTransition) {
            setDummyTransitionName((DummyTransition)node, name, forceInstance);
        } else {
            super.setName(node, name);
        }
    }

    @Override
    public void setName(Node node, String s) {
        setName(node, s, false);
    }

    @Override
    public String getName(Node node) {
        String result = null;
        if ((node instanceof STGPlace) && ((STGPlace)node).isImplicit()) {
            // Skip implicit places
        } else if (node instanceof NamedTransition) {
            Pair<String, Integer> instance = instancedNameManager.getInstance(node);
            if (instance != null) {
                if (instance.getSecond().equals(0)) {
                    result = instance.getFirst();
                } else {
                    result = instance.getFirst() + "/" + instance.getSecond();
                }
            }
        } else {
            result = super.getName(node);
        }
        return result;
    }

    public Pair<String, Integer> getNamePair(Node node) {
        if (node instanceof Transition) {
            return instancedNameManager.getInstance(node);
        }
        return null;
    }

    @Override
    public boolean isNamed(Node node) {
        return (super.isNamed(node)
                || (instancedNameManager.getInstance(node) != null));
    }

    @Override
    public boolean isUnusedName(String name) {
    return (super.isUnusedName(name)
            && !instancedNameManager.containsGenerator(name + "-")
            && !instancedNameManager.containsGenerator(name + "+")
            && !instancedNameManager.containsGenerator(name + "~")
            && !instancedNameManager.containsGenerator(name));
    }

    @Override
    public Node getNode(String name) {
        Node result = null;
        Pair<String, Integer> instancedName = LabelParser.parseInstancedTransition(name);
        if (instancedName != null)    {
            if (instancedName.getSecond() == null) {
                instancedName = Pair.of(instancedName.getFirst(), 0);
            }
            result = instancedNameManager.getObject(instancedName);
        }
        if (result == null) {
            result = super.getNode(name);
        }
        return result;
    }

    @Override
    public void remove(Node node) {
        super.remove(node);
        if (instancedNameManager.getInstance(node) != null) {
            instancedNameManager.remove(node);
        }
    }

    private SignalTransition.Type getSignalType(String signalName) {
        for (SignalTransition st : getSignalTransitions(signalName)) {
            return st.getSignalType();
        }
        return null;
    }

    private boolean isSignalName(String name) {
        return !getSignalTransitions(name).isEmpty();
    }

    private boolean isDummyName(String name) {
        return !getDummyTransitions(name).isEmpty();
    }

    private boolean isGoodSignalName(String name, SignalTransition.Type type) {
        boolean result = true;
        if (super.getNode(name) != null) {
            result = false;
        } else if (isDummyName(name)) {
            result = false;
        } else if (isSignalName(name)) {
            SignalTransition.Type expectedType = getSignalType(name);
            if ((expectedType != null) && !expectedType.equals(type)) {
                result = false;
            }
        }
        return result;
    }

    private boolean isGoodDummyName(String name) {
        boolean result = true;
        if (super.getNode(name) != null) {
            result = false;
        } else if (isSignalName(name)) {
            result = false;
        } else if (isDummyName(name)) {
            result = false;
        }
        return result;
    }

    private void setDeaultSignalTransitionNameIfUnnamed(final SignalTransition st) {
        if (!instancedNameManager.contains(st)) {
            String prefix = getPrefix(st);
            Integer count = getPrefixCount(prefix);
            String name = prefix;
            if (count > 0) {
                name = prefix + count;
            }
            while ( !isGoodSignalName(name, st.getSignalType()) ) {
                name = prefix + (++count);
            }
            setPrefixCount(prefix, count);
            st.setSignalName(name);
            signalTransitions.put(name, st);
            instancedNameManager.assign(st);
        }
    }

    private void setDefaultDummyTransitionNameIfUnnamed(final DummyTransition dt) {
        if (!instancedNameManager.contains(dt)) {
            String prefix = getPrefix(dt);
            Integer count = getPrefixCount(prefix);
            String name;
            do {
                name = prefix + (count++);
            } while ( !isGoodDummyName(name) );
            dt.setName(name);
            dummyTransitions.put(name, dt);
            instancedNameManager.assign(dt);
        }
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if ((node instanceof STGPlace) && ((STGPlace)node).isImplicit()) {
            // Skip implicit places
        } else if (node instanceof SignalTransition) {
            setDeaultSignalTransitionNameIfUnnamed((SignalTransition)node);
        } else if (node instanceof DummyTransition) {
            setDefaultDummyTransitionNameIfUnnamed((DummyTransition)node);
        } else {
            super.setDefaultNameIfUnnamed(node);
        }
    }

    @Override
    public String getDerivedName(Node node, String candidate) {
        String result = candidate;
        if (!(node instanceof NamedTransition)) {
            result = super.getDerivedName(node, candidate);
        }
        return result;
    }

}
