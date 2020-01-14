package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.*;
import org.workcraft.observation.PropertyChangedEvent;
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

    public Collection<VisualEvent> getVisualSymbols() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class);
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            List<Symbol> symbols = new ArrayList<>(getMathModel().getSymbols());
            Collections.sort(symbols, Comparator.comparing(getMathModel()::getNodeReference));
            for (final Symbol symbol : symbols) {
                properties.add(getSymbolProperty(symbol));
            }
        } else if (node instanceof VisualEvent) {
            properties.add(getEventSymbolProperty((VisualEvent) node));
        }
        return properties;
    }

    private PropertyDescriptor getSymbolProperty(Symbol symbol) {
        return new PropertyDeclaration<>(String.class, getMathModel().getName(symbol) + " name",
                value -> {
                    Node node = getMathModel().getNodeByReference(value);
                    if (node == null) {
                        getMathModel().setName(symbol, value);
                        for (Event event: getMathModel().getEvents(symbol)) {
                            event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
                        }
                    } else if (!(node instanceof Symbol)) {
                        throw new FormatException("Node '" + value + "' already exists and it is not a symbol.");
                    }
                },
                () -> getMathModel().getName(symbol));
    }

    private PropertyDescriptor getEventSymbolProperty(VisualEvent event) {
        return new PropertyDeclaration<>(String.class, Event.PROPERTY_SYMBOL,
                value -> {
                    Symbol symbol = null;
                    if (!value.isEmpty()) {
                        Node node = getMathModel().getNodeByReference(value);
                        if (node instanceof Symbol) {
                            symbol = (Symbol) node;
                        } else {
                            symbol = getMathModel().createSymbol(value);
                        }
                    }
                    event.getReferencedConnection().setSymbol(symbol);
                },
                () -> {
                    Symbol symbol = event.getReferencedConnection().getSymbol();
                    String symbolName = "";
                    if (symbol != null) {
                        symbolName = getMathModel().getName(symbol);
                    }
                    return symbolName;
                })
                .setCombinable().setTemplatable();
    }

}
