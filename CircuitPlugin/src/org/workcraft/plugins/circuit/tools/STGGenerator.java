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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.HierarchicalNames;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.VisualJoint;
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
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public class STGGenerator {

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

	private static final double xScaling = 4;
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

	private static ContactSTG generatePlaces(VisualCircuit circuit, VisualSTG stg, VisualContact contact) {
		String contactName = getContactName(circuit, contact);

		String path = HierarchicalNames.getParentReference(circuit.getMathModel().getNodeReference(contact.getReferencedComponent()));
		Container curContainer = (Container)createdContainers.get(path);
		while (curContainer==null) {
			path = HierarchicalNames.getParentReference(path);
			curContainer = (Container)createdContainers.get(path);
		}


		VisualPlace zeroPlace = stg.createPlace(contactName+"_0", curContainer);
		zeroPlace.setLabel(contactName+"=0");

		if (!contact.getInitOne()) {
			zeroPlace.getReferencedPlace().setTokens(1);
		}

		VisualPlace onePlace = stg.createPlace(contactName+"_1", curContainer);
		onePlace.setLabel(contactName+"=1");
		if (contact.getInitOne()) {
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
			if (c.getFirst()==component&&c instanceof VisualCircuitConnection) {

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



	// store created containers in a separate map
	private static HashMap<String, Node> createdContainers = null;
	private static void copyPages(VisualSTG targetModel, Container targetContainer, VisualCircuit sourceModel, Container sourceContainer) {
		HashMap<Container, Container> toProcess = new HashMap<Container, Container>();

		for (Node vn: sourceContainer.getChildren()) {
			if (vn instanceof VisualPage) {

				VisualPage vp = (VisualPage)vn;
				String name = sourceModel.getMathModel().getName(vp.getReferencedComponent());

				PageNode np2 = new PageNode();
				VisualPage vp2 = new VisualPage(np2);
				targetContainer.add(vp2);
				AbstractVisualModel.getMathContainer(targetModel, targetContainer).add(np2);
				targetModel.getMathModel().setName(np2, name);
				createdContainers.put(targetModel.getMathModel().getNodeReference(np2), vp2);


				toProcess.put(vp, vp2);
			}
		}

		for (Entry<Container, Container> en: toProcess.entrySet()) {
			copyPages(targetModel, en.getValue(), sourceModel, en.getKey());
		}
	}

	public synchronized static VisualSTG generate(VisualCircuit circuit) {
		try {
			VisualSTG stg = new VisualSTG(new STG());

			// first, create the same page structure
			createdContainers = new HashMap<String, Node>();
			createdContainers.put("", stg.getRoot());
			copyPages(stg, stg.getRoot(), circuit, circuit.getRoot());


			Map<Contact, VisualContact> targetDrivers = new HashMap<Contact, VisualContact>();
			Map<VisualContact, ContactSTG> drivers = new HashMap<VisualContact, ContactSTG>();

			// generate all possible drivers and fill out the targets
			for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
				ContactSTG cstg;

				if(VisualContact.isDriver(contact)) {
					// if it is a driver, add it to the list of drivers
					cstg = generatePlaces(circuit, stg, contact);
					drivers.put(contact, cstg);

					// attach driven wires to the place
					attachConnections(circuit, contact, cstg);

					// put itself on a target list as well, so that it can be addressed by other drivers
					targetDrivers.put(contact.getReferencedContact(), contact);
				} else {
					// if not a driver, find related driver, add to the map of targets
					VisualContact driver = findDriver(circuit, contact);

					if (driver==null) {
						// if target driver was not found, create artificial one that looks like input
						//driver = new VisualContact(new Contact(IOType.INPUT), VisualContact.flipDirection(contact.getDirection()), contact.getName());
						//driver.setTransform(contact.getTransform());
						//driver.setParent(contact.getParent());
						driver = contact;
						cstg = generatePlaces(circuit, stg, contact);

						drivers.put(driver, cstg);
						// attach driven wires to the place
						attachConnections(circuit, contact, cstg);
					}

					targetDrivers.put(contact.getReferencedContact(), driver);
				}
			}

			// generate implementation for each of the drivers
			for (VisualContact c : drivers.keySet()) {
				if (c instanceof VisualFunctionContact) {
					// function based driver
					Dnf set = null;
					Dnf reset = null;
					VisualFunctionContact contact = (VisualFunctionContact)c;
					SignalTransition.Type ttype = SignalTransition.Type.OUTPUT;


					if (contact.getFunction().getSetFunction()!=null) {

						set = DnfGenerator.generate(contact.getFunction().getSetFunction());
						if (contact.getFunction().getResetFunction() != null) {
							reset = DnfGenerator.generate(contact.getFunction().getResetFunction());
						} else {
							BooleanOperations.worker = new DumbBooleanWorker();
							reset = DnfGenerator.generate(BooleanOperations.worker.not(contact.getFunction().getSetFunction()));
						}

					}


					if (contact.getParent() instanceof VisualCircuitComponent) {


						if (((VisualCircuitComponent)contact.getParent()).getIsEnvironment()) {
							ttype = SignalTransition.Type.INPUT;
						} else if (contact.getIOType()==IOType.INPUT) {
							ttype = SignalTransition.Type.INPUT;
						}
					} else {

						if (contact.getIOType()==IOType.INPUT) {
							ttype = SignalTransition.Type.INPUT;
						}
					}
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

		AffineTransform transform = TransformHelper.getTransformToRoot(contact);
		Point2D center = new Point2D.Double(xScaling*(transform.getTranslateX()+contact.getX()), yScaling*(transform.getTranslateY()+contact.getY()));

		Point2D direction;
		Point2D pOffset;
		Point2D plusDirection;
		Point2D minusDirection;

//		int maxC = Math.max(set.getClauses().size(), reset.getClauses().size());

		VisualContact.Direction dir = contact.getDirection();

		if (contact.getIOType()==IOType.INPUT) {
			dir = VisualContact.Direction.flipDirection(dir);
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

		String signalName = getContactName(circuit, contact);

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
				set,
				add(add(center, direction), pOffset), plusDirection,
				signalName, ttype, SignalTransition.Direction.PLUS, p.p0, p.p1));

		nodes.addAll(buildTransitions(contact, stg, circuit, drivers, targetDrivers,
				reset,
				subtract(add(center, direction), pOffset),  minusDirection,
				signalName, ttype, SignalTransition.Direction.MINUS, p.p1, p.p0));

		stg.select(nodes);
		stg.groupSelection();
	}

	private static LinkedList<VisualNode> buildTransitions(VisualContact parentContact,
			VisualSTG stg, VisualCircuit circuit, Map<VisualContact, ContactSTG> drivers, Map<Contact, VisualContact> targetDrivers,
			Dnf function, Point2D baseOffset, Point2D transitionOffset,
			String signalName, SignalTransition.Type type, Direction transitionDirection,
			VisualPlace preset, VisualPlace postset) throws InvalidConnectionException {

		LinkedList<VisualNode> nodes = new LinkedList<VisualNode>();


		String path = HierarchicalNames.getParentReference(circuit.getMathModel().getNodeReference(parentContact.getReferencedComponent()));
		Container curContainer = (Container)createdContainers.get(path);
		while (curContainer==null) {
			path = HierarchicalNames.getParentReference(path);
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


		for(DnfClause clause : clauses)
		{
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

				if(source == null)
					throw new RuntimeException("No source for " + circuit.getMathModel().getName(targetContact) + " while generating " + signalName);

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
		String result = "";
		Node parent = contact.getParent();
		if (parent instanceof VisualFunctionComponent) {
			int output_cnt=0;
			VisualFunctionComponent vc = (VisualFunctionComponent)parent;
			for (Node n: vc.getChildren()) {
				if ((n instanceof VisualContact)&&
						((VisualContact)n).getIOType()!=IOType.INPUT) {
					output_cnt++;
				}
			}

			result = HierarchicalNames.getFlatName(
						circuit.getMathModel().getName(vc.getReferencedComponent())
					);

//			result = HierarchicalNames.getFlatName(
//					circuit.getMathModel().getNodeReference(vc.getReferencedComponent())
//					);

			if (contact.getIOType() == IOType.INPUT || output_cnt > 1) {
				result += "_" + circuit.getMathModel().getName(contact.getReferencedContact());
			}
		} else {
			result = circuit.getMathModel().getName(contact.getReferencedContact());
		}
		return result;
	}
}
