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
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.xbm.properties.DeclaredSignalPropertyDescriptor;
import org.workcraft.plugins.xbm.properties.SignalModifierDescriptors;
import org.workcraft.plugins.xbm.properties.SignalPropertyDescriptors;
import org.workcraft.plugins.xbm.properties.UneditablePropertyDescriptor;
import org.workcraft.plugins.xbm.tool.XbmSimulationTool;
import org.workcraft.plugins.xbm.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;

import java.util.*;

@DisplayName("eXtended Burst-Mode Machine")
@ShortName("XBM")
public class VisualXbm extends VisualFsm {

    private static final PropertyDescriptor PROPERTY_INPUT_BURST_PLACEHOLDER = new UneditablePropertyDescriptor("Input Burst", "");
    private static final PropertyDescriptor PROPERTY_OUTPUT_BURST_PLACEHOLDER = new UneditablePropertyDescriptor("Output Burst", "");

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
        tools.add(new XbmSimulationTool());
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

        if (first == second) throw new InvalidConnectionException("Self-loops are not allowed.");
        else if (ConversionUtils.doesArcExist(this, first, second)) throw new InvalidConnectionException("This arc already exists.");
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        final Xbm xbm = getMathModel();
        if (node == null) {
            properties.addAll(getSignalNameAndTypeProperties());
            for (Signal s: xbm.getSignals()) {
                final String signalName = xbm.getName(s);
                properties.add(new DeclaredSignalPropertyDescriptor(this, signalName, s.getType()));
            }
            properties.add(new DeclaredSignalPropertyDescriptor(this, DeclaredSignalPropertyDescriptor.PROPERTY_NEW_SIGNAL, Signal.DEFAULT_SIGNAL_TYPE));
        }
        else if (node instanceof VisualBurstEvent) {
            final VisualBurstEvent visualBurstevent = (VisualBurstEvent) node;
            final BurstEvent burstEvent = visualBurstevent.getReferencedBurstEvent();
            properties.add(getConditionalProperty(burstEvent));
            properties.addAll(getSignalDirectionProperties(burstEvent));
            properties.removeByName(Event.PROPERTY_SYMBOL);
        }
        else if (node instanceof VisualXbmState) {
            final VisualXbmState vXbmState = (VisualXbmState) node;
            final XbmState xbmState = vXbmState.getReferencedState();
            properties.add(SignalModifierDescriptors.toggleProperty(xbmState));
            properties.add(SignalModifierDescriptors.allOneProperty(xbmState));
            properties.add(SignalModifierDescriptors.allZeroProperty(xbmState));
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
        for (final Signal s: xbm.getSignals()) {
            list.add(SignalPropertyDescriptors.nameProperty(this, s));
            list.add(SignalPropertyDescriptors.typeProperty(this, s));
        }
        return list;
    }

    private List<PropertyDescriptor> getSignalValueProperties(final XbmState state) {

        final Xbm xbm = getMathModel();
        final List<PropertyDescriptor> list = new LinkedList<>();
        final Set<Signal> inputs = new LinkedHashSet<>();
        final Set<Signal> outputs = new LinkedHashSet<>();

        inputs.addAll(xbm.getSignals(Signal.Type.INPUT));
        outputs.addAll(xbm.getSignals(Signal.Type.OUTPUT));

        if (!inputs.isEmpty()) {
            list.add(PROPERTY_INPUT_BURST_PLACEHOLDER);
            for (Signal i: inputs) {
                list.add(SignalPropertyDescriptors.valueProperty(this, state, i));
            }
        }
        if (!outputs.isEmpty()) {
            list.add(PROPERTY_OUTPUT_BURST_PLACEHOLDER);
            for (Signal o: outputs) {
                list.add(SignalPropertyDescriptors.valueProperty(this, state, o));
            }
        }
        return list;
    }

    private List<PropertyDescriptor> getSignalDirectionProperties(final BurstEvent burstEvent) {

        final Xbm xbm = getMathModel();
        final List<PropertyDescriptor> list = new LinkedList<>();
        final Set<Signal> inputs = new LinkedHashSet<>();
        final Set<Signal> outputs = new LinkedHashSet<>();

        inputs.addAll(xbm.getSignals(Signal.Type.INPUT));
        outputs.addAll(xbm.getSignals(Signal.Type.OUTPUT));

        if (!inputs.isEmpty()) {
            list.add(PROPERTY_INPUT_BURST_PLACEHOLDER);
            for (Signal i: inputs) {
                list.add(SignalPropertyDescriptors.directionProperty(this, burstEvent, i));
            }
            list.add(new DeclaredSignalPropertyDescriptor(this, DeclaredSignalPropertyDescriptor.PROPERTY_NEW_INPUT, Signal.Type.INPUT));
        }
        if (!outputs.isEmpty()) {
            list.add(PROPERTY_OUTPUT_BURST_PLACEHOLDER);
            for (Signal o: outputs) {
                list.add(SignalPropertyDescriptors.directionProperty(this, burstEvent, o));
            }
            list.add(new DeclaredSignalPropertyDescriptor(this, DeclaredSignalPropertyDescriptor.PROPERTY_NEW_OUTPUT, Signal.Type.OUTPUT));
        }
        return list;
    }

    private PropertyDescriptor getConditionalProperty(final BurstEvent event) {
        final Xbm xbm = getMathModel();
        PropertyDeclaration propertyDeclaration = new PropertyDeclaration<BurstEvent, String>
                (event, BurstEvent.PROPERTY_CONDITIONAL, String.class, true, true) {
            @Override
            public void setter(BurstEvent object, String value) {
                object.setConditional(value);
            }

            @Override
            public String getter(BurstEvent object) {
                return object.getConditional();
            }

            @Override
            public boolean isEditable() {
                if (!xbm.getSignals(Signal.Type.CONDITIONAL).isEmpty()) {
                    return true;
                }
                else {
                    return false;
                }
            }
        };
        return propertyDeclaration;
    }

    public Collection<VisualBurstEvent> getVisualBurstEvents(){
        Collection<VisualBurstEvent> result = new LinkedHashSet<>();
        for (VisualEvent vEvent: getVisualSymbols()) {
            if (vEvent instanceof VisualBurstEvent) {
                result.add((VisualBurstEvent) vEvent);
            }
        }
        return result;
    }
}
