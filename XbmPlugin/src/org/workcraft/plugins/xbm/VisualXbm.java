package org.workcraft.plugins.xbm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.ActionDeclaration;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.xbm.properties.SignalPropertyDescriptor;
import org.workcraft.plugins.xbm.properties.SignalPropertyDescriptors;
import org.workcraft.plugins.xbm.properties.StatePropertyDescriptors;
import org.workcraft.plugins.xbm.tool.XbmSignalSimulationTool;
import org.workcraft.plugins.xbm.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;

import java.util.*;

@DisplayName("eXtended Burst-Mode Machine")
@ShortName("XBM")
public class VisualXbm extends VisualFsm {

    public VisualXbm() {
        this(null, null);
    }

    public VisualXbm(Xbm model) {
        this(model, null);
    }

    public VisualXbm(Xbm model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(true, false, true, true));
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(XbmState.class)));
//        tools.add(new XbmSimulationTool());
        tools.add(new XbmSignalSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Xbm getMathModel() {
        return (Xbm) super.getMathModel();
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualXbmState vStateFrom = (VisualXbmState) first;
        VisualXbmState vStateTo = (VisualXbmState) second;
        XbmState mStateFrom = vStateFrom.getReferencedState();
        XbmState mStateTo = vStateTo.getReferencedState();

        if (mConnection == null) {
            mConnection = createBurstEvent(mStateFrom, mStateTo);
        }
        VisualBurstEvent vBEvent = new VisualBurstEvent((BurstEvent) mConnection, vStateFrom, vStateTo);

        Container container = Hierarchy.getNearestContainer(vStateFrom, vStateTo);
        container.add(vBEvent);

        return vBEvent;
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        } else if (ConversionUtils.doesArcExist(this, first, second)) {
            throw new InvalidConnectionException("This arc already exists.");
        }
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.addAll(getSignalNameAndTypeProperties());
            properties.add(new ActionDeclaration("Create signal",
                    () -> getMathModel().createSignal(null, XbmSignal.DEFAULT_SIGNAL_TYPE)));

        } else if (node instanceof VisualBurstEvent) {
            final VisualBurstEvent visualBurstevent = (VisualBurstEvent) node;
            final BurstEvent burstEvent = visualBurstevent.getReferencedBurstEvent();
            properties.add(getConditionalProperty(burstEvent));
            properties.addAll(getSignalDirectionProperties(burstEvent));
            properties.removeByName(Event.PROPERTY_SYMBOL);
        } else if (node instanceof VisualXbmState) {
            final VisualXbmState vXbmState = (VisualXbmState) node;
            final XbmState xbmState = vXbmState.getReferencedState();
            properties.addAll(getSignalValueProperties(xbmState));
            properties.removeByName(VisualComponent.PROPERTY_LABEL);
        }
        return properties;
    }

    public BurstEvent createBurstEvent(final XbmState from, final XbmState to) {
        final Xbm xbm = getMathModel();
        final Burst burst = new Burst(from, to);
        return xbm.createBurstEvent(from, to, burst);
    }

    private List<PropertyDescriptor> getSignalNameAndTypeProperties() {
        final Xbm xbm = getMathModel();
        final List<PropertyDescriptor> list = new LinkedList<>();
        for (final XbmSignal s: xbm.getSignals()) {
            list.add(new SignalPropertyDescriptor(xbm, s));
            list.add(SignalPropertyDescriptors.typeProperty(this, s));
        }
        return list;
    }

    private List<PropertyDescriptor> getSignalValueProperties(final XbmState state) {
        final Xbm xbm = getMathModel();
        final List<PropertyDescriptor> list = new LinkedList<>();
        final Set<XbmSignal> inputs = new LinkedHashSet<>(xbm.getSignals(XbmSignal.Type.INPUT));
        final Set<XbmSignal> outputs = new LinkedHashSet<>(xbm.getSignals(XbmSignal.Type.OUTPUT));
        if (!inputs.isEmpty()) {
            list.add(StatePropertyDescriptors.burstProperty(state, "Input burst", XbmSignal.Type.INPUT));
            for (XbmSignal i: inputs) {
                list.add(SignalPropertyDescriptors.valueProperty(this, state, i));
            }
        }
        if (!outputs.isEmpty()) {
            list.add(StatePropertyDescriptors.burstProperty(state, "Output burst", XbmSignal.Type.OUTPUT));
            for (XbmSignal o: outputs) {
                list.add(SignalPropertyDescriptors.valueProperty(this, state, o));
            }
        }
        return list;
    }

    private List<PropertyDescriptor> getSignalDirectionProperties(final BurstEvent burstEvent) {
        final Xbm xbm = getMathModel();
        final List<PropertyDescriptor> result = new LinkedList<>();
        final Set<XbmSignal> inputs = new LinkedHashSet<>(xbm.getSignals(XbmSignal.Type.INPUT));
        final Set<XbmSignal> outputs = new LinkedHashSet<>(xbm.getSignals(XbmSignal.Type.OUTPUT));

        result.add(new ActionDeclaration("Input burst", "Create input",
                () -> getMathModel().createSignal(null, XbmSignal.Type.INPUT)));

        if (!inputs.isEmpty()) {
            for (XbmSignal i: inputs) {
                result.add(SignalPropertyDescriptors.directionProperty(this, burstEvent, i));
            }
        }

        result.add(new ActionDeclaration("Output burst", "Create output",
                () -> getMathModel().createSignal(null, XbmSignal.Type.OUTPUT)));

        if (!outputs.isEmpty()) {
            for (XbmSignal o: outputs) {
                result.add(SignalPropertyDescriptors.directionProperty(this, burstEvent, o));
            }
        }
        return result;
    }

    private PropertyDescriptor getConditionalProperty(final BurstEvent event) {
        return new PropertyDeclaration<BurstEvent, String>(event, BurstEvent.PROPERTY_CONDITIONAL, String.class, true, true) {
            @Override
            public void setter(BurstEvent object, String value) {
                object.setConditional(value);
            }

            @Override
            public String getter(BurstEvent object) {
                return object.getConditional();
            }
        };
    }

}
