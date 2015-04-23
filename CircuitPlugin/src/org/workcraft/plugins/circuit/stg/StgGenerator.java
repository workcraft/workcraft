package org.workcraft.plugins.circuit.stg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class StgGenerator {
	private static final double SCALE_X = 4.0;
	private static final double SCALE_Y = 4.0;
	private static final Point2D OFFSET_P1 = new Point2D.Double(0.0, -1.0);
	private static final Point2D OFFSET_P0 = new Point2D.Double(0.0, 1.0);
	private static final Point2D OFFSET_INIT_PLUS = new Point2D.Double(0.0, -1.0);
	private static final Point2D OFFSET_INIT_MINUS = new Point2D.Double(0.0, 1.0);
	private static final Point2D OFFSET_INC_PLUS = new Point2D.Double(0.0, -2.0);
	private static final Point2D OFFSET_INC_MINUS = new Point2D.Double(0.0, 2.0);

	private final VisualCircuit circuit;
	private final VisualSTG stg;

	// store created containers in a separate map
	private static HashMap<String, Container> refToContainerMap = null;

	private Map<VisualContact, VisualContact> driverMap = new HashMap<VisualContact, VisualContact>();
	private Map<VisualContact, SignalStg> contactMap = new HashMap<VisualContact, SignalStg>();

	public StgGenerator(VisualCircuit circuit) {
		this.circuit = circuit;
		this.stg = new VisualSTG(new STG());
		convertPages();
		buildDriverMap();
		generatePlaces();
		generateTransitions();
		groupContacts();
		if (CircuitSettings.getSimplifyStg()) {
			simplifyStg(); // remove dead transitions
		}
	}

	private void convertPages() {
		NamespaceHelper.copyPageStructure(circuit, stg);
		refToContainerMap = NamespaceHelper.getRefToPageMapping(stg);
	}

	private void buildDriverMap() {
		for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
			VisualContact driver = null;
			if (!contact.isDriver()) {
				driver = CircuitUtils.findDriver(circuit, contact);
			}
			if (driver == null) {
				driver = contact;
			}
			driverMap.put(contact, driver);
		}
	}

	private void generatePlaces() {
		HashSet<VisualContact> drivers = new HashSet<>(driverMap.values());
		for (VisualContact contact: drivers) {
			SignalStg signalStg = generatePlaces(circuit, stg, contact);
			contactMap.put(contact, signalStg);
		}
	}

	private void generateTransitions() {
		for(VisualContact driver : contactMap.keySet()) {
			BooleanFormula setFunc = null;
			BooleanFormula resetFunc = null;
			if (driver instanceof VisualFunctionContact) {
				// Determine signal type
				VisualFunctionContact contact = (VisualFunctionContact)driver;

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
			buildTransitions(driver, setDnf, Direction.PLUS);

			Dnf resetDnf = DnfGenerator.generate(resetFunc);
			buildTransitions(driver, resetDnf, Direction.MINUS);
		}
	}

	public VisualSTG getStg() {
		return stg;
	}

	public SignalStg getContactSTG(VisualContact contact) {
		return contactMap.get(contact);
	}

	private void simplifyStg() {
		for (SignalStg contactStg: contactMap.values()) {
			HashSet<Node> deadPostset = new HashSet<Node>(stg.getPostset(contactStg.P0));
			deadPostset.retainAll(stg.getPostset(contactStg.P1));
			for (Node node: deadPostset) {
				if (node instanceof VisualTransition) {
					contactStg.Rs.remove(node);
					contactStg.Fs.remove(node);
					stg.remove(node);
				}
			}
		}
	}

	static void setPosition(Movable node, Point2D point) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}

	private SignalStg generatePlaces(VisualCircuit circuit, VisualSTG stg, VisualContact contact) {
		String nodeReference = circuit.getMathModel().getNodeReference(contact.getReferencedComponent());
		String parentReference = NamespaceHelper.getParentReference(nodeReference);
		Container curContainer = (Container)refToContainerMap.get(parentReference);
		while (curContainer == null) {
			parentReference = NamespaceHelper.getParentReference(parentReference);
			curContainer = (Container)refToContainerMap.get(parentReference);
		}

		String contactName = CircuitUtils.getContactName(circuit, contact);

		VisualPlace zeroPlace = stg.createPlace(contactName + "_0", curContainer);
		zeroPlace.setLabel(contactName + "=0");
		if (!contact.getReferencedContact().getInitToOne()) {
			zeroPlace.getReferencedPlace().setTokens(1);
		}

		VisualPlace onePlace = stg.createPlace(contactName + "_1", curContainer);
		onePlace.setLabel(contactName + "=1");
		if (contact.getReferencedContact().getInitToOne()) {
			onePlace.getReferencedPlace().setTokens(1);
		}


		Point2D center = getPosition(contact);
		setPosition(onePlace, Geometry.add(center, OFFSET_P1));
		setPosition(zeroPlace, Geometry.add(center, OFFSET_P0));

		return new SignalStg(zeroPlace, onePlace);
	}

