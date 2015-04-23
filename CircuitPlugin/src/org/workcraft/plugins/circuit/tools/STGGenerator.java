package org.workcraft.plugins.circuit.tools;

import static org.workcraft.util.Geometry.add;
import static org.workcraft.util.Geometry.subtract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public class STGGenerator {

	static class ContactSTG {
		public final VisualPlace p0;
		public final VisualPlace p1;

		public ContactSTG(VisualPlace p0, VisualPlace p1) {
			this.p0 = p0;
			this.p1 = p1;
		}
	}

	private static final double xScaling = 4;
	private static final double yScaling = 4;
	// store created containers in a separate map
	private static HashMap<String, Container> createdContainers = null;

	static void setPosition(Movable node, Point2D point) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}

	private static ContactSTG generatePlaces(VisualCircuit circuit, VisualSTG stg, VisualContact contact) {
		String contactName = CircuitUtils.getContactName(circuit, contact);

		String path = NamespaceHelper.getParentReference(circuit.getMathModel().getNodeReference(contact.getReferencedComponent()));
		Container curContainer = (Container)createdContainers.get(path);
		while (curContainer==null) {
			path = NamespaceHelper.getParentReference(path);
			curContainer = (Container)createdContainers.get(path);
		}


		VisualPlace zeroPlace = stg.createPlace(contactName+"_0", curContainer);
		zeroPlace.setLabel(contactName+"=0");

		if (!contact.getReferencedContact().getInitToOne()) {
			zeroPlace.getReferencedPlace().setTokens(1);
		}

		VisualPlace onePlace = stg.createPlace(contactName+"_1", curContainer);
		onePlace.setLabel(contactName+"=1");
		if (contact.getReferencedContact().getInitToOne()) {
			onePlace.getReferencedPlace().setTokens(1);
		}

		return new ContactSTG(zeroPlace, onePlace);
	}

	public static void attachConnections(VisualCircuit circuit, VisualComponent component, ContactSTG cstg) {
		if (component instanceof VisualContact) {
			VisualContact vc = (VisualContact)component;
			vc.setReferencedOnePlace(cstg.p1.getReferencedPlace());
			vc.setReferencedZeroPlace(cstg.p0.getReferencedPlace());
		}

		for (Connection c: circuit.getConnections(component)) {
			if ((c.getFirst() == component) && (c instanceof VisualCircuitConnection)) {

				((VisualCircuitConnection)c).setReferencedOnePlace(cstg.p1.getReferencedPlace());
				((VisualCircuitConnection)c).setReferencedZeroPlace(cstg.p0.getReferencedPlace());

				if (c.getSecond() instanceof VisualJoint) {
					VisualJoint vj = (VisualJoint)c.getSecond();
					vj.setReferencedOnePlace(cstg.p1.getReferencedPlace());
					vj.setReferencedZeroPlace(cstg.p0.getReferencedPlace());

					attachConnections(circuit, (VisualJoint)c.getSecond(), cstg);
				}

				if (c.getSecond() instanceof VisualContact) {
					attachConnections(circuit, (VisualContact)c.getSecond(), cstg);
				}
			}
		}
	}

	public synchronized static VisualSTG generate(VisualCircuit circuit) {
		try {
			VisualSTG stg = new VisualSTG(new STG());

			// first, create the same page structure
			NamespaceHelper.copyPageStructure(circuit, stg);
			createdContainers = NamespaceHelper.getRefToPageMapping(stg);

			Map<Contact, VisualContact> targetDrivers = new HashMap<Contact, VisualContact>();
			Map<VisualContact, ContactSTG> drivers = new HashMap<VisualContact, ContactSTG>();

			// generate all possible drivers and fill out the targets
			LinkedList<ContactSTG> contactStgs = new LinkedList<ContactSTG>();
			for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
				ContactSTG cstg = null;
				if(contact.isDriver()) {
					// if it is a driver, add it to the list of drivers
					cstg = generatePlaces(circuit, stg, contact);
					drivers.put(contact, cstg);
					// attach driven wires to the place
					attachConnections(circuit, contact, cstg);
					// put itself on a target list as well, so that it can be addressed by other drivers
					targetDrivers.put(contact.getReferencedContact(), contact);
				} else {
					// if not a driver, find related driver, add to the map of targets
					VisualContact driver = CircuitUtils.findDriver(circuit, contact);
					if (driver == null) {
						// if target driver was not found, create artificial one that looks like input
						driver = contact;
						cstg = generatePlaces(circuit, stg, contact);
						drivers.put(driver, cstg);
						// attach driven wires to the place
						attachConnections(circuit, contact, cstg);
					}
					targetDrivers.put(contact.getReferencedContact(), driver);
				}
				if (cstg != null) {
					contactStgs.add(cstg);
				}
			}

			// Generate implementation for each of the drivers
			for(VisualContact driver : drivers.keySet()) {
				BooleanFormula setFunc = null;
				BooleanFormula resetFunc = null;
				Type signalType = Type.INPUT;
				if (driver instanceof VisualFunctionContact) {
					// Determine signal type
					VisualFunctionContact contact = (VisualFunctionContact)driver;
					signalType = CircuitUtils.getSignalType(circuit, contact);

					if (contact.isOutput() && contact.isPort()) {
						// Driver of the primary output port
						VisualContact outputDriver = CircuitUtils.findDriver(circuit, contact);
						if (outputDriver != null) {
							setFunc = outputDriver.getReferencedContact();
						}
					} else {
						// Function based driver
						setFunc = contact.getSetFunction();
						resetFunc = contact.getResetFunction();
					}
				}
				// Create complementary set/reset if only one of them is defined
				if ((setFunc != null) && (resetFunc == null)) {
					resetFunc = new DumbBooleanWorker().not(setFunc);
				} else if ((setFunc == null) && (resetFunc != null)) {
					setFunc = new DumbBooleanWorker().not(resetFunc);
				}
				Dnf setDnf = DnfGenerator.generate(setFunc);
				Dnf resetDnf = DnfGenerator.generate(resetFunc);
				implementDriver(circuit, stg, driver, drivers, targetDrivers, setDnf, resetDnf, signalType);
			}
			if (CircuitSettings.getSimplifyStg()) {
				// remove dead transitions
				simplifyStg(stg, circuit, contactStgs);
			}
			return stg;
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private static void simplifyStg(VisualSTG stg, VisualCircuit circuit, LinkedList<ContactSTG> contactStgs) {
		LinkedList<Transition> redundantTransitions = new LinkedList<>();
		for (ContactSTG cstg: contactStgs) {
			HashSet<Node> postset = new HashSet<Node>(stg.getPostset(cstg.p0));
			postset.retainAll(stg.getPostset(cstg.p1));
			for (Node node: postset) {
				if (node instanceof VisualTransition) {
					stg.remove(node);
					VisualTransition t = (VisualTransition)node;
					redundantTransitions.add(t.getReferencedTransition());
				}
			}
		}
		for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
			contact.getReferencedTransitions().removeAll(redundantTransitions);
		}
	}

	private static void implementDriver(VisualCircuit circuit, VisualSTG stg,
			VisualContact contact,
			Map<VisualContact, ContactSTG> drivers,
			Map<Contact, VisualContact> targetDrivers, Dnf set, Dnf reset, SignalTransition.Type ttype) throws InvalidConnectionException {

		AffineTransform transform = TransformHelper.getTransformToRoot(contact);
		Point2D center = new Point2D.Double(xScaling*(transform.getTranslateX()+contact.getX()), yScaling*(transform.getTranslateY()+contact.getY()));

		Point2D direction;
		Point2D pOffset;
		Point2D plusDirection;
		Point2D minusDirection;

		VisualContact.Direction dir = contact.getDirection();
		if (contact.isInput()) {
			dir = dir.flip();
		}

		switch(dir) {
			case WEST:
				direction		= new Point2D.Double( 6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case EAST:
				direction		= new Point2D.Double(-6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case NORTH:
				direction		= new Point2D.Double( 6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case SOUTH:
				direction		= new Point2D.Double(-6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			default: throw new RuntimeException();
		}

		String signalName = CircuitUtils.getContactName(circuit, contact);

		ContactSTG p = drivers.get(contact);

		if (p == null)
			throw new RuntimeException("Places for driver " + signalName + " cannot be found.");

		Collection<Node> nodes = new LinkedList<Node>();

		setPosition(p.p1, add(center, pOffset));
		setPosition(p.p0, subtract(center, pOffset));

		nodes.add(p.p1);
		nodes.add(p.p0);

		contact.getReferencedTransitions().clear();

		nodes.addAll(buildTransitions(contact, stg, circuit, drivers, targetDrivers,
				set, add(add(center, direction), pOffset), plusDirection,
				signalName, ttype, SignalTransition.Direction.PLUS, p.p0, p.p1));

		nodes.addAll(buildTransitions(contact, stg, circuit, drivers, targetDrivers,
				reset, subtract(add(center, direction), pOffset),  minusDirection,
				signalName, ttype, SignalTransition.Direction.MINUS, p.p1, p.p0));

		Container currentLevel = null;
		Container oldLevel = stg.getCurrentLevel();

		for (Node node:nodes) {
			if (currentLevel==null)
				currentLevel = (Container)node.getParent();

			if (currentLevel!=node.getParent())
				throw new RuntimeException("Current level is not the same among the processed nodes");
		}


		stg.setCurrentLevel(currentLevel);
		stg.select(nodes);
		stg.groupSelection();
		stg.setCurrentLevel(oldLevel);
	}

	private static LinkedList<VisualNode> buildTransitions(VisualContact parentContact,
			VisualSTG stg, VisualCircuit circuit, Map<VisualContact, ContactSTG> drivers, Map<Contact, VisualContact> targetDrivers,
			Dnf function, Point2D baseOffset, Point2D transitionOffset,
			String signalName, SignalTransition.Type type, Direction transitionDirection,
			VisualPlace preset, VisualPlace postset) throws InvalidConnectionException {

		LinkedList<VisualNode> nodes = new LinkedList<VisualNode>();


		String path = NamespaceHelper.getParentReference(circuit.getMathModel().getNodeReference(parentContact.getReferencedComponent()));
		Container curContainer = (Container)createdContainers.get(path);
		while (curContainer==null) {
			path = NamespaceHelper.getParentReference(path);
			curContainer = (Container)createdContainers.get(path);
		}


		TreeSet<DnfClause> clauses = new TreeSet<DnfClause>(
				new Comparator<DnfClause>() {
					@Override
					public int compare(DnfClause arg0, DnfClause arg1) {
						String st1 = FormulaToString.toString(arg0);
						String st2 = FormulaToString.toString(arg1);
						return st1.compareTo(st2);
					}
				});

		clauses.addAll(function.getClauses());


		for(DnfClause clause : clauses) {
			VisualSignalTransition transition = stg.createSignalTransition(signalName, type, transitionDirection, curContainer);
			nodes.add(transition);
			parentContact.getReferencedTransitions().add(transition.getReferencedTransition());

			setPosition(transition, baseOffset);

			stg.connect(transition, postset);
			stg.connect(preset, transition);
			transition.setLabel(FormulaToString.toString(clause));

			baseOffset = add(baseOffset, transitionOffset);

			HashSet<VisualPlace> placesToRead = new HashSet<VisualPlace>();

			for (Literal literal : clause.getLiterals()) {
				Contact targetContact = (Contact)literal.getVariable();
				VisualContact driverContact = targetDrivers.get(targetContact);
				ContactSTG source = drivers.get(driverContact);
				if(source == null) {
					throw new RuntimeException("No source for " + circuit.getMathModel().getName(targetContact) + " while generating " + signalName);
				}
				VisualPlace place = literal.getNegation() ? source.p0 : source.p1;
				placesToRead.add(place);
			}
			if(placesToRead.remove(preset)) {
				System.out.println(String.format("warning: signal %s depends on itself", signalName));
			}
			for(VisualPlace place : placesToRead) {
				// FIXME: Why duplicate arcs would be created in the first place?
				if(stg.getConnection(place, transition) == null) {
					stg.connect(place, transition);
				}
				if(stg.getConnection(transition, place) == null) {
					stg.connect(transition, place);
				}
			}
		}

		return nodes;
	}

}
