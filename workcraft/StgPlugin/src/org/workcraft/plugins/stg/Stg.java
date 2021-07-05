package org.workcraft.plugins.stg;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.observers.SignalTypeConsistencySupervisor;
import org.workcraft.plugins.stg.references.StgReferenceManager;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.serialisation.References;
import org.workcraft.types.MultiSet;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.SetUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@VisualClass(VisualStg.class)
public class Stg extends AbstractMathModel implements StgModel {

    private FileReference refinement = null;

    public Stg() {
        this(null, null);
    }

    public Stg(Container root, References refs) {
        super(root, new StgReferenceManager(refs));
        new SignalTypeConsistencySupervisor(this).attach(getRoot());
    }

    @Override
    public StgReferenceManager getReferenceManager() {
        return (StgReferenceManager) super.getReferenceManager();
    }

    public final StgPlace createPlace() {
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
        getReferenceManager().setName(transition, name, forceInstance);
        return transition;
    }

    public SignalTransition createSignalTransition(String signalRef, SignalTransition.Direction direction, Container container) {
        String ref = null;
        if ((signalRef != null) && (direction != null)) {
            ref = signalRef + direction;
        }
        return createSignalTransition(ref, container);
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
        getReferenceManager().setName(transition, name, forceInstance);
        return transition;
    }

    @Override
    public boolean isEnabled(Transition t) {
        return Petri.isEnabled(this, t);
    }

    @Override
    public final void fire(Transition t) {
        Petri.fire(this, t);
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return Petri.isUnfireEnabled(this, t);
    }

    @Override
    public final void unFire(Transition t) {
        Petri.unFire(this, t);
    }

