package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.observers.FirstStateSupervisor;
import org.workcraft.plugins.fsm.tools.FsmSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.*;

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
    public Fsm getMathModel() {
        return (Fsm) super.getMathModel();
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualState vState1 = (VisualState) first;
        VisualState vState2 = (VisualState) second;
        State mState1 = vState1.getReferencedComponent();
        State mState2 = vState2.getReferencedComponent();

        if (mConnection == null) {
            mConnection = getMathModel().createEvent(mState1, mState2, null);
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

    public Collection<VisualEvent> getVisualEvents() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class);
    }

    public Collection<VisualEvent> getVisualEvents(Symbol symbol) {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class,
                event -> event.getReferencedConnection().getSymbol().equals(symbol));
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.add(PropertyHelper.createSeparatorProperty("Symbols"));
            List<Symbol> symbols = new ArrayList<>(getMathModel().getSymbols());
            Collections.sort(symbols, Comparator.comparing(getMathModel()::getNodeReference));
            for (final Symbol symbol : symbols) {
                properties.add(FsmPropertyHelper.getSymbolProperty(this, symbol));
            }
        } else if (node instanceof VisualEvent) {
            Event event = ((VisualEvent) node).getReferencedConnection();
            properties.add(FsmPropertyHelper.getEventSymbolProperty(getMathModel(), event));
        }
        return properties;
    }

}
