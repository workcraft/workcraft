package org.workcraft.plugins.circuit.stg;

import static org.workcraft.util.Geometry.add;
import static org.workcraft.util.Geometry.subtract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Hierarchy;

public class CircuitPetriNetGenerator {

	static class ContactSTG
	{
		public ContactSTG(VisualPlace p0, VisualPlace p1)
		{
			this.p0 = p0;
			this.p1 = p1;
		}
		public final VisualPlace p0;
		public final VisualPlace p1;
	}

	private static final double xScaling = 8;
	private static final double yScaling = 4;

	static void setPosition(Movable node, Point2D point) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}

	public static VisualContact findDriver(VisualCircuit circuit, VisualContact target) {

		Set<Node> neighbours = new HashSet<Node>(circuit.getPreset(target));

		while (neighbours.size()>=1) {

			if(neighbours.size() != 1) throw new RuntimeException("Found more than one potential driver for target "+getContactName(circuit, target)+"!");

			Node node = neighbours.iterator().next();

			if (VisualContact.isDriver(node)) {
				// if it is a driver, return it
				return (VisualContact)node;
			}

			// continue searching otherwise
			neighbours = new HashSet<Node>(circuit.getPreset(node));
		}

		return null;
	}

	private static ContactSTG generatePlaces(VisualCircuit circuit,
			VisualSTG stg, VisualContact contact) {

		String contactName = getContactName(circuit, contact);
		VisualPlace zeroPlace = stg.createPlace(contactName+"_0");
		zeroPlace.setLabel(contactName+"=0");
		zeroPlace.setTokens(1);

		VisualPlace onePlace = stg.createPlace(contactName+"_1");
		onePlace.setLabel(contactName+"=1");

		ContactSTG contactSTG = new ContactSTG(zeroPlace, onePlace);

		return contactSTG;
	}

	public static VisualSTG generate(VisualCircuit circuit) {
		try {
			VisualSTG stg = new VisualSTG(new STG());

			Map<Contact, VisualContact> targetDrivers = new HashMap<Contact, VisualContact>();
			Map<VisualContact, ContactSTG> drivers = new HashMap<VisualContact, ContactSTG>();

			// generate all possible drivers and fill out the targets
			for(VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {

				if(VisualContact.isDriver(contact)) {
					// if it is a driver, add it to the list of drivers
					drivers.put(contact, generatePlaces(circuit, stg, contact));

					// put itself on a target list as well, so that it cab be addressed by other drivers
					targetDrivers.put(contact.getReferencedContact(), contact);
				} else {
					// if not a driver, find related driver, add to the map of targets
					VisualContact driver = findDriver(circuit, contact);

					if (driver==null) {
						// if target driver was not found, create artificial one that looks like input
						driver = new VisualContact(new Contact(IOType.INPUT), VisualContact.flipDirection(contact.getDirection()), contact.getName());
						driver.setTransform(contact.getTransform());
						driver.setParent(contact.getParent());

						drivers.put(driver, generatePlaces(circuit, stg, contact));
					}

					targetDrivers.put(contact.getReferencedContact(), driver);
				}
			}

			// generate implementation for each of the drivers
			for(VisualContact c : drivers.keySet())
			{
				if (c instanceof VisualFunctionContact) {
					// function based driver
					VisualFunctionContact contact = (VisualFunctionContact)c;
					Dnf set = DnfGenerator.generate(contact.getFunction().getSetFunction());

					Dnf reset = null;
					BooleanOperations.worker = new DumbBooleanWorker();

					if (reset!=null)
						reset = DnfGenerator.generate(contact.getFunction().getResetFunction());
					else
						reset = DnfGenerator.generate(BooleanOperations.worker.not(contact.getFunction().getSetFunction()));

					SignalTransition.Type ttype = SignalTransition.Type.OUTPUT;

					if (contact.getParent()!=null&&
						contact.getParent() instanceof VisualCircuitComponent&&
						((VisualCircuitComponent)contact.getParent()).getIsEnvironment()) ttype = SignalTransition.Type.INPUT;

					implementDriver(circuit, stg, contact, drivers, targetDrivers, set, reset, ttype);

				} else {
					// some generic driver implementation otherwise
					Dnf set = new Dnf(new DnfClause());
					Dnf reset = new Dnf(new DnfClause());
					implementDriver(circuit, stg, c, drivers, targetDrivers, set, reset, SignalTransition.Type.INPUT);
				}
			}

			return stg;
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}


	private static void implementDriver(VisualCircuit circuit, VisualSTG stg,
			VisualContact contact,
			Map<VisualContact, ContactSTG> drivers,
			Map<Contact, VisualContact> targetDrivers, Dnf set, Dnf reset, SignalTransition.Type ttype) throws InvalidConnectionException {

		AffineTransform transform = TransformHelper.getTransformToAncestor(contact, circuit.getRoot());
		Point2D center = new Point2D.Double(xScaling*(transform.getTranslateX()+contact.getX()), yScaling*(transform.getTranslateY()+contact.getY()));

		Point2D direction;
		Point2D pOffset;
		Point2D plusDirection;
		Point2D minusDirection;

//		int maxC = Math.max(set.getClauses().size(), reset.getClauses().size());

		switch(contact.getDirection()) {
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

		String signalName = getContactName(circuit, contact);

		ContactSTG p = drivers.get(contact);

		if (p == null)
			throw new RuntimeException("Places for driver " + signalName + " cannot be found.");

		Collection<Node> nodes = new LinkedList<Node>();

		setPosition(p.p1, add(center, pOffset));
		setPosition(p.p0, subtract(center, pOffset));

		nodes.add(p.p1);
		nodes.add(p.p0);

		nodes.addAll(buildTransitions(stg, circuit, drivers, targetDrivers,
				set,
				add(add(center, direction), pOffset), plusDirection,
				signalName, ttype, SignalTransition.Direction.PLUS, p.p0, p.p1));

		nodes.addAll(buildTransitions(stg, circuit, drivers, targetDrivers,
				reset,
				subtract(add(center, direction), pOffset),  minusDirection,
				signalName, ttype, SignalTransition.Direction.MINUS, p.p1, p.p0));

		stg.groupCollection(nodes);


	}

	private static LinkedList<VisualNode> buildTransitions(
			VisualSTG stg, VisualCircuit circuit, Map<VisualContact, ContactSTG> drivers, Map<Contact, VisualContact> targetDrivers,
			Dnf function, Point2D baseOffset, Point2D transitionOffset,
			String signalName, SignalTransition.Type type, Direction transitionDirection,
			VisualPlace preset, VisualPlace postset) throws InvalidConnectionException {

		LinkedList<VisualNode> nodes = new LinkedList<VisualNode>();

		for(DnfClause clause : function.getClauses())
		{
			VisualSignalTransition transition = stg.createSignalTransition(signalName, type, transitionDirection);
			nodes.add(transition);

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

				if(source == null)
					throw new RuntimeException("No source for " + targetContact.getName() + " while generating " + signalName);

				VisualPlace p = literal.getNegation() ? source.p0 : source.p1;

				placesToRead.add(p);
			}

			if(placesToRead.remove(preset))
				System.out.println(String.format("warning: signal %s depends on itself", signalName));

			for(VisualPlace p : placesToRead) {
				stg.connect(p, transition);
				stg.connect(transition, p);
			}
		}

		return nodes;
	}

	private static String getContactName(VisualCircuit circuit, VisualContact contact) {
		String prefix = "";
		Node parent = contact.getParent();
		if (parent instanceof VisualFunctionComponent)
			prefix = ((VisualFunctionComponent)parent).getName()+"_";
		return prefix+contact.getName();
	}
}
