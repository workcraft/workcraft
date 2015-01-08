package org.workcraft.plugins.fsm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.fsm.propertydescriptors.EventSymbolPropertyDescriptor;
import org.workcraft.plugins.fsm.propertydescriptors.SymbolPropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.fsm.VisualFsm.class)
public class Fsm extends AbstractMathModel {
	public static String EPSILON_SERIALISATION = "epsilon";

	private final class StateSupervisorExtension extends StateSupervisor {
		@Override
		public void handleEvent(StateEvent e) {
			if (e instanceof PropertyChangedEvent) {
				PropertyChangedEvent pce = (PropertyChangedEvent)e;
				Object sender = e.getSender();
				if ((sender instanceof State) && pce.getPropertyName().equals("initial")) {
					// Update all the states on a change of the initial property
					handleInitialStateChange((State)sender);
				} else if ((sender instanceof Event) && pce.getPropertyName().equals("symbol")) {
					// Update the collection of symbols on a change of event symbol property
					handleEventSymbolChange((Event)sender);
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

		private void handleEventSymbolChange(Event event) {
			HashSet<Node> unusedSymbols = new HashSet<Node>(getSymbols());
			for (Event e: getEvents()) {
				Symbol symbol = e.getSymbol();
				unusedSymbols.remove(symbol);
			}
			remove(unusedSymbols);
		}
	}

	private final class HierarchySupervisorExtension extends HierarchySupervisor {
		@Override
		public void handleEvent(HierarchyEvent e) {
			if (e instanceof NodesDeletingEvent) {
				for (Node node: e.getAffectedNodes()) {
					if (node instanceof State) {
						// Move the initial property to another state on state removal
						handleStateRemoval((State)node);
					} else if (node instanceof Event) {
						// Remove unused symbols on event deletion
						handleEventRemoval((Event)node);
					}
				}
			} else if (e instanceof NodesAddingEvent) {
				for (Node node: e.getAffectedNodes()) {
					if (node instanceof State) {
						// Make pasted states non-initial
						((State)node).setInitialQuiet(false);
					}
				}
			}
		}

		private void handleStateRemoval(State state) {
			if (state.isInitial()) {
				for (State s: getStates()) {
					if ( !s.equals(state) ) {
						s.setInitial(true);
						break;
					}
				}
			}
		}


		private void handleEventRemoval(Event event) {
			boolean symbolIsUnused = true;
			Symbol symbol = event.getSymbol();
			for (Event e: getEvents()) {
				if ((e != event) && (e.getSymbol() == symbol)) {
					symbolIsUnused = false;
					break;
				}
			}
			if (symbolIsUnused) {
				remove(symbol);
			}
		}
	}

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
                if (node instanceof Event) return "e";
				return super.getPrefix(node);
			}
		});
		new HierarchySupervisorExtension().attach(getRoot());
		new StateSupervisorExtension().attach(getRoot());
	}

	public State createState(String name) {
		return createNode(name, null, State.class);
	}

	public Symbol createSymbol(String name) {
		return createNode(name, null, Symbol.class);
	}

	public Event createEvent(State first, State second, Symbol symbol) {
		Event event = new Event(first, second, symbol);
		Container container = Hierarchy.getNearestContainer(first, second);
		container.add(event);
		return event;
	}

	final public Collection<State> getStates() {
		return Hierarchy.getDescendantsOfType(getRoot(), State.class);
	}

	final public Collection<Symbol> getSymbols() {
		return Hierarchy.getDescendantsOfType(getRoot(), Symbol.class);
	}

	final public Collection<Event> getEvents() {
		return Hierarchy.getDescendantsOfType(getRoot(), Event.class);
	}

	final public Collection<Event> getEvents(final Symbol symbol) {
		return Hierarchy.getDescendantsOfType(getRoot(), Event.class, new Func<Event, Boolean>() {
			@Override
			public Boolean eval(Event arg) {
				return (arg.getSymbol() == symbol);
			}
		});
	}

	public State getInitialState() {
		for (State state: getStates()) {
			if (state.isInitial()) {
				return state;
			}
		}
		return null;
	}

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node == null) {
			for (final Symbol symbol: getSymbols()) {
				properties.add(new SymbolPropertyDescriptor(this, symbol));
			}
		} else if (node instanceof Event) {
			Event event = (Event) node;
			properties.add(new EventSymbolPropertyDescriptor(this, event));
		}
		return properties;
	}

}
