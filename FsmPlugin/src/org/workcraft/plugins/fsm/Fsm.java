package org.workcraft.plugins.fsm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.ArgumentException;
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
			Symbol symbol = event.getSymbol();
			if (symbol != null) {
				boolean symbolIsUnused = true;
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
	}

	public Fsm() {
		this(null, (References)null);
	}

	public Fsm(Container root, References refs) {
		this(root, new HierarchicalUniqueNameReferenceManager(refs) {
			@Override
			public String getPrefix(Node node) {
                if (node instanceof State) return "s";
                if (node instanceof Event) return "e";
				return super.getPrefix(node);
			}
		});
	}

	public Fsm(Container root, ReferenceManager man) {
		super(root, man);
		new HierarchySupervisorExtension().attach(getRoot());
		new StateSupervisorExtension().attach(getRoot());
	}


	public State createState(String name) {
		return createNode(name, null, State.class);
	}

    public State getOrCreateState(String name) {
    	State state = null;
        Node node = getNodeByReference(name);
        if (node == null) {
            state = createState(name);
        } else if (node instanceof State) {
        	state = (State)node;
        } else {
            throw new ArgumentException("Node '" + name + "' is not a state.");
        }
        return state;
    }

	public Symbol createSymbol(String name) {
		return createNode(name, null, Symbol.class);
	}

	public Symbol getOrCreateSymbol(String name) {
		Symbol symbol = null;
		Node node = getNodeByReference(name);
		if (node == null) {
			symbol = createSymbol(name);
		} else if (node instanceof Symbol) {
			symbol = (Symbol)node;
		} else {
			throw new ArgumentException("Node '" + name + "' already exists and it is not a symbol.");
		}
		return symbol;
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

	public boolean isDeterministicSymbol(Symbol symbol) {
		return (symbol != null);
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
	public void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
		if (srcModel == null) {
			srcModel = this;
		}
		HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager)getReferenceManager();
		NameManager nameManagerer = refManager.getNameManager(null);
		for (Node srcNode: srcChildren) {
			if (srcNode instanceof Event) {
				Event srcEvent = (Event)srcNode;
				Symbol dstSymbol = null;
				Symbol srcSymbol = srcEvent.getSymbol();
				if (srcSymbol != null) {
					String symbolName = srcModel.getNodeReference(srcSymbol);
					Node dstNode = getNodeByReference(symbolName);
					if (dstNode instanceof Symbol) {
						dstSymbol = (Symbol)dstNode;
					} else {
						if (dstNode != null) {
							symbolName = nameManagerer.getDerivedName(null, symbolName);
						}
						dstSymbol = createSymbol(symbolName);
					}
				}
				srcEvent.setSymbol(dstSymbol);
			}
		}
		super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
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
