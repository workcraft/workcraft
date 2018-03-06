package org.workcraft.plugins.stg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.DirectionPropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.InstancePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalPropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.TypePropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.MultiSet;
import org.workcraft.util.Pair;
import org.workcraft.util.SetUtils;
import org.workcraft.util.Triple;

@VisualClass(org.workcraft.plugins.stg.VisualStg.class)
public class Stg extends AbstractMathModel implements StgModel {

    private final StgReferenceManager referenceManager;

    public Stg() {
        this(null, null);
    }

    public Stg(Container root) {
        this(root, null);
    }

    public Stg(Container root, References refs) {
        super(root, new StgReferenceManager(refs));
        referenceManager = (StgReferenceManager) getReferenceManager();
        new SignalTypeConsistencySupervisor(this).attach(getRoot());
    }

    public final Place createPlace() {
        return createPlace(null, null);
    }

    public final StgPlace createPlace(String ref, Container container) {
        return createNodeWithHierarchy(ref, container, StgPlace.class);
    }

    public final DummyTransition createDummyTransition(String ref, Container container) {
        return createDummyTransition(ref, container, false);
    }

    public final DummyTransition createDummyTransition(String ref, Container container, boolean forceInstance) {
        DummyTransition transition = createNodeWithHierarchy(ref, container, DummyTransition.class);
        if (ref == null) {
            ref = transition.getName();
        }
        String name = NamespaceHelper.getReferenceName(ref);
        setName(transition, name, forceInstance);
        return transition;
    }

    public final SignalTransition createSignalTransition(String ref, Container container) {
        return createSignalTransition(ref, container, false);
    }

    public final SignalTransition createSignalTransition(String ref, Container container, boolean forceInstance) {
        SignalTransition transition = createNodeWithHierarchy(ref, container, SignalTransition.class);
        if (ref == null) {
            ref = transition.getName();
        }
        String name = NamespaceHelper.getReferenceName(ref);
        setName(transition, name, forceInstance);
        return transition;
    }

    @Override
    public boolean isEnabled(Transition t) {
        return PetriNet.isEnabled(this, t);
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return PetriNet.isUnfireEnabled(this, t);
    }

    @Override
    public final void fire(Transition t) {
        PetriNet.fire(this, t);
    }

    @Override
    public final void unFire(Transition t) {
        PetriNet.unFire(this, t);
    }

