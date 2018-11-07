package org.workcraft.plugins.fst;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.*;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.properties.DirectionPropertyDescriptor;
import org.workcraft.plugins.fst.properties.EventSignalPropertyDescriptor;
import org.workcraft.plugins.fst.properties.SignalTypePropertyDescriptor;
import org.workcraft.plugins.fst.properties.TypePropertyDescriptor;
import org.workcraft.plugins.fst.tools.FstSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.*;

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
        State mState1 = vState1.getReferencedState();
        State mState2 = vState2.getReferencedState();

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
                SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(getMathModel(), signal);
                properties.insertOrderedByFirstWord(typeDescriptor);
            }
        } else if (node instanceof VisualSignalEvent) {
            LinkedList<PropertyDescriptor> eventDescriptors = new LinkedList<>();
            SignalEvent signalEvent = ((VisualSignalEvent) node).getReferencedSignalEvent();
            eventDescriptors.add(new EventSignalPropertyDescriptor(getMathModel(), signalEvent));
            Signal signal = signalEvent.getSignal();
            eventDescriptors.add(new TypePropertyDescriptor(signal));
            if (signal.hasDirection()) {
                eventDescriptors.add(new DirectionPropertyDescriptor(signalEvent));
            }
            Collections.sort(eventDescriptors, Comparator.comparing(PropertyDescriptor::getName));
            properties.addAll(eventDescriptors);
            properties.removeByName(Event.PROPERTY_SYMBOL);
        }
        return properties;
    }

}
