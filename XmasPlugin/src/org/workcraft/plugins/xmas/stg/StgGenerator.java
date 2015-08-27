package org.workcraft.plugins.xmas.stg;

import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.XmasUtils;
import org.workcraft.plugins.xmas.components.VisualFunctionComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualXmasComponent;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.util.Hierarchy;

public class StgGenerator {
	public static final String nameOirdy	= "_Oirdy";
	public static final String nameAirdy	= "_Airdy";
	public static final String nameBirdy	= "_Birdy";
	public static final String nameItrdy	= "_Itrdy";
	public static final String nameAtrdy	= "_Atrdy";
	public static final String nameBtrdy	= "_Btrdy";
	public static final String name0	 	= "_0";
	public static final String name1 		= "_1";
	private static final double xScaling = 6;
	private static final double yScaling = 6;

	private Map<VisualXmasContact, ContactStg> contactMap = new HashMap<>();
	private Map<VisualSourceComponent, SourceStg> sourceMap = new HashMap<>();
	private Map<VisualSinkComponent, SinkStg> sinkMap = new HashMap<>();
	private Map<VisualFunctionComponent, FunctionStg> functionMap = new HashMap<>();
	private final VisualXmas xmas;
	private final VisualSTG stg;

	public StgGenerator(VisualXmas xmas) {
		this.xmas = xmas;
		this.stg = new VisualSTG(new STG());
		convert();
	}

	private void convert() {
		try {
			for(VisualSourceComponent sourceComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
				SourceStg sourceStg = generateSourceStg(sourceComponent);
				sourceMap.put(sourceComponent, sourceStg);
			}
			for(VisualSinkComponent sinkComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
				SinkStg sinkStg = generateSinkStg(sinkComponent);
				sinkMap.put(sinkComponent, sinkStg);
			}
			for(VisualFunctionComponent functionComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
				FunctionStg functionStg = generateFunctionStg(functionComponent);
				functionMap.put(functionComponent, functionStg);
			}

			for(VisualSourceComponent sourceComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
				connectSourceStg(sourceComponent);
			}
			for(VisualSinkComponent sinkComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
				connectSinkStg(sinkComponent);
			}
			for(VisualFunctionComponent functionComponent : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
				connectFunctionStg(functionComponent);
			}
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	public VisualSTG getStg() {
		return stg;
	}

	static void setPosition(Movable node, double x, double y) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
	}

	private void createConsumingArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
		if (p != null && t != null) {
			stg.connect(p, t);
		}
	}

	private void createProducingArc(VisualSignalTransition t, VisualPlace p) throws InvalidConnectionException {
		if (p != null && t != null) {
			stg.connect(t, p);
		}
	}

