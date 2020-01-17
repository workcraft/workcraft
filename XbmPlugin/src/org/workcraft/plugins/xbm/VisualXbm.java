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
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.xbm.tool.XbmSimulationTool;
import org.workcraft.plugins.xbm.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.List;

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
        XbmState mStateFrom = vStateFrom.getReferencedComponent();
        XbmState mStateTo = vStateTo.getReferencedComponent();

        if (mConnection == null) {
            Burst burst = new Burst(mStateFrom, mStateTo);
            mConnection = getMathModel().createBurstEvent(mStateFrom, mStateTo, burst);
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
        Xbm xbm = getMathModel();
        if (node == null) {
            properties.add(PropertyHelper.getSignalSectionProperty(this));
            properties.addAll(XbmPropertyHelper.getSignalProperties(xbm));
            properties.add(XbmPropertyHelper.getCreateSignalProperty(this));

        } else if (node instanceof VisualBurstEvent) {
            final BurstEvent burstEvent = ((VisualBurstEvent) node).getReferencedConnection();
            properties.add(XbmPropertyHelper.getConditionalProperty(burstEvent));
            properties.addAll(XbmPropertyHelper.getSignalDirectionProperties(this, burstEvent));
            properties.removeByName(Event.PROPERTY_SYMBOL);
            //TODO Add VisualBurstTransition here
        } else if (node instanceof VisualXbmState) {
            final XbmState xbmState = ((VisualXbmState) node).getReferencedComponent();
            properties.addAll(XbmPropertyHelper.getSignalValueProperties(this, xbmState));
            properties.removeByName(VisualComponent.PROPERTY_LABEL);
        }
        return properties;
    }

}
