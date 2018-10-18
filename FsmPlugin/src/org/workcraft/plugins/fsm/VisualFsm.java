package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.*;
import org.workcraft.plugins.fsm.observers.FirstStateSupervisor;
import org.workcraft.plugins.fsm.tools.FsmSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Finite State Machine")
public class VisualFsm extends AbstractVisualModel {

    public VisualFsm(Fsm model) {
        this(model, null);
    }

    public VisualFsm(Fsm model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
        new FirstStateSupervisor().attach(getRoot());
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(true, false, true, true));
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(State.class)));
        tools.add(new FsmSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
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