	private void createReadArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
		if (p != null && t != null) {
			stg.connect(p, t);
			stg.connect(t, p);
		}
	}

	public ContactStg getContactStg(VisualXmasContact contact) {
		return contactMap.get(contact);
	}


	private SourceStg generateSourceStg(VisualSourceComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		Container curContainer = null;
		ContactStg oStg = null;
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				VisualPlace o0 = stg.createPlace(name + nameOirdy + name0, curContainer);
				o0.getReferencedPlace().setTokens(1);
				o0.setForegroundColor(component.getForegroundColor());
				o0.setFillColor(component.getFillColor());
				setPosition(o0, x + 4.0, y + 1.0);
				nodes.add(o0);

				VisualPlace o1 = stg.createPlace(name + nameOirdy + name1, curContainer);
				o1.getReferencedPlace().setTokens(0);
				o1.setForegroundColor(component.getForegroundColor());
				o1.setFillColor(component.getFillColor());
				setPosition(o1, x + 4.0, y - 1.0);
				nodes.add(o1);

				VisualSignalTransition oF = stg.createSignalTransition(name + nameOirdy, type, SignalTransition.Direction.MINUS, curContainer);
				createConsumingArc(o1, oF);
				createProducingArc(oF, o0);
				setPosition(oF, x - 0.0, y + 1.0);
				nodes.add(oF);

				VisualSignalTransition oR = stg.createSignalTransition(name + nameOirdy, type, SignalTransition.Direction.PLUS, curContainer);
				createConsumingArc(o0, oR);
				createProducingArc(oR, o1);
				setPosition(oR, x - 0.0, y - 1.0);
				nodes.add(oR);

				oStg = new ContactStg(o0, o1, oF, oR);
				oContact = contact;
				contactMap.put(oContact, oStg);
			}
		}

		stg.select(nodes);
		stg.groupSelection();

		return new SourceStg(oStg);
	}

	private void connectSourceStg(VisualSourceComponent component) throws InvalidConnectionException {
		VisualPlace i1 = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				VisualXmasComponent succComponent =  XmasUtils.getConnectedComponent(xmas, contact);
				if (succComponent instanceof VisualSinkComponent) {
					SinkStg stg = getSinkStg((VisualSinkComponent)succComponent);
					i1 = stg.i.rdy1;
				} else if (succComponent instanceof VisualFunctionComponent) {
					FunctionStg stg = getFunctionStg((VisualFunctionComponent)succComponent);
					i1 = stg.i.rdy1;
				}
			}
		}
		if (i1 != null) {
			SourceStg stg = getSourceStg(component);
			createReadArc(i1, stg.o.rdyF);
		}
	}

	public SourceStg getSourceStg(VisualSourceComponent component) {
		return sourceMap.get(component);
	}


	private SinkStg generateSinkStg(VisualSinkComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		Container curContainer = null;
		ContactStg iStg = null;
		VisualXmasContact iContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				VisualPlace i0 = stg.createPlace(name + nameItrdy + name0, curContainer);
				i0.getReferencedPlace().setTokens(1);
				i0.setForegroundColor(component.getForegroundColor());
				i0.setFillColor(component.getFillColor());
				setPosition(i0, x - 4.0, y + 1.0);
				nodes.add(i0);

				VisualPlace i1 = stg.createPlace(name + nameItrdy + name1, curContainer);
				i1.getReferencedPlace().setTokens(0);
				i1.setForegroundColor(component.getForegroundColor());
				i1.setFillColor(component.getFillColor());
				setPosition(i1, x - 4.0, y - 1.0);
				nodes.add(i1);

				VisualSignalTransition iF = stg.createSignalTransition(name + nameItrdy, type, SignalTransition.Direction.MINUS, curContainer);
				createConsumingArc(i1, iF);
				createProducingArc(iF, i0);
				setPosition(iF, x + 0.0, y + 1.0);
				nodes.add(iF);

				VisualSignalTransition iR = stg.createSignalTransition(name + nameItrdy, type, SignalTransition.Direction.PLUS, curContainer);
				createConsumingArc(i0, iR);
				createProducingArc(iR, i1);
				setPosition(iR, x + 0.0, y - 1.0);
				nodes.add(iR);

				iStg = new ContactStg(i0, i1, iF, iR);
				iContact = contact;
				contactMap.put(iContact, iStg);
			}
		}

		stg.select(nodes);
		stg.groupSelection();
		return new SinkStg(iStg);
	}

	private void connectSinkStg(VisualSinkComponent component) throws InvalidConnectionException {
		VisualPlace o1 = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				VisualXmasComponent prevComponent =  XmasUtils.getConnectedComponent(xmas, contact);
				if (prevComponent instanceof VisualSourceComponent) {
					SourceStg stg = getSourceStg((VisualSourceComponent)prevComponent);
					o1 = stg.o.rdy1;
				} else if (prevComponent instanceof VisualFunctionComponent) {
					FunctionStg stg = getFunctionStg((VisualFunctionComponent)prevComponent);
					o1 = stg.o.rdy1;
				}
			}
		}
		if (o1 != null) {
			SinkStg stg = getSinkStg(component);
			createReadArc(o1, stg.i.rdyF);
		}
	}

	public SinkStg getSinkStg(VisualSinkComponent component) {
		return sinkMap.get(component);
	}


	private FunctionStg generateFunctionStg(VisualFunctionComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		Container curContainer = null;
		ContactStg iStg = null;
		VisualXmasContact iContact = null;
		ContactStg oStg = null;
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				VisualPlace i0 = stg.createPlace(name + nameItrdy + name0, curContainer);
				i0.getReferencedPlace().setTokens(1);
				i0.setForegroundColor(component.getForegroundColor());
				i0.setFillColor(component.getFillColor());
				setPosition(i0, x - 4.0, y + 4.0);
				nodes.add(i0);

				VisualPlace i1 = stg.createPlace(name + nameItrdy + name1, curContainer);
				i1.getReferencedPlace().setTokens(0);
				i1.setForegroundColor(component.getForegroundColor());
				i1.setFillColor(component.getFillColor());
				setPosition(i1, x - 4.0, y + 2.0);
				nodes.add(i1);

				VisualSignalTransition iF = stg.createSignalTransition(name + nameItrdy, type, SignalTransition.Direction.MINUS, curContainer);
				createConsumingArc(i1, iF);
				createProducingArc(iF, i0);
				setPosition(iF, x + 0.0, y + 4.0);
				nodes.add(iF);

				VisualSignalTransition iR = stg.createSignalTransition(name + nameItrdy, type, SignalTransition.Direction.PLUS, curContainer);
				createConsumingArc(i0, iR);
				createProducingArc(iR, i1);
				setPosition(iR, x + 0.0, y + 2.0);
				nodes.add(iR);

				iStg = new ContactStg(i0, i1, iF, iR);
				iContact = contact;
				contactMap.put(iContact, iStg);
			} else {
				VisualPlace o0 = stg.createPlace(name + nameOirdy + name0, curContainer);
				o0.getReferencedPlace().setTokens(1);
				o0.setForegroundColor(component.getForegroundColor());
				o0.setFillColor(component.getFillColor());
				setPosition(o0, x + 4.0, y - 2.0);
				nodes.add(o0);

				VisualPlace o1 = stg.createPlace(name + nameOirdy + name1, curContainer);
				o1.getReferencedPlace().setTokens(0);
				o1.setForegroundColor(component.getForegroundColor());
				o1.setFillColor(component.getFillColor());
				setPosition(o1, x + 4.0, y - 4.0);
				nodes.add(o1);

				VisualSignalTransition oF = stg.createSignalTransition(name + nameOirdy, type, SignalTransition.Direction.MINUS, curContainer);
				createConsumingArc(o1, oF);
				createProducingArc(oF, o0);
				setPosition(oF, x - 0.0, y - 2.0);
				nodes.add(oF);

				VisualSignalTransition oR = stg.createSignalTransition(name + nameOirdy, type, SignalTransition.Direction.PLUS, curContainer);
				createConsumingArc(o0, oR);
				createProducingArc(oR, o1);
				setPosition(oR, x - 0.0, y - 4.0);
				nodes.add(oR);

				oStg = new ContactStg(o0, o1, oF, oR);
				oContact = contact;
				contactMap.put(oContact, oStg);
			}
		}
		stg.select(nodes);
		stg.groupSelection();
		return new FunctionStg(iStg, oStg);
	}

	private void connectFunctionStg(VisualFunctionComponent component) throws InvalidConnectionException {
		VisualPlace i0 = null;
		VisualPlace i1 = null;
		VisualPlace o0 = null;
		VisualPlace o1 = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				VisualXmasComponent prevComponent =  XmasUtils.getConnectedComponent(xmas, contact);
				if (prevComponent instanceof VisualSourceComponent) {
					SourceStg stg = getSourceStg((VisualSourceComponent)prevComponent);
					o0 = stg.o.rdy0;
					o1 = stg.o.rdy1;
				} else if (prevComponent instanceof VisualFunctionComponent) {
					FunctionStg stg = getFunctionStg((VisualFunctionComponent)prevComponent);
					o0 = stg.o.rdy0;
					o1 = stg.o.rdy1;
				}
			} else {
				VisualXmasComponent succComponent =  XmasUtils.getConnectedComponent(xmas, contact);
				if (succComponent instanceof VisualSinkComponent) {
					SinkStg stg = getSinkStg((VisualSinkComponent)succComponent);
					i0 = stg.i.rdy0;
					i1 = stg.i.rdy1;
				} else if (succComponent instanceof VisualFunctionComponent) {
					FunctionStg stg = getFunctionStg((VisualFunctionComponent)succComponent);
					i0 = stg.i.rdy0;
					i1 = stg.i.rdy1;
				}
			}
		}
		if ( (i0 != null) && (i1 != null) && (o0 != null) && (o1 != null) ) {
			FunctionStg stg = getFunctionStg(component);
			createReadArc(o0, stg.o.rdyF);
			createReadArc(o1, stg.o.rdyR);
			createReadArc(i0, stg.i.rdyF);
			createReadArc(i1, stg.i.rdyR);
		}
	}

	public FunctionStg getFunctionStg(VisualFunctionComponent component) {
		return functionMap.get(component);
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		NodeStg nodeStg = null;
		if (highLevelNode instanceof VisualSourceComponent) {
			nodeStg = getSourceStg((VisualSourceComponent)highLevelNode);
		} else if (highLevelNode instanceof VisualSinkComponent) {
			nodeStg = getSinkStg((VisualSinkComponent)highLevelNode);
		} else if (highLevelNode instanceof VisualFunctionComponent) {
			nodeStg = getFunctionStg((VisualFunctionComponent)highLevelNode);
		}
		return ((nodeStg != null) && nodeStg.contains(node));
	}

}
