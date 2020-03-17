package org.workcraft.plugins.fst;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fst.tools.FstSimulationTool;
import org.workcraft.utils.Hierarchy;

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
            properties.removeByName("Symbols");
            for (final Symbol symbol : getMathModel().getSymbols()) {
                properties.removeByName("Symbol " + getMathName(symbol));
            }
            properties.add(PropertyHelper.getSignalSectionProperty(this));
            properties.addAll(FstPropertyHelper.getSignalProperties(this));
        } else if (node instanceof VisualSignalEvent) {
            VisualSignalEvent signalEvent = (VisualSignalEvent) node;
            Signal signal = signalEvent.getReferencedConnection().getSignal();
            properties.add(FstPropertyHelper.getEventSignalProperty(getMathModel(), signalEvent.getReferencedConnection()));
            properties.add(FstPropertyHelper.getSignalTypeProperty(signal, Signal.PROPERTY_TYPE));
            if (signal.hasDirection()) {
                properties.add(FstPropertyHelper.getEventDrectionProperty(signalEvent.getReferencedConnection()));
            }
            properties.removeByName(Event.PROPERTY_SYMBOL);
        }
        return properties;
    }

}