    @Override
    public final Collection<SignalTransition> getSignalTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
    }

    @Override
    public final Collection<StgPlace> getPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), StgPlace.class);
    }

    @Override
    public final Collection<StgPlace> getMutexPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), StgPlace.class, StgPlace::isMutex);
    }

    @Override
    public final Collection<MathConnection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), MathConnection.class);
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
    public Collection<SignalTransition> getSignalTransitions(final Signal.Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class,
                transition -> (transition.getSignalType() == type) || (type == null));
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

    public Set<String> getSignalNames(final Signal.Type type, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Set<String> result = new HashSet<>();
        for (SignalTransition st: getSignalTransitions(type, container)) {
            result.add(st.getSignalName());
        }
        return result;
    }

    public Collection<SignalTransition> getSignalTransitions(final Signal.Type type, Container container) {
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

    public String getDummyReference(DummyTransition dummyTransition) {
        String ref = getReferenceManager().getNodeReference(null, dummyTransition);
        String parentRef = NamespaceHelper.getParentReference(ref);
        return NamespaceHelper.getReference(parentRef, dummyTransition.getName());
    }

    @Override
    public Set<String> getSignalReferences() {
        Set<String> result = new HashSet<>();
        for (SignalTransition t : getSignalTransitions()) {
            result.add(getSignalReference(t));
        }
        return result;
    }

    @Override
    public Set<String> getSignalReferences(Signal.Type type) {
        Set<String> result = new HashSet<>();
        for (SignalTransition t : getSignalTransitions(type)) {
            result.add(getSignalReference(t));
        }
        return result;
    }

    @Override
    public String getSignalReference(SignalTransition signalTransition) {
        String ref = getReferenceManager().getNodeReference(null, signalTransition);
        String parentRef = NamespaceHelper.getParentReference(ref);
        return NamespaceHelper.getReference(parentRef, signalTransition.getSignalName());
    }

    @Override
    public int getInstanceNumber(NamedTransition namedTransition) {
        return getReferenceManager().getInstanceNumber(namedTransition);
    }

    @Override
    public void setInstanceNumber(NamedTransition namedTransition, int number) {
        getReferenceManager().setInstanceNumber(namedTransition, number);
    }

    public SignalTransition.Direction getDirection(SignalTransition signalTransition) {
        SignalTransition.Direction result = null;
        String name = getReferenceManager().getName(signalTransition);
        if (name != null) {
            result = LabelParser.parseSignalTransition(name).getSecond();
        }
        return result;
    }

    public void setDirection(SignalTransition signalTransition, SignalTransition.Direction direction) {
        String name = getReferenceManager().getName(signalTransition);
        Triple<String, SignalTransition.Direction, Integer> old = LabelParser.parseSignalTransition(name);
        getReferenceManager().setName(signalTransition, old.getFirst() + direction.toString());
    }

    @Override
    public String getName(Node node) {
        return getReferenceManager().getName(node);
    }

    @Override
    public void setName(Node node, String name) {
        getReferenceManager().setName(node, name, true);
    }

    public Collection<SignalTransition> getSignalTransitions(String signalReference) {
        String parentReference = NamespaceHelper.getParentReference(signalReference);
        Node parent = getNodeByReference(null, parentReference);
        Container container = (parent instanceof Container) ? (Container) parent : null;
        String signalName = NamespaceHelper.getReferenceName(signalReference);
        return getSignalTransitions(signalName, container);
    }

    public Signal.Type getSignalType(String signalReference) {
        Signal.Type type = null;
        Collection<SignalTransition> transitions = getSignalTransitions(signalReference);
        if (!transitions.isEmpty()) {
            type = transitions.iterator().next().getSignalType();
        }
        return type;
    }

    public void setSignalType(String signalReference, Signal.Type signalType) {
        for (SignalTransition transition: getSignalTransitions(signalReference)) {
            transition.setSignalType(signalType);
            // It is sufficient to change the type of a single transition
            // - all the others will be notified.
            break;
        }
    }

    public Signal.Type getSignalType(String signalName, Container container) {
        Signal.Type type = null;
        Collection<SignalTransition> transitions = getSignalTransitions(signalName, container);
        if (!transitions.isEmpty()) {
            type = transitions.iterator().next().getSignalType();
        }
        return type;
    }

    public void setSignalType(String signalName, Signal.Type signalType, Container container) {
        for (SignalTransition transition: getSignalTransitions(signalName, container)) {
            transition.setSignalType(signalType);
            // It is sufficient to change the type of a single transition
            // - all the others will be notified.
            break;
        }
    }

    public ImplicitPlaceConnection connect(NamedTransition first, NamedTransition second)
            throws InvalidConnectionException {

        StgPlace p = new StgPlace();
        p.setImplicit(true);

        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(p);

        MathConnection con1 = connect(first, p);
        MathConnection con2 = connect(p, second);
        return new ImplicitPlaceConnection(p, con1, con2);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        if (node instanceof StgPlace) {
            StgPlace place = (StgPlace) node;
            if (place.isImplicit()) {
                Set<MathNode> preset = getPreset(place);
                Set<MathNode> postset = getPostset(place);
                if (!(preset.size() == 1 && postset.size() == 1)) {
                    throw new RuntimeException("An implicit place must have one transition in its preset and one transition in its postset.");
                }
                String predNodeRef = getReferenceManager().getNodeReference(null, preset.iterator().next());
                String succNodeRef = getReferenceManager().getNodeReference(null, postset.iterator().next());
                return "<" + predNodeRef + "," + succNodeRef + ">";
            }
        }
        return super.getNodeReference(provider, node);
    }

    @Override
    public MathNode getNodeByReference(NamespaceProvider provider, String reference) {
        Pair<String, String> implicitPlaceTransitions = LabelParser.parseImplicitPlace(reference);
        if (implicitPlaceTransitions != null) {
            Node predNode = getReferenceManager().getNodeByReference(provider, implicitPlaceTransitions.getFirst());
            Node succNode = getReferenceManager().getNodeByReference(provider, implicitPlaceTransitions.getSecond());
            if ((predNode instanceof Transition) && (succNode instanceof  Transition)) {
                Set<MathNode> predPostset = getPostset((Transition) predNode);
                Set<MathNode> succPreset = getPreset((Transition) succNode);
                Set<MathNode> implicitPlaceCandidates = SetUtils.intersection(succPreset, predPostset);
                for (MathNode node: implicitPlaceCandidates) {
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
        getReferenceManager().setDefaultNameIfUnnamed(implicitPlace);
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
                    if (srcSignalTransition.getSignalType() == Signal.Type.OUTPUT) {
                        foundOutput = true;
                    }
                    if (srcSignalTransition.getSignalType() == Signal.Type.INPUT) {
                        foundInput = true;
                    }
                    if (srcSignalTransition.getSignalType() == Signal.Type.INTERNAL) {
                        foundInternal = true;
                    }
                    if (srcSignalTransition.getDirection() == SignalTransition.Direction.TOGGLE) {
                        foundToggle = true;
                    }
                    if (srcSignalTransition.getDirection() == SignalTransition.Direction.PLUS) {
                        foundPlus = true;
                    }
                    if (srcSignalTransition.getDirection() == SignalTransition.Direction.MINUS) {
                        foundMinus = true;
                    }
                }
            }
            if (foundOutput) {
                signalTransition.setSignalType(Signal.Type.OUTPUT);
            } else if (foundInput) {
                signalTransition.setSignalType(Signal.Type.INPUT);
            } else if (foundInternal) {
                signalTransition.setSignalType(Signal.Type.INTERNAL);
            }
            if (foundToggle || (foundPlus && foundMinus)) {
                setDirection(signalTransition, SignalTransition.Direction.TOGGLE);
            } else if (foundPlus) {
                setDirection(signalTransition, SignalTransition.Direction.PLUS);
            } else if (foundMinus) {
                setDirection(signalTransition, SignalTransition.Direction.MINUS);
            }
        }
        return result;
    }

    @Override
    public boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<? extends MathNode> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        if (getReferenceManager() == null) {
            return false;
        }
        NamespaceProvider dstProvider = dstContainer instanceof NamespaceProvider
                ? (NamespaceProvider) dstContainer
                : getReferenceManager().getNamespaceProvider(dstContainer);

        NameManager dstNameManager = getReferenceManager().getNameManager(dstProvider);
        for (MathNode srcChild : srcChildren) {
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
                Signal.Type srcSignalType = srcTransition.getSignalType();
                Signal.Type dstSignalType = getSignalType(dstSignalRef);
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
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Place", getPlaces().size());
        result.add("Transition", getTransitions().size());
        result.add("Arc", getConnections().size());
        return result;
    }

    @Override
    public void anonymise() {
        setTitle("");
        StgReferenceManager referenceManager = getReferenceManager();
        for (MathNode node : Hierarchy.getDescendantsOfType(getRoot(), MathNode.class)) {
            String name = getName(node);
            if ((name != null) && !Identifier.isInternal(name) && !(node instanceof SignalTransition)) {
                referenceManager.setDefaultName(node);
            }
        }
        for (String signalRef : getSignalReferences()) {
            String newName = null;
            for (SignalTransition signalTransition : getSignalTransitions(signalRef)) {
                if (newName == null) {
                    referenceManager.setDefaultName(signalTransition);
                    newName = signalTransition.getSignalName();
                } else {
                    referenceManager.setName(signalTransition, newName, false);
                }
            }
        }
    }

    public FileReference getRefinement() {
        return refinement;
    }

    public void setRefinement(FileReference value) {
        refinement = value;
    }

    public File getRefinementFile() {
        return (refinement == null) ? null : refinement.getFile();
    }

}
