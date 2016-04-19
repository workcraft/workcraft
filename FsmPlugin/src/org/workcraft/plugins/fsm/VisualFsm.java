package org.workcraft.plugins.fsm;

import java.util.Collection;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.util.Hierarchy;

@DisplayName("Finite State Machine")
@CustomTools(FsmToolsProvider.class)
public class VisualFsm extends AbstractVisualModel {

    public VisualFsm(Fsm model) {
        this(model, null);
    }

    public VisualFsm(Fsm model, VisualGroup root) {
        super(model, root);
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
        // Make the first created state initial
        new HierarchySupervisor() {
            @Override
            public void handleEvent(HierarchyEvent e) {
                if (e instanceof NodesAddingEvent) {
                    Collection<VisualState> existingStates = Hierarchy.getChildrenOfType(getRoot(), VisualState.class);
                    if (existingStates.isEmpty()) {
                        Collection<VisualState> newStates = Hierarchy.filterNodesByType(e.getAffectedNodes(), VisualState.class);
                        if (!newStates.isEmpty()) {
                            VisualState state = newStates.iterator().next();
                            state.getReferencedState().setInitial(true);
                        }
                    }
                }
            }
        }.attach(getRoot());
    }

    @Override
    public void validateConnection(Node first, Node second)    throws InvalidConnectionException {
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualState vState1 = (VisualState) first;
        VisualState vState2 = (VisualState) second;
        State mState1 = vState1.getReferencedState();
        State mState2 = vState2.getReferencedState();

        if (mConnection == null) {
            mConnection = ((Fsm) getMathModel()).createEvent(mState1, mState2, null);
        }
        VisualEvent vEvent = new VisualEvent((Event) mConnection, vState1, vState2);

        Container container = Hierarchy.getNearestContainer(vState1, vState2);
        container.add(vEvent);
        return vEvent;
    }

    public String getStateName(VisualState state) {
        return getMathModel().getName(state.getReferencedComponent());
    }

    public Collection<VisualState> getVisualStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualState.class);
    }

    public Collection<VisualEvent> getVisualSymbols() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class);
    }

}
