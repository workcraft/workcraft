package org.workcraft.plugins.stg.references;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.DefaultNameManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.ListMap;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.DialogUtils;

public class StgNameManager extends DefaultNameManager {

    public static final String INPUT_SIGNAL_PREFIX = "in";
    public static final String OUTPUT_SIGNAL_PREFIX = "out";
    public static final String INTERNAL_SIGNAL_PREFIX = "sig";
    public static final String DUMMY_PREFIX = "dum";

    private final InstanceManager instancedNameManager = new InstanceManager();
    private final ListMap<String, SignalTransition> signalTransitions = new ListMap<>();
    private final ListMap<String, DummyTransition> dummyTransitions = new ListMap<>();

    @Override
    public String getPrefix(Node node) {
        if (node instanceof SignalTransition) {
            return switch (((SignalTransition) node).getSignalType()) {
                case INPUT -> INPUT_SIGNAL_PREFIX;
                case OUTPUT -> OUTPUT_SIGNAL_PREFIX;
                case INTERNAL -> INTERNAL_SIGNAL_PREFIX;
            };
        }
        return super.getPrefix(node);
    }

    public int getInstanceNumber(Node node) {
        return instancedNameManager.getInstance(node).getSecond();
    }

    public void setInstanceNumber(Node node, int number) {
        instancedNameManager.assign(node, number);
    }

    private void setSignalTransitionName(SignalTransition st, String name, boolean forceInstance) {
        String signalName = name;
        SignalTransition.Direction direction = st.getDirection();
        Integer instance = null;
        if (!Identifier.isValid(name)) {
            final Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(name);
            signalName = r == null ? null : r.getFirst();
            if (!Identifier.isValid(signalName)) {
                throw new ArgumentException("Name '" + name + "' is not a valid signal transition label.");
            }
            direction = r.getSecond();
            instance = r.getThird();
        }
        if (isSignalName(signalName) || isUnusedName(signalName) || renameOccupantIfDifferent(st, signalName)) {
            instancedNameManager.assign(st, Pair.of(signalName + direction, instance), forceInstance);
            st.setDirectionQuiet(direction);

            String oldSignalName = st.getSignalName();
            signalTransitions.remove(oldSignalName, st);
            if (!signalName.equals(oldSignalName)) {
                st.setSignalNameQuiet(signalName);
                for (SignalTransition tt : signalTransitions.get(signalName)) {
                    st.setSignalTypeQuiet(tt.getSignalType());
                    break;
                }
            }
            signalTransitions.put(signalName, st);
            st.sendNotification(new PropertyChangedEvent(st, Model.PROPERTY_NAME));
        }
    }

    private void setDummyTransitionName(DummyTransition dt, String name, boolean forceInstance) {
        final Pair<String, Integer> r = LabelParser.parseDummyTransition(name);
        String dummyName = r == null ? null : r.getFirst();
        if (!Identifier.isValid(dummyName)) {
            throw new ArgumentException("Name '" + name + "' is not a valid dummy label.");
        }
        if (isDummyName(dummyName) || isUnusedName(dummyName) || renameOccupantIfDifferent(dt, dummyName)) {
            instancedNameManager.assign(dt, r, forceInstance);
            dummyTransitions.remove(dt.getName(), dt);
            dt.setNameQuiet(dummyName);
            dummyTransitions.put(dt.getName(), dt);
            dt.sendNotification(new PropertyChangedEvent(dt, Model.PROPERTY_NAME));
        }
    }

    private void setPlaceName(StgPlace p, String name) {
        if (!p.isImplicit()) {
            if (isUnusedName(name) || renameOccupantIfDifferent(p, name)) {
                super.setName(p, name, true);
            }
        }
    }

    private boolean renameOccupantIfDifferent(Node node, String name) {
        Node occupant = getNode(name);
        if (occupant != node) {
            if (!(occupant instanceof StgPlace)) {
                throw new ArgumentException("Name '" + name + "' is unavailable.");
            }
            String derivedName = getDerivedName(occupant, name);
            String message = "Name '" + name + "' is taken by a place.";
            String question = "\nRename that place to '" + derivedName + "' and continue?";
            if (!DialogUtils.showConfirmWarning(message, question)) {
                return false;
            }
            setName(occupant, derivedName, true);
        }
        return true;
    }

    @Override
    public void setName(Node node, String name, boolean force) {
        if (node instanceof StgPlace) {
            setPlaceName((StgPlace) node, name);
        } else if (node instanceof SignalTransition) {
            setSignalTransitionName((SignalTransition) node, name, force);
        } else if (node instanceof DummyTransition) {
            setDummyTransitionName((DummyTransition) node, name, force);
        } else {
            super.setName(node, name, force);
        }
    }

