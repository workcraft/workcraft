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
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.observers.FirstStateSupervisor;
import org.workcraft.plugins.fsm.tools.FsmSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

@DisplayName("Finite State Machine")
public class VisualFsm extends AbstractVisualModel {

    public VisualFsm(Fsm model) {
        this(model, null);
    }

    public VisualFsm(Fsm model, VisualGroup root) {
        super(model, root);
        new FirstStateSupervisor().attach(getRoot());
    }

    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new SelectionTool(true, false, true, true));
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new ConnectionTool(false, true, true));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(State.class)));
        addGraphEditorTool(new FsmSimulationTool());
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
            properties.addAll(FsmPropertyHelper.getSymbolProperties(this));
        } else if (node instanceof VisualEvent event) {
            properties.add(FsmPropertyHelper.getEventSymbolProperty(getMathModel(), event.getReferencedConnection()));
        }
        return properties;
    }

}
