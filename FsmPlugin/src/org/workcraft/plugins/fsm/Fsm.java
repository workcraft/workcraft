package org.workcraft.plugins.fsm;

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.fsm.VisualFsm.class)
public class Fsm extends AbstractMathModel {

	public Fsm() {
		this(null, null);
	}

	public Fsm(Container root) {
		this(root, null);
	}

	public Fsm(Container root, References refs) {
		super(root, new HierarchicalUniqueNameReferenceManager(refs) {
			@Override
			public String getPrefix(Node node) {
                if (node instanceof State) return "s";
                if (node instanceof Event) return "t";
				return super.getPrefix(node);
			}
		});

		// Move the initial property to another state on state removal
		new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if (e instanceof NodesDeletingEvent) {
					for (Node node: e.getAffectedNodes()) {
						if (node instanceof State) {
							handleInitialStateRemoval((State)node);
						}
					}
				} else if (e instanceof NodesAddingEvent) {
					for (Node node: e.getAffectedNodes()) {
						if (node instanceof State) {
							((State)node).setInitialQuiet(false);
						}
					}
				}
			}
		}.attach(getRoot());

		// Update all the states on a change of the initial property
		new StateSupervisor() {
			@Override
			public void handleEvent(StateEvent e) {
				if (e instanceof PropertyChangedEvent) {
					Object object = e.getSender();
					if (object instanceof State) {
						PropertyChangedEvent pce = (PropertyChangedEvent)e;
						if (pce.getPropertyName().equals("initial")) {
							handleInitialStateChange((State)object);
						}
					}
				}
			}
		}.attach(getRoot());
	}

	private void handleInitialStateRemoval(State state) {
		if (state.isInitial()) {
			for (State s: Hierarchy.getChildrenOfType(state.getParent(), State.class)) {
				if ( !s.equals(state) ) {
					s.setInitial(true);
					break;
				}
			}
		}
	}

	private void handleInitialStateChange(State state) {
		for (State s: Hierarchy.getChildrenOfType(state.getParent(), State.class)) {
			if ( !s.equals(state) ) {
				if (state.isInitial()) {
					s.setInitialQuiet(false);
				} else {
					s.setInitialQuiet(true);
					break;
				}
			}
		}
	}

	final public State createState(String name) {
		return createNode(name, null, State.class);
	}

	public Event connect(State first, State second) {
		Event con = new Event(first, second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	final public Collection<State> getStates() {
		return Hierarchy.getDescendantsOfType(getRoot(), State.class);
	}

	final public Collection<Event> getEvents() {
		return Hierarchy.getDescendantsOfType(getRoot(), Event.class);
	}

	public State getInitialState() {
		for (State state: getStates()) {
			if (state.isInitial()) {
				return state;
			}
		}
		return null;
	}

}