    @Override
    public final Collection<SignalTransition> getSignalTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
    }

    @Override
    public final Collection<Place> getPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
    }

    @Override
    public final Collection<StgPlace> getMutexPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), StgPlace.class, place -> place.isMutex());
    }

    @Override
    public final Collection<Connection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), Connection.class);
    }

    @Override
    public final Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    public Collection<DummyTransition> getDummyTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), DummyTransition.class);
    }

    @Override
    public Collection<SignalTransition> getSignalTransitions(final Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class,
                transition -> transition.getSignalType() == type);
    }

    public Set<String> getSignalNames(Container container) {
        if (container == null) {
            container = getRoot();
        }
        Set<String> result = new HashSet<>();
        for (SignalTransition st: Hierarchy.getChildrenOfType(container, SignalTransition.class)) {
            result.add(st.getSignalName());
        }
        return result;
    }

    public Set<String> getSignalNames(final Type type, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Set<String> result = new HashSet<>();
        for (SignalTransition st: getSignalTransitions(type, container)) {
            result.add(st.getSignalName());
        }
        return result;
    }

    public Collection<SignalTransition> getSignalTransitions(final Type type, Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, SignalTransition.class,
                transition -> type.equals(transition.getSignalType()));
    }

    public Collection<SignalTransition> getSignalTransitions(final String signalName, Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, SignalTransition.class,
                transition -> signalName.equals(transition.getSignalName()));
    }

    @Override
    public Set<String> getDummyReferences() {
        Set<String> result = new HashSet<>();
        for (DummyTransition t: getDummyTransitions()) {
            result.add(getDummyReference(t));
        }
        return result;
    }

    public String getDummyReference(DummyTransition t) {
        String reference = referenceManager.getNodeReference(null, t);
        String path = NamespaceHelper.getReferencePath(reference);
        return path + t.getName();
    }

    @Override
    public Set<String> getSignalReferences() {
        Set<String> result = new HashSet<>();
        for (SignalTransition t: getSignalTransitions()) {
            result.add(getSignalReference(t));
        }
        return result;
    }

    @Override
    public Set<String> getSignalReferences(Type type) {
        Set<String> result = new HashSet<>();
        for (SignalTransition t: getSignalTransitions(type)) {
            result.add(getSignalReference(t));
        }
        return result;
    }

    public String getSignalReference(SignalTransition t) {
        String reference = referenceManager.getNodeReference(null, t);
        String path = NamespaceHelper.getReferencePath(reference);
        return path + t.getSignalName();
    }

    @Override
    public int getInstanceNumber(Node st) {
        return referenceManager.getInstanceNumber(st);
    }

    @Override
    public void setInstanceNumber(Node st, int number) {
        referenceManager.setInstanceNumber(st, number);
    }

    public Direction getDirection(Node t) {
        Direction result = null;
        String name = referenceManager.getName(t);
        if (name != null) {
            result = LabelParser.parseSignalTransition(name).getSecond();
        }
        return result;
    }

    public void setDirection(Node t, Direction direction) {
        String name = referenceManager.getName(t);
        Triple<String, Direction, Integer> old = LabelParser.parseSignalTransition(name);
        referenceManager.setName(t, old.getFirst() + direction.toString());
    }

    @Override
    public String getName(Node node) {
        return referenceManager.getName(node);
    }

    @Override
    public void setName(Node node, String name) {
        this.setName(node, name, false);
    }

    private void setName(Node node, String name, boolean forceInstance) {
        referenceManager.setName(node, name, forceInstance);
    }

    public Collection<SignalTransition> getSignalTransitions(String signalReference) {
        String parentReference = NamespaceHelper.getParentReference(signalReference);
        Node parent = getNodeByReference(null, parentReference);
        Container container = (parent instanceof Container) ? (Container) parent : null;
        String signalName = NamespaceHelper.getReferenceName(signalReference);
        return getSignalTransitions(signalName, container);
    }

    public Type getSignalType(String signalReference) {
        Type type = null;
        Collection<SignalTransition> transitions = getSignalTransitions(signalReference);
        if (!transitions.isEmpty()) {
            type = transitions.iterator().next().getSignalType();
        }
        return type;
    }

    public void setSignalType(String signalReference, Type signalType) {
        for (SignalTransition transition: getSignalTransitions(signalReference)) {
            transition.setSignalType(signalType);
            // It is sufficient to change the type of a single transition
            // - all the others will be notified.
            break;
        }
    }

    public Type getSignalType(String signalName, Container container) {
        Type type = null;
        Collection<SignalTransition> transitions = getSignalTransitions(signalName, container);
        if (!transitions.isEmpty()) {
            type = transitions.iterator().next().getSignalType();
        }
        return type;
    }

    public void setSignalType(String signalName, Type signalType, Container container) {
        for (SignalTransition transition: getSignalTransitions(signalName, container)) {
            transition.setSignalType(signalType);
            // It is sufficient to change the type of a single transition
            // - all the others will be notified.
            break;
        }
    }

    public ConnectionResult connect(Node first, Node second)
            throws InvalidConnectionException {
        if ((first instanceof Transition) && (second instanceof Transition)) {
            StgPlace p = new StgPlace();
            p.setImplicit(true);

            MathConnection con1 = new MathConnection((Transition) first, p);
            MathConnection con2 = new MathConnection(p, (Transition) second);

            Hierarchy.getNearestContainer(first, second).add(
                    Arrays.asList(new Node[] {p, con1, con2 }));

            return new ComplexResult(p, con1, con2);
        } else if (first instanceof Place && second instanceof Place) {
            throw new InvalidConnectionException("Connections between places are not valid");
        } else {
            MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
            Hierarchy.getNearestContainer(first, second).add(con);
            return new SimpleResult(con);
        }
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        if (node instanceof StgPlace) {
            if (node != null && ((StgPlace) node).isImplicit()) {
                Set<Node> preset = getPreset(node);
                Set<Node> postset = getPostset(node);
                if (!(preset.size() == 1 && postset.size() == 1)) {
                    throw new RuntimeException("An implicit place must have one transition in its preset and one transition in its postset.");
                }
                String predNodeRef = referenceManager.getNodeReference(null, preset.iterator().next());
                String succNodeRef = referenceManager.getNodeReference(null, postset.iterator().next());
                return "<" + predNodeRef + "," + succNodeRef + ">";
            }
        }
        return super.getNodeReference(provider, node);
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        Pair<String, String> implicitPlaceTransitions = LabelParser.parseImplicitPlaceReference(reference);
        if (implicitPlaceTransitions != null) {
            Node t1 = referenceManager.getNodeByReference(provider, implicitPlaceTransitions.getFirst());

            Node t2 = referenceManager.getNodeByReference(provider, implicitPlaceTransitions.getSecond());
            if ((t1 != null) && (t2 != null)) {
                Set<Node> implicitPlaceCandidates = SetUtils.intersection(getPreset(t2), getPostset(t1));

                for (Node node: implicitPlaceCandidates) {
                    if ((node instanceof StgPlace) && ((StgPlace) node).isImplicit()) {
                        return node;
                    }
                }
            }
            throw new NotFoundException("Implicit place between "
                    + implicitPlaceTransitions.getFirst() + " and "
                    + implicitPlaceTransitions.getSecond() + " does not exist.");
        } else {
            return super.getNodeByReference(provider, reference);
        }
    }

    public void makeExplicit(StgPlace implicitPlace) {
        implicitPlace.setImplicit(false);
        referenceManager.setDefaultNameIfUnnamed(implicitPlace);
    }

    @Override
    public <T extends MathNode> T createMergedNode(Collection<MathNode> srcNodes, Container container, Class<T> type) {
        T result = super.createMergedNode(srcNodes, container, type);
        if (result instanceof SignalTransition) {
            SignalTransition signalTransition = (SignalTransition) result;
            // Type priority: OUTPUT > INPUT > INTERNAL
            boolean foundOutput = false;
            boolean foundInput = false;
            boolean foundInternal = false;
            // Direction priority: TOGGLE > PLUS == MINUS
            boolean foundToggle = false;
            boolean foundPlus = false;
            boolean foundMinus = false;
            for (MathNode srcNode: srcNodes) {
                if (srcNode instanceof SignalTransition) {
                    SignalTransition srcSignalTransition = (SignalTransition) srcNode;
                    if (srcSignalTransition.getSignalType() == Type.OUTPUT) {
                        foundOutput = true;
                    }
                    if (srcSignalTransition.getSignalType() == Type.INPUT) {
                        foundInput = true;
                    }
                    if (srcSignalTransition.getSignalType() == Type.INTERNAL) {
                        foundInternal = true;
                    }
                    if (srcSignalTransition.getDirection() == Direction.TOGGLE) {
                        foundToggle = true;
                    }
                    if (srcSignalTransition.getDirection() == Direction.PLUS) {
                        foundPlus = true;
                    }
                    if (srcSignalTransition.getDirection() == Direction.MINUS) {
                        foundMinus = true;
                    }
                }
            }
            if (foundOutput) {
                signalTransition.setSignalType(Type.OUTPUT);
            } else if (foundInput) {
                signalTransition.setSignalType(Type.INPUT);
            } else if (foundInternal) {
                signalTransition.setSignalType(Type.INTERNAL);
            }
            if (foundToggle || (foundPlus && foundMinus)) {
                setDirection(signalTransition, Direction.TOGGLE);
            } else if (foundPlus) {
                setDirection(signalTransition, Direction.PLUS);
            } else if (foundMinus) {
                setDirection(signalTransition, Direction.MINUS);
            }
        }
        return result;
    }

    @Override
    public boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        if (referenceManager == null) {
            return false;
        }
        NamespaceProvider dstProvider = null;
        if (dstContainer instanceof NamespaceProvider) {
            dstProvider = (NamespaceProvider) dstContainer;
        } else {
            dstProvider = referenceManager.getNamespaceProvider(dstContainer);
        }
        NameManager dstNameManager = referenceManager.getNameManager(dstProvider);
        for (Node srcChild: srcChildren) {
            if (srcChild instanceof SignalTransition) {
                SignalTransition srcTransition = (SignalTransition) srcChild;
                String signalName = srcTransition.getSignalName();
                if (dstNameManager.isUnusedName(signalName)) continue;
                // Check for name clash with non-signal nodes.
                String dstContainerRef = getNodeReference(dstContainer);
                String dstSignalRef = NamespaceHelper.getReference(dstContainerRef, signalName);
                String srcTransitionName = srcTransition.getName();
                if (getSignalTransitions(dstSignalRef).isEmpty()) {
                    DialogUtils.showError("Cannot move transition '" + srcTransitionName
                            + "' because the name '" + signalName + "' is taken at the destination.");
                    return false;
                }
                // Check for name clash with a signal of different type.
                Type srcSignalType = srcTransition.getSignalType();
                Type dstSignalType = getSignalType(dstSignalRef);
                if (srcSignalType != dstSignalType) {
                    DialogUtils.showError("Cannot move an " + srcSignalType
                            + " transition '" + srcTransitionName + "' because there is an "
                            + dstSignalType + " signal '" + signalName + "' at the destination.");
                    return false;
                }
            }
        }
        return super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node != null) {
            if (node instanceof StgPlace) {
                StgPlace place = (StgPlace) node;
                if (place.isImplicit()) {
                    properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
                }
            } else if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
                properties.add(new TypePropertyDescriptor(this, transition));
                properties.add(new SignalPropertyDescriptor(this, transition));
                properties.add(new DirectionPropertyDescriptor(this, transition));
                if (StgSettings.getShowTransitionInstance()) {
                    properties.add(new InstancePropertyDescriptor(this, transition));
                }
            } else if (node instanceof DummyTransition) {
                DummyTransition dummy = (DummyTransition) node;
                if (StgSettings.getShowTransitionInstance()) {
                    properties.add(new InstancePropertyDescriptor(this, dummy));
                }
            }
        }
        return properties;
    }

    @Override
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Place", getPlaces().size());
        result.add("Transition", getTransitions().size());
        result.add("Arc", getConnections().size());
        return result;
    }

}
