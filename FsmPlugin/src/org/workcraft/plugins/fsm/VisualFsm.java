package org.workcraft.plugins.fsm;

import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
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
	public void connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualState vState1 = (VisualState)first;
		VisualState vState2 = (VisualState)second;
		State mState1 = vState1.getReferencedState();
		State mState2 = vState2.getReferencedState();

		Event mTransition = ((Fsm)getMathModel()).connect(mState1, mState2);
		VisualEvent vTransition = new VisualEvent(mTransition, vState1, vState2);
		Hierarchy.getNearestContainer(vState1, vState2).add(vTransition);
		if (vState1 == vState2) {
			vTransition.setConnectionType(ConnectionType.BEZIER, true);
		}
	}

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first == null || second == null) {
			throw new InvalidConnectionException ("Invalid connection");
		}
		if (!(first instanceof VisualState) || !(second instanceof VisualState)) {
			throw new InvalidConnectionException ("Invalid connection");
		}
		if (getPostset(first).contains(second) || getPreset(second).contains(first)) {
			throw new InvalidConnectionException ("There is already an arc from " +
				getStateName((VisualState)first) + " to " + getStateName((VisualState)second));
		}
	}

	public String getStateName(VisualState state) {
		return getMathModel().getName(state.getReferencedComponent());
	}

	public void createInitialState() {
		State state = new State();
		getMathModel().add(state);
		add(new VisualState(state));
		state.setInitial(true);
	}

}