    @Override
    public String getName(Node node) {
        String result = null;
        if (node instanceof NamedTransition) {
            Pair<String, Integer> instance = instancedNameManager.getInstance(node);
            if (instance != null) {
                if (instance.getSecond().equals(0)) {
                    result = instance.getFirst();
                } else {
                    result = LabelParser.getInstancedTransitionReference(instance.getFirst(), instance.getSecond());
                }
            }
        } else if (!((node instanceof StgPlace) && ((StgPlace) node).isImplicit())) {
            // Skip implicit places
            result = super.getName(node);
        }
        return result;
    }

    @Override
    public boolean isNamed(Node node) {
        return super.isNamed(node) || (instancedNameManager.getInstance(node) != null);
    }

    @Override
    public boolean isUnusedName(String name) {
        return super.isUnusedName(name)
                && !instancedNameManager.containsGenerator(name + "-")
                && !instancedNameManager.containsGenerator(name + "+")
                && !instancedNameManager.containsGenerator(name + "~")
                && !instancedNameManager.containsGenerator(name);
    }

    @Override
    public Node getNode(String name) {
        Node result = null;
        Pair<String, Integer> instancedName = LabelParser.parseInstancedTransition(name);
        if (instancedName != null) {
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

    private Signal.Type getSignalType(String signalName) {
        for (SignalTransition st: signalTransitions.get(signalName)) {
            return st.getSignalType();
        }
        return null;
    }

    private boolean isSignalName(String name) {
        return !signalTransitions.get(name).isEmpty();
    }

    private boolean isDummyName(String name) {
        return !dummyTransitions.get(name).isEmpty();
    }

    private boolean isGoodSignalName(String name, Signal.Type type) {
        if (super.getNode(name) != null) {
            return false;
        }
        if (isDummyName(name)) {
            return false;
        }
        if (isSignalName(name)) {
            Signal.Type expectedType = getSignalType(name);
            return (expectedType == null) || (expectedType == type);
        }
        return true;
    }

    private boolean isGoodDummyName(String name) {
        return (super.getNode(name) == null) && !isSignalName(name);
    }

    private void setDefaultSignalTransitionNameIfUnnamed(final SignalTransition st) {
        if (!instancedNameManager.contains(st)) {
            String prefix = getPrefix(st);
            Integer count = getPrefixCount(prefix);
            String name = (count <= 0) ? prefix : Identifier.compose(prefix, count.toString());
            while (!isGoodSignalName(name, st.getSignalType())) {
                count++;
                name = Identifier.compose(prefix, count.toString());
            }
            setPrefixCount(prefix, count);
            st.setSignalNameQuiet(name);
            signalTransitions.put(name, st);
            if (instancedNameManager.getInstance(st) == null) {
                instancedNameManager.assign(st);
            }
        }
    }

    private void setDefaultDummyTransitionNameIfUnnamed(final DummyTransition dt) {
        if (!instancedNameManager.contains(dt)) {
            String prefix = getPrefix(dt);
            Integer count = getPrefixCount(prefix);
            String dummyName = (count <= 0) ? prefix : Identifier.compose(prefix, count.toString());
            while (!isGoodDummyName(dummyName)) {
                count++;
                dummyName = Identifier.compose(prefix, count.toString());
            }
            setPrefixCount(prefix, count);
            dt.setNameQuiet(dummyName);
            dummyTransitions.put(dummyName, dt);
            if (instancedNameManager.getInstance(dt) == null) {
                instancedNameManager.assign(dt);
            }
        }
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if (node instanceof SignalTransition) {
            setDefaultSignalTransitionNameIfUnnamed((SignalTransition) node);
        } else if (node instanceof DummyTransition) {
            setDefaultDummyTransitionNameIfUnnamed((DummyTransition) node);
        } else if (!((node instanceof StgPlace) && ((StgPlace) node).isImplicit())) {
            // Skip implicit places
            super.setDefaultNameIfUnnamed(node);
        }
    }

    @Override
    public String getDerivedName(Node node, String candidate) {
        if (node instanceof SignalTransition) {
            return candidate;
        }
        if (node instanceof DummyTransition) {
            Pair<String, Integer> r = LabelParser.parseDummyTransition(candidate);
            candidate = r.getFirst();
            if (isDummyName(candidate)) {
                return candidate;
            }
        }
        return super.getDerivedName(node, candidate);
    }

}
