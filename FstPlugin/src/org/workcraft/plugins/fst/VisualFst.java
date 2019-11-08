package org.workcraft.plugins.fst;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.tools.FstSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Finite State Transducer")
public class VisualFst extends VisualFsm {

    public VisualFst(Fst model) {
        this(model, null);
    }

    public VisualFst(Fst model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(true, false, true, true));
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(State.class)));
        tools.add(new FstSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Fst getMathModel() {
        return (Fst) super.getMathModel();
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualState vState1 = (VisualState) first;
        VisualState vState2 = (VisualState) second;
        State mState1 = vState1.getReferencedComponent();
        State mState2 = vState2.getReferencedComponent();

        if (mConnection == null) {
            Signal signal = getMathModel().createSignal(null, Signal.Type.DUMMY);
            mConnection = getMathModel().createSignalEvent(mState1, mState2, signal);
        }
        VisualSignalEvent vEvent = new VisualSignalEvent((SignalEvent) mConnection, vState1, vState2);

        Container container = Hierarchy.getNearestContainer(vState1, vState2);
        container.add(vEvent);
        return vEvent;
    }

    public Collection<VisualSignalEvent> getVisualSignalEvents() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualSignalEvent.class);
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            for (final Signal signal: getMathModel().getSignals()) {
                String description = getMathModel().getName(signal) + " type";
                properties.insertOrderedByFirstWord(getSignalTypeProperty(signal, description));
            }
        } else if (node instanceof VisualSignalEvent) {
            VisualSignalEvent signalEvent = (VisualSignalEvent) node;
            Signal signal = signalEvent.getReferencedConnection().getSignal();
            properties.add(getEventSignalProperty(signalEvent));
            properties.add(getSignalTypeProperty(signal, Signal.PROPERTY_TYPE));
            if (signal.hasDirection()) {
                properties.add(getEventDrectionProperty(signalEvent));
            }
            properties.removeByName(Event.PROPERTY_SYMBOL);
        }
        return properties;
    }

    private PropertyDescriptor getSignalTypeProperty(Signal signal, String description) {
        return new PropertyDeclaration<>(Signal.Type.class, description,
                value -> signal.setType(value), () -> signal.getType())
                .setCombinable().setTemplatable();
    }

    private PropertyDescriptor getEventSignalProperty(VisualSignalEvent signalEvent) {
        return new PropertyDeclaration<>(String.class, "Signal",
                value -> {
                    Signal signal = null;
                    if (!value.isEmpty()) {
                        Node node = getMathModel().getNodeByReference(value);
                        if (node instanceof Signal) {
                            signal = (Signal) node;
                        } else {
                            Signal oldSignal = signalEvent.getReferencedConnection().getSignal();
                            Signal.Type type = oldSignal.getType();
                            signal = getMathModel().createSignal(value, type);
                        }
                    }
                    if (signal != null) {
                        signalEvent.getReferencedConnection().setSymbol(signal);
                    }
                },
                () -> {
                    Signal signal = signalEvent.getReferencedConnection().getSignal();
                    if (signal != null) {
                        return getMathModel().getName(signal);
                    }
                    return null;
                })
                .setCombinable().setTemplatable();
    }

    private PropertyDescriptor getEventDrectionProperty(VisualSignalEvent signalEvent) {
        return new PropertyDeclaration<>(SignalEvent.Direction.class, SignalEvent.PROPERTY_DIRECTION,
                value -> signalEvent.getReferencedConnection().setDirection(value),
                () -> signalEvent.getReferencedConnection().getDirection())
                .setCombinable().setTemplatable();
    }

}
