package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Func;
import org.workcraft.util.Identifier;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class STGNameManager implements NameManager<Node> {

	private static final String inputTransitionName = "in";
	private static final String outputTransitionName = "out";
	private static final String internalTransitionName = "t";
	private static final String dummyTransitionName = "dum";

	private InstanceManager<Node> instancedNameManager;
	private UniqueNameManager<Node> defaultNameManager;

	//private References existingReferences;
	private ListMap<String, SignalTransition> signalTransitions = new ListMap<String, SignalTransition>();
	private ListMap<String, DummyTransition> dummyTransitions = new ListMap<String, DummyTransition>();


	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return signalTransitions.get(signalName);
	}

	public Collection<DummyTransition> getDummyTransitions(String name) {
		return dummyTransitions.get(name);
	}

	Func<Node, String> nodePrefix;

	public STGNameManager() {
		this(null);
	}


	public int getInstanceNumber (Node st) {
		return instancedNameManager.getInstance(st).getSecond();
	}

	public void setInstanceNumber (Node st, int number) {
		instancedNameManager.assign(st, number);
	}


	public STGNameManager(Func<Node, String> nodePrefix) {

		if (nodePrefix != null)
			this.nodePrefix = nodePrefix;
		else {
			this.nodePrefix = new Func<Node, String>() {

				@Override
				public String eval(Node arg) {
					if (arg instanceof STGPlace) {
						return "p";
					}
					if (arg instanceof Connection) {
						return "con";
					}
					if (arg instanceof PageNode) {
						return "pg";
					}
					if (arg instanceof CommentNode) return "comment";
					if (arg instanceof Container) {
						return "g";
					}
					if (arg instanceof SignalTransition) {
						switch ( ((SignalTransition)arg).getSignalType() ) {
						case INPUT: return inputTransitionName;
						case OUTPUT: return outputTransitionName;
						case INTERNAL: return internalTransitionName;
						}
					}
					if (arg instanceof DummyTransition) {
						return dummyTransitionName;
					}
					return "v";
				}
			};
		}


		this.defaultNameManager = new UniqueNameManager<Node>(this.nodePrefix);

		this.instancedNameManager = new InstanceManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof SignalTransition) {
					return ((SignalTransition) arg).getSignalName() + ((SignalTransition) arg).getDirection();
				} else if (arg instanceof DummyTransition) {
					return ((DummyTransition)arg).getName();
				} else {
					throw new RuntimeException ("Unexpected class " + arg.getClass().getName());
				}
			}
		});

	}

	private SignalTransition.Type getSignalType(String signalName) {
		for (SignalTransition st : getSignalTransitions(signalName)) {
			return st.getSignalType();
		}
		return null;
	}

	private boolean isSignalName(String name) {
		return !getSignalTransitions(name).isEmpty();
	}

	private boolean isDummyName(String name) {
		return !getDummyTransitions(name).isEmpty();
	}

	private boolean isGoodSignalName(String name, SignalTransition.Type type) {
		if (type == null) {
			return false;
		}
		if (isDummyName(name)) {
			return false;
		}
		if (isSignalName(name)) {
			SignalTransition.Type expectedType = getSignalType(name);
			if (expectedType != null && !expectedType.equals(type)) {
				return false;
			}
		}
		return true;
	}

	private boolean isGoodDummyName(String name) {
		if (isSignalName(name)) {
			return false;
		}
		if (isDummyName(name)) {
			return false;
		}
		return true;
	}


	@Override
	public void setDefaultNameIfUnnamed(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			if (instancedNameManager.contains(st)) {
				return;
			}
			String prefix = defaultNameManager.getNodePrefix((Node) node);
			Integer count = defaultNameManager.getPrefixCount(prefix);
			String name = prefix + count;
			while ( !isGoodSignalName(name, st.getSignalType()) ) {
				name = prefix + (++count);
			};
			defaultNameManager.setPrefixCount(prefix, count);
			st.setSignalName(name);
			signalTransitions.put(name, st);
			instancedNameManager.assign(st);
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;
			if (instancedNameManager.contains(dt)) {
				return;
			}
			String prefix = defaultNameManager.getNodePrefix((Node) node);
			Integer count = defaultNameManager.getPrefixCount(prefix);
			String name;
			do {
				name = prefix + (count++);
			} while ( !isGoodDummyName(name) );
			dt.setName(name);
			dummyTransitions.put(name, dt);
			instancedNameManager.assign(dt);
		} else if (node instanceof STGPlace) {
			STGPlace p = (STGPlace)node;
			if (!p.isImplicit()) {
				defaultNameManager.setDefaultNameIfUnnamed((Node) node);
			}
		} else {
			defaultNameManager.setDefaultNameIfUnnamed((Node) node);
		}
	}

	private void renameSignalTransition(SignalTransition t, String signalName) {
		signalTransitions.remove(t.getSignalName(), t);
		t.setSignalName(signalName);
		signalTransitions.put(t.getSignalName(), t);
	}

	private void renameDummyTransition(DummyTransition t, String name) {
		dummyTransitions.remove(t.getName(), t);
		t.setName(name);
		dummyTransitions.put(t.getName(), t);
	}


	public void setName(Node node, String s, boolean forceInstance) {
		if (node instanceof STGPlace) {
			// do not set a name for an implicit place
			if (((STGPlace) node).isImplicit()) return;
		}

		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			try {
				final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(s);
				if (r == null) {
					throw new ArgumentException (s + " is not a valid signal transition label");
				}
				instancedNameManager.assign(st, Pair.of(r.getFirst() + r.getSecond(), r.getThird()), forceInstance);
				renameSignalTransition(st, r.getFirst());
				st.setDirection(r.getSecond());
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			} catch (ArgumentException e) {
				if (Identifier.isValid(s)) {
					instancedNameManager.assign(st, s + st.getDirection());
					renameSignalTransition(st, s);
				} else {
					throw new ArgumentException ("\"" + s + "\" is not a valid signal transition label.");
				}
			}
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;
			try {
				final Pair<String,Integer> r = LabelParser.parseDummyTransition(s);
				if (r==null) {
					throw new ArgumentException (s + " is not a valid transition label");
				}
				if (r.getSecond() != null) {
					instancedNameManager.assign(dt, r, forceInstance);
				} else {
					instancedNameManager.assign(dt, r.getFirst());
				}
				renameDummyTransition(dt, r.getFirst());
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			}
		} else {
			defaultNameManager.setName(node, s);
		}
	}

	public Pair<String, Integer> getNamePair(Node node) {
		if (node instanceof Transition)
			return instancedNameManager.getInstance(node);
		return null;
	}

	@Override
	public void setName(Node node, String s) {
		setName(node, s, false);
	}

	@Override
	public String getName (Node node) {

		if (node instanceof Transition) {
			Pair<String, Integer> instance = instancedNameManager.getInstance(node);

			if (instance.getSecond().equals(0))
				return instance.getFirst();
			else
				return instance.getFirst() + "/" + instance.getSecond();


		} else {

			if (node instanceof STGPlace && ((STGPlace)node).isImplicit()) {
				return null;
			}

			return defaultNameManager.getName(node);
		}
	}

	@Override
	public boolean isNamed(Node t) {
		return defaultNameManager.isNamed(t);
	}


	public Node get (String name) {

		Pair<String, Integer> instancedName = LabelParser.parseInstancedTransition(name);

		if (instancedName != null)	{

			if (instancedName.getSecond() == null) {
				instancedName = Pair.of(instancedName.getFirst(), 0);
			}
			Node node = instancedNameManager.getObject(instancedName);
			if (node != null) {
				return node;
			}
		}

		Node ret = defaultNameManager.get(name);
		return ret;

	}

	public void remove (Node n) {
		defaultNameManager.remove(n);
	}



}
