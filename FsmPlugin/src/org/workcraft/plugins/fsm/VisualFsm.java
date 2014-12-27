package org.workcraft.plugins.fsm;

import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.SetUtils;

@DisplayName("Finite State Machine")
@CustomTools(ToolsProvider.class)
public class VisualFsm extends AbstractVisualModel {

	public VisualFsm(Fsm model) {
		this(model, null);
	}

	public VisualFsm(Fsm model, VisualGroup root) {
		super(model, root);
		if (root == null) {
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}
		}
		createInitialState();

		// Create a new initial state when the last state is removed
		new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if (e instanceof NodesDeletingEvent) {
					HashSet<Node> stateSet = new HashSet<Node>(Hierarchy.getChildrenOfType(getRoot(), VisualState.class));
					HashSet<Node> removeSet = new HashSet<Node>(e.getAffectedNodes());
					if (SetUtils.intersection(stateSet, removeSet).size() == stateSet.size()) {
						createInitialState();
					}
				}
			}
		}.attach(getRoot());
	}

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
	}

	@Override
	public VisualConnection connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualState vState1 = (VisualState)first;
		VisualState vState2 = (VisualState)second;
		State mState1 = vState1.getReferencedState();
		State mState2 = vState2.getReferencedState();

		Event mEvent = ((Fsm)getMathModel()).connect(mState1, mState2);
		VisualEvent vEvent = new VisualEvent(mEvent, vState1, vState2);

		Container container = Hierarchy.getNearestContainer(vState1, vState2);
		container.add(vEvent);
		return vEvent;
	}

	public String getStateName(VisualState state) {
		return getMathModel().getName(state.getReferencedComponent());
	}

	public void createInitialState() {
		Fsm fsm = (Fsm)getMathModel();
		if ((fsm != null) && (fsm.getInitialState() == null)) {
			State state = new State();
			fsm.add(state);
			add(new VisualState(state));
			state.setInitial(true);
		}
	}

}