//	public void attachConnections(VisualComponent component, SignalStg signalStg) {
//		if (!contactMap.containsKey(component)) {
//			if (component instanceof VisualContact) {
//				VisualContact contact = (VisualContact)component;
//				contactMap.put(contact,  signalStg);
//			}
//
//			for (Connection connection: circuit.getConnections(component)) {
//				if ((connection.getFirst() == component) && (connection instanceof VisualCircuitConnection)) {
//					contactMap.put((VisualCircuitConnection)connection, signalStg);
//
//					if (connection.getSecond() instanceof VisualJoint) {
//						VisualJoint joint = (VisualJoint)connection.getSecond();
//						attachConnections(joint, signalStg);
//					}
//
//					if (connection.getSecond() instanceof VisualContact) {
//						VisualContact contact = (VisualContact)connection.getSecond();
//						attachConnections(contact, signalStg);
//					}
//				}
//			}
//		}
//	}

	private Point2D getPosition(VisualContact contact) {
		AffineTransform transform = TransformHelper.getTransformToRoot(contact);
		Point2D position = new Point2D.Double(
				SCALE_X * (transform.getTranslateX() + contact.getX()),
				SCALE_Y * (transform.getTranslateY() + contact.getY()));
		return position;
	}

	private void buildTransitions(VisualContact contact, Dnf dnf, Direction direction) {
		SignalStg contactStg = contactMap.get(contact);
		Point2D position = Geometry.add(getPosition(contact), getDirectionOffset(contact));
		Point2D initOffset = (direction == Direction.PLUS ? OFFSET_INIT_PLUS : OFFSET_INIT_MINUS);
		Point2D incOffset = (direction == Direction.PLUS ? OFFSET_INC_PLUS : OFFSET_INC_MINUS);
		VisualPlace predPlace = (direction == Direction.PLUS ? contactStg.P0 : contactStg.P1);
		VisualPlace succPlace = (direction == Direction.PLUS ? contactStg.P1 : contactStg.P0);
		HashSet<VisualSignalTransition> transitions = (direction == Direction.PLUS ? contactStg.Rs : contactStg.Fs);

		TreeSet<DnfClause> clauses = new TreeSet<DnfClause>(
				new Comparator<DnfClause>() {
					@Override
					public int compare(DnfClause arg0, DnfClause arg1) {
						String st1 = FormulaToString.toString(arg0);
						String st2 = FormulaToString.toString(arg1);
						return st1.compareTo(st2);
					}
				});

		clauses.addAll(dnf.getClauses());

		Container container = getContainer(contact);
		String signalName = CircuitUtils.getContactName(circuit, contact);
		SignalTransition.Type signalType = CircuitUtils.getSignalType(circuit, contact);
		position = Geometry.add(position, initOffset);
		for(DnfClause clause : clauses) {
			VisualSignalTransition transition = stg.createSignalTransition(signalName, signalType, direction, container);
			setPosition(transition, position);
			transition.setLabel(FormulaToString.toString(clause));
			transitions.add(transition);
			position = Geometry.add(position, incOffset);

			try {
				stg.connect(predPlace, transition);
				stg.connect(transition, succPlace);
			} catch (InvalidConnectionException e) {
				throw new RuntimeException(e);
			}


			HashSet<VisualPlace> placesToRead = new HashSet<VisualPlace>();
			for (Literal literal : clause.getLiterals()) {
				BooleanVariable b = literal.getVariable();
				VisualContact targetContact = CircuitUtils.getVisualContact(circuit, (Contact)b);
				if (targetContact == null) {
					throw new RuntimeException("No source for " + circuit.getMathModel().getName(targetContact) + " while generating " + signalName);
				}

				VisualContact driverContact = driverMap.get(targetContact);
				SignalStg signalStg = contactMap.get(driverContact);
				if (signalStg == null) {
					throw new RuntimeException("No source for " + circuit.getMathName(targetContact) + " while generating " + signalName);
				}
				VisualPlace place = literal.getNegation() ? signalStg.P0 : signalStg.P1;
				placesToRead.add(place);
			}

			if (placesToRead.remove(predPlace)) {
				System.out.println(String.format("warning: signal %s depends on itself", signalName));
			}

			for (VisualPlace place : placesToRead) {
				// FIXME: Why duplicate arcs would be created in the first place?
				try {
					if(stg.getConnection(place, transition) == null) {
						stg.connect(place, transition);
					}
					if(stg.getConnection(transition, place) == null) {
						stg.connect(transition, place);
					}
				} catch (InvalidConnectionException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private Container getContainer(VisualContact contact) {
		String nodeReference = circuit.getMathModel().getNodeReference(contact.getReferencedComponent());
		String path = NamespaceHelper.getParentReference(nodeReference);
		Container curContainer = (Container)refToContainerMap.get(path);
		while (curContainer==null) {
			path = NamespaceHelper.getParentReference(path);
			curContainer = (Container)refToContainerMap.get(path);
		}
		return curContainer;
	}

	private Point2D getDirectionOffset(VisualContact contact) {
		VisualContact.Direction direction = contact.getDirection();
		if (contact.isInput()) {
			direction = direction.flip();
		}
		switch (direction) {
			case WEST: return new Point2D.Double( 6.0, 0.0);
			case EAST: return new Point2D.Double(-6.0, 0.0);
			case NORTH: return new Point2D.Double( 6.0, 0.0);
			case SOUTH: return new Point2D.Double(-6.0, 0.0);
			default: return new Point2D.Double( 0.0, 0.0);
		}
	}

	private void groupContacts() {
		for(SignalStg contactStg: new HashSet<>(contactMap.values())) {
			Collection<Node> nodesToGroup = new LinkedList<Node>();
			nodesToGroup.add(contactStg.P1);
			nodesToGroup.add(contactStg.P0);
			nodesToGroup.addAll(contactStg.Rs);
			nodesToGroup.addAll(contactStg.Fs);

			Container currentLevel = null;
			Container oldLevel = stg.getCurrentLevel();
			for (Node node:nodesToGroup) {
				if (currentLevel == null) {
					currentLevel = (Container)node.getParent();
				}
				if (currentLevel != node.getParent()) {
					throw new RuntimeException("Current level is not the same among the processed nodes");
				}
			}

			stg.setCurrentLevel(currentLevel);
			stg.select(nodesToGroup);
			stg.groupSelection();
			stg.setCurrentLevel(oldLevel);
		}
	}

}
