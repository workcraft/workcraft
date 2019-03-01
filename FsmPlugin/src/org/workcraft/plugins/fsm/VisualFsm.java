package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.*;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.observers.FirstStateSupervisor;
import org.workcraft.plugins.fsm.tools.FsmSimulationTool;
import org.workcraft.utils.Hierarchy;

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
        State mState1 = vState1.getReferencedState();
        State mState2 = vState2.getReferencedState();

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
            for (final Symbol symbol: getMathModel().getSymbols()) {
                properties.insertOrderedByFirstWord(getSymbolProperty(symbol));
            }
        } else if (node instanceof VisualEvent) {
            properties.add(getEventSymbolProperty((VisualEvent) node));
        }
        return properties;
    }

    private PropertyDescriptor getSymbolProperty(Symbol symbol) {
        return new PropertyDeclaration<Symbol, String>(
                symbol, getMathModel().getName(symbol) + " name", String.class) {
            @Override
            public void setter(Symbol object, String value) {
                Node node = getMathModel().getNodeByReference(value);
                if (node == null) {
                    getMathModel().setName(object, value);
                    for (Event event: getMathModel().getEvents(object)) {
                        event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
                    }
                } else if (!(node instanceof Symbol)) {
                    throw new FormatException("Node '" + value + "' already exists and it is not a symbol.");
                }
            }
            @Override
            public String getter(Symbol object) {
                return getMathModel().getName(object);
            }
        };
    }

    private PropertyDescriptor getEventSymbolProperty(VisualEvent event) {
        return new PropertyDeclaration<VisualEvent, String>(
                event, Event.PROPERTY_SYMBOL, String.class, true, true) {
            @Override
            public void setter(VisualEvent object, String value) {
                Symbol symbol = null;
                if (!value.isEmpty()) {
                    Node node = getMathModel().getNodeByReference(value);
                    if (node instanceof Symbol) {
                        symbol = (Symbol) node;
                    } else {
                        symbol = getMathModel().createSymbol(value);
                    }
                }
                object.getReferencedEvent().setSymbol(symbol);
            }
            @Override
            public String getter(VisualEvent object) {
                Symbol symbol = object.getReferencedEvent().getSymbol();
                String symbolName = "";
                if (symbol != null) {
                    symbolName = getMathModel().getName(symbol);
                }
                return symbolName;
            }
        };
    }

}
