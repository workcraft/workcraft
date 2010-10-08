package org.workcraft.plugins.circuit.stg;

import static org.workcraft.util.Geometry.add;
import static org.workcraft.util.Geometry.multiply;
import static org.workcraft.util.Geometry.rotate90CCW;
import static org.workcraft.util.Geometry.subtract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Geometry;
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

	static void setPosition(Movable node, Point2D point) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}

	public static VisualSTG generate(VisualCircuit circuit) {
		try {
			VisualSTG stg = new VisualSTG(new STG());
			Set<VisualFunctionContact> sources = new HashSet<VisualFunctionContact>();
			Map<FunctionContact, ContactSTG> places = new HashMap<FunctionContact, ContactSTG>();

			for(VisualFunctionContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionContact.class)) {
//				System.out.println();
				if((contact.getIOType() == IOType.OUTPUT)
				 == (contact.getParent() instanceof VisualFunctionComponent))
				{
					places.put(contact.getFunction(), generatePlaces(circuit, stg, contact));
					sources.add(contact);
				}
			}

			for(VisualFunctionContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionContact.class)) {
				if(!places.containsKey(contact.getFunction()))
				{
					Set<Node> neighbours = new HashSet<Node>(circuit.getPreset(contact));
					neighbours.addAll(circuit.getPostset(contact));
					if(neighbours.size() == 0) {
						places.put(contact.getFunction(), generatePlaces(circuit, stg, contact));
						sources.add(contact);
					}
					else
					{
						if(neighbours.size() != 1)
							throw new RuntimeException("Found more than one driver!");
						VisualFunctionContact neighbour = (VisualFunctionContact)neighbours.iterator().next();
						places.put(contact.getFunction(), places.get(neighbour.getFunction()));
					}
				}
			}

			for(VisualFunctionContact contact : sources)
			{
				AffineTransform transform = TransformHelper.getTransformToAncestor(contact, circuit.getRoot());
				Point2D center = new Point2D.Double(4*(transform.getTranslateX()+contact.getX()), 4*(transform.getTranslateY()+contact.getY()));

				Point2D direction = new Point2D.Double(1, 0);
				int rotations;
				switch(contact.getDirection())
				{
				case WEST: rotations=0;break;
				case NORTH: rotations=1;break;
				case EAST: rotations=2;break;
				case SOUTH: rotations=3;break;
				default: throw new RuntimeException();
				}

				for(int i=0;i<rotations;i++)
					direction = rotate90CCW(direction);

				String signalName = getContactName(circuit, contact);

				ContactSTG p = places.get(contact.getFunction());
				if(p == null)
					throw new RuntimeException("Places for contact " + signalName + " cannot be found.");

				Point2D plusDirection = rotate90CCW(direction);
				Point2D minusDirection = Geometry.multiply(plusDirection, -1);

				setPosition(p.p1, add(center, plusDirection));
				setPosition(p.p0, subtract(center, plusDirection));

				Dnf set = DnfGenerator.generate(contact.getFunction().getSetFunction());
				Dnf reset = DnfGenerator.generate(contact.getFunction().getResetFunction());

				buildTransitions(places, stg, circuit, set, center, direction, plusDirection, signalName, SignalTransition.Direction.PLUS, p.p0, p.p1);
				buildTransitions(places, stg, circuit, reset, center, direction,  minusDirection, signalName, SignalTransition.Direction.MINUS, p.p1, p.p0);
			}

			return stg;
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}

	}

	private static ContactSTG generatePlaces(VisualCircuit circuit,
			VisualSTG stg, VisualFunctionContact contact) {
		String contactName = getContactName(circuit, contact);
		VisualPlace zeroPlace = stg.createPlace(contactName+"_0");
		zeroPlace.setTokens(1);
		ContactSTG contactSTG = new ContactSTG(zeroPlace, stg.createPlace(contactName+"_1"));
		return contactSTG;
	}

	private static void buildTransitions(
			Map<FunctionContact, ContactSTG> places, VisualSTG stg, VisualCircuit circuit,
			Dnf function,
			Point2D center, Point2D direction, Point2D signDirection,
			String signalName, Direction transitionDirection,
			VisualPlace preset, VisualPlace postset) throws InvalidConnectionException {
		int i=0;
		for(DnfClause clause : function.getClauses())
		{
			VisualSignalTransition transition = stg.createSignalTransition(signalName, SignalTransition.Type.OUTPUT, transitionDirection);
			setPosition(transition, subtract(add(center, multiply(signDirection, i+1)), direction));
			stg.connect(transition, postset);
			stg.connect(preset, transition);

			transition.setLabel(FormulaToString.toString(clause));

			for(Literal literal : clause.getLiterals()) {
				{
					FunctionContact inputContact = (FunctionContact)literal.getVariable();

					ContactSTG source = places.get(inputContact);

					if(source == null)
						throw new RuntimeException("No source for " + inputContact.getName() + " while generating " + signalName);

					VisualPlace p = literal.getNegation() ? source.p0 : source.p1;

					stg.connect(p, transition);
					stg.connect(transition, p);
				}
			}
			i++;
		}
	}

	private static String getContactName(VisualCircuit circuit, VisualFunctionContact contact) {
		String prefix = "";
		Node parent = contact.getParent();
		if(parent instanceof VisualFunctionComponent)
			prefix = ((VisualFunctionComponent)parent).getName()+"_";
		return prefix+contact.getName();
	}
}
