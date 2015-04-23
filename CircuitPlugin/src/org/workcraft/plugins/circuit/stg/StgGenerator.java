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

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
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
import org.workcraft.util.TwoWayMap;

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

	private final HashMap<String, Container> refToPageMap;
	private final Map<VisualNode, VisualContact> nodeToDriverMap;
	private final TwoWayMap<VisualContact, SignalStg> driverToStgMap;

	public StgGenerator(VisualCircuit circuit) {
		this.circuit = circuit;
		this.stg = new VisualSTG(new STG());
		HashSet<VisualContact> drivers = identifyDrivers();
		this.nodeToDriverMap = associateDrivers(drivers);
		this.refToPageMap = convertPages();
		this.driverToStgMap = convertDrivers(drivers);
		connectDrivers(drivers);
		groupStg();
		if (CircuitSettings.getSimplifyStg()) {
			simplifyStg(); // remove dead transitions
		}
	}

	public VisualSTG getStg() {
		return stg;
	}

	public boolean isDriver(VisualContact contact) {
		return driverToStgMap.containsKey(contact);

	}

	public SignalStg getSignalStg(VisualNode node) {
		SignalStg result = null;
		VisualContact driver = nodeToDriverMap.get(node);
		if (driver != null) {
			result = driverToStgMap.getValue(driver);
		}
		return result;
	}

	private HashMap<String, Container> convertPages() {
		NamespaceHelper.copyPageStructure(circuit, stg);
		return NamespaceHelper.getRefToPageMapping(stg);
	}

	private Container getContainer(VisualContact contact) {
		String nodeReference = circuit.getMathModel().getNodeReference(contact.getReferencedComponent());
		String parentReference = NamespaceHelper.getParentReference(nodeReference);
		Container container = (Container)refToPageMap.get(parentReference);
		while (container==null) {
			parentReference = NamespaceHelper.getParentReference(parentReference);
			container = (Container)refToPageMap.get(parentReference);
		}
		return container;
	}

	private HashSet<VisualContact> identifyDrivers() {
		HashSet<VisualContact> result = new HashSet<>();
		for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
			VisualContact driver = null;
			if (!contact.isDriver()) {
				driver = CircuitUtils.findDriver(circuit, contact);
			}
			if (driver == null) {
				driver = contact;
			}
			result.add(driver);
		}
		return result;
	}

	private HashMap<VisualNode, VisualContact> associateDrivers(HashSet<VisualContact> driverSet) {
		HashMap<VisualNode, VisualContact> result = new HashMap<>();
		for (VisualContact driver: driverSet) {
			propagateDriver(driver, driver, result);
		}
		return result;
	}

	private void propagateDriver(VisualNode node, VisualContact driver, HashMap<VisualNode, VisualContact> nodeToDriverMap) {
		if (!nodeToDriverMap.containsKey(node)) {
			nodeToDriverMap.put(node, driver);

			for (Connection connection: circuit.getConnections(node)) {
				if ((connection.getFirst() == node) && (connection instanceof VisualCircuitConnection)) {
					nodeToDriverMap.put((VisualCircuitConnection)connection, driver);
					Node succNode = connection.getSecond();
					if (succNode instanceof VisualNode) {
						propagateDriver((VisualNode)succNode, driver, nodeToDriverMap);
					}
				}
			}
		}
	}

	private TwoWayMap<VisualContact, SignalStg> convertDrivers(HashSet<VisualContact> drivers) {
		TwoWayMap<VisualContact, SignalStg> result = new TwoWayMap<>();
		for (VisualContact driver: drivers) {
			Container container = getContainer(driver);
			String contactName = CircuitUtils.getContactName(circuit, driver);
			Point2D center = getPosition(driver);

			VisualPlace zeroPlace = stg.createPlace(contactName + "_0", container);
			zeroPlace.setLabel(contactName + "=0");
			setPosition(zeroPlace, Geometry.add(center, OFFSET_P0));
			if (!driver.getReferencedContact().getInitToOne()) {
				zeroPlace.getReferencedPlace().setTokens(1);
			}

			VisualPlace onePlace = stg.createPlace(contactName + "_1", container);
			onePlace.setLabel(contactName + "=1");
			setPosition(onePlace, Geometry.add(center, OFFSET_P1));
			if (driver.getReferencedContact().getInitToOne()) {
				onePlace.getReferencedPlace().setTokens(1);
			}

			SignalStg signalStg = new SignalStg(zeroPlace, onePlace);
			result.put(driver, signalStg);
		}
		return result;
	}

	private void connectDrivers(HashSet<VisualContact> drivers) {
		for (VisualContact driver: drivers) {
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
			createSignalStgTransitions(driver, setDnf, Direction.PLUS);

			Dnf resetDnf = DnfGenerator.generate(resetFunc);
			createSignalStgTransitions(driver, resetDnf, Direction.MINUS);
		}
	}

	private void setPosition(Movable node, Point2D point) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}

	private void createSignalStgTransitions(VisualContact driver, Dnf dnf, Direction direction) {
		SignalStg driverStg = driverToStgMap.getValue(driver);
		Point2D position = Geometry.add(getPosition(driver), getDirectionOffset(driver));
		Point2D initOffset = (direction == Direction.PLUS ? OFFSET_INIT_PLUS : OFFSET_INIT_MINUS);
		Point2D incOffset = (direction == Direction.PLUS ? OFFSET_INC_PLUS : OFFSET_INC_MINUS);
		VisualPlace predPlace = (direction == Direction.PLUS ? driverStg.P0 : driverStg.P1);
		VisualPlace succPlace = (direction == Direction.PLUS ? driverStg.P1 : driverStg.P0);
		HashSet<VisualSignalTransition> transitions = (direction == Direction.PLUS ? driverStg.Rs : driverStg.Fs);

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

		Container container = getContainer(driver);
		String signalName = CircuitUtils.getContactName(circuit, driver);
		SignalTransition.Type signalType = CircuitUtils.getSignalType(circuit, driver);
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
				BooleanVariable variable = literal.getVariable();
				VisualContact sourceContact = CircuitUtils.getVisualContact(circuit, (Contact)variable);
				VisualContact sourceDriver = nodeToDriverMap.get(sourceContact);
				SignalStg sourceDriverStg = driverToStgMap.getValue(sourceDriver);
				if (sourceDriverStg == null) {
					throw new RuntimeException("No source for " + circuit.getMathName(sourceContact) + " while generating " + signalName);
				}
				VisualPlace place = literal.getNegation() ? sourceDriverStg.P0 : sourceDriverStg.P1;
				placesToRead.add(place);
			}

			if (placesToRead.remove(predPlace)) {
				System.out.println("warning: signal " + signalName + " depends on itself");
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

	private Point2D getPosition(VisualContact contact) {
		AffineTransform transform = TransformHelper.getTransformToRoot(contact);
		Point2D position = new Point2D.Double(
				SCALE_X * (transform.getTranslateX() + contact.getX()),
				SCALE_Y * (transform.getTranslateY() + contact.getY()));
		return position;
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

	private void groupStg() {
		for (SignalStg signalStg: driverToStgMap.getValues()) {
			Collection<Node> nodesToGroup = new LinkedList<Node>();
			nodesToGroup.add(signalStg.P1);
			nodesToGroup.add(signalStg.P0);
			nodesToGroup.addAll(signalStg.Rs);
			nodesToGroup.addAll(signalStg.Fs);

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

	private void simplifyStg() {
		for (SignalStg contactStg: driverToStgMap.values()) {
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

}
