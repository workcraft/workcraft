package org.workcraft.plugins.xmas.stg;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.workcraft.plugins.xmas.components.VisualForkComponent;
import org.workcraft.plugins.xmas.components.VisualFunctionComponent;
import org.workcraft.plugins.xmas.components.VisualJoinComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
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
	private Map<VisualForkComponent, ForkStg> forkMap = new HashMap<>();
	private Map<VisualJoinComponent, JoinStg> joinMap = new HashMap<>();
	private final VisualXmas xmas;
	private final VisualSTG stg;

	public StgGenerator(VisualXmas xmas) {
		this.xmas = xmas;
		this.stg = new VisualSTG(new STG());
		convert();
	}

	private void convert() {
		try {
			for(VisualSourceComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
				SourceStg sourceStg = generateSourceStg(component);
				sourceMap.put(component, sourceStg);
			}
			for(VisualSinkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
				SinkStg sinkStg = generateSinkStg(component);
				sinkMap.put(component, sinkStg);
			}
			for(VisualFunctionComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
				FunctionStg functionStg = generateFunctionStg(component);
				functionMap.put(component, functionStg);
			}
			for(VisualForkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualForkComponent.class)) {
				ForkStg forkStg = generateForkStg(component);
				forkMap.put(component, forkStg);
			}
			for(VisualJoinComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualJoinComponent.class)) {
				JoinStg joinStg = generateJoinStg(component);
				joinMap.put(component, joinStg);
			}

			for(VisualSourceComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
				connectSourceStg(component);
			}
			for(VisualSinkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
				connectSinkStg(component);
			}
			for(VisualFunctionComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
				connectFunctionStg(component);
			}
			for(VisualForkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualForkComponent.class)) {
				connectForkStg(component);
			}
			for(VisualJoinComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualJoinComponent.class)) {
				connectJoinStg(component);
			}
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
		stg.selectNone();
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

	private ContactStg generateInputContactStg(String signalName, double x, double y) throws InvalidConnectionException {
		return generateInputContactStg(signalName, x, y, 1, 1);
	}

	private ContactStg generateInputContactStg(String signalName, double x, double y, int cntF, int cntR) throws InvalidConnectionException {
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		VisualPlace p0 = stg.createPlace(signalName + name0, null);
		p0.getReferencedPlace().setTokens(1);
		setPosition(p0, x - 4.0, y + 1.0);

		VisualPlace p1 = stg.createPlace(signalName + name1, null);
		p1.getReferencedPlace().setTokens(0);
		setPosition(p1, x - 4.0, y - 1.0);

		ArrayList<VisualSignalTransition> tFs = new ArrayList<>(cntF);
		for (int i = 0; i < cntF; i++) {
			VisualSignalTransition tF = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
			createConsumingArc(p1, tF);
			createProducingArc(tF, p0);
			setPosition(tF, x + 0.0, y + 1.0 + i);
			tFs.add(tF);
		}

		ArrayList<VisualSignalTransition> tRs = new ArrayList<>(cntR);
		for (int i = 0; i < cntR; i++) {
			VisualSignalTransition tR = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
			createConsumingArc(p0, tR);
			createProducingArc(tR, p1);
			setPosition(tR, x + 0.0, y - 1.0 - i);
			tRs.add(tR);
		}

		return new ContactStg(p0, p1, tFs, tRs);
	}

	private ContactStg generateOutputContactStg(String signalName, double x, double y) throws InvalidConnectionException {
		return generateOutputContactStg(signalName, x, y, 1, 1);
	}

	private ContactStg generateOutputContactStg(String signalName, double x, double y, int cntF, int cntR) throws InvalidConnectionException {
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		VisualPlace p0 = stg.createPlace(signalName + name0, null);
		p0.getReferencedPlace().setTokens(1);
		setPosition(p0, x + 4.0, y + 1.0);

		VisualPlace p1 = stg.createPlace(signalName + name1, null);
		p1.getReferencedPlace().setTokens(0);
		setPosition(p1, x + 4.0, y - 1.0);

		ArrayList<VisualSignalTransition> tFs = new ArrayList<>(cntF);
		for (int i = 0; i < cntF; i++) {
			VisualSignalTransition tF = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
			createConsumingArc(p1, tF);
			createProducingArc(tF, p0);
			setPosition(tF, x - 0.0, y + 1.0 + i);
			tFs.add(tF);
		}

		ArrayList<VisualSignalTransition> tRs = new ArrayList<>(cntR);
		for (int i = 0; i < cntR; i++) {
			VisualSignalTransition tR = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
			createConsumingArc(p0, tR);
			createProducingArc(tR, p1);
			setPosition(tR, x - 0.0, y - 1.0 - i);
			tRs.add(tR);
		}

		return new ContactStg(p0, p1, tFs, tRs);
	}

	public ContactStg getContactStg(VisualXmasContact contact) {
		return contactMap.get(contact);
	}


	private SourceStg generateSourceStg(VisualSourceComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		ContactStg oStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oStg = generateOutputContactStg(name + nameOirdy, x, y);
				contactMap.put(contact, oStg);
			}
		}
		stg.select(oStg.getAllNodes());
		stg.groupSelection();
		return new SourceStg(oStg);
	}

	private void connectSourceStg(VisualSourceComponent component) throws InvalidConnectionException {
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		SourceStg sourceStg = getSourceStg(component);
		if ((sourceStg != null) && (oContact != null)) {
			ContactStg oStg = getContactStg(oContact);
			createReadArc(oStg.rdy1, sourceStg.o.rdyFs.get(0));
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
		ContactStg iStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateOutputContactStg(name + nameItrdy, x, y);
				contactMap.put(contact, iStg);
			}
		}
		stg.select(iStg.getAllNodes());
		stg.groupSelection();
		return new SinkStg(iStg);
	}

	private void connectSinkStg(VisualSinkComponent component) throws InvalidConnectionException {
		VisualXmasContact iContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		SinkStg sinkStg = getSinkStg(component);
		if (iContact != null) {
			ContactStg iStg = getContactStg(iContact);
			if ((sinkStg != null) && (iStg !=null)) {
				createReadArc(iStg.rdy1, sinkStg.i.rdyFs.get(0));
			}
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
		ContactStg iStg = null;
		ContactStg oStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateInputContactStg(name + nameItrdy, x, y + 3.0);
				contactMap.put(contact, iStg);
			} else {
				oStg = generateOutputContactStg(name + nameOirdy, x, y - 3.0);
				contactMap.put(contact, oStg);
			}
		}
		stg.selectNone();
		stg.addToSelection(iStg.getAllNodes());
		stg.addToSelection(oStg.getAllNodes());
		stg.groupSelection();
		return new FunctionStg(iStg, oStg);
	}

	private void connectFunctionStg(VisualFunctionComponent component) throws InvalidConnectionException {
		VisualXmasContact iContact = null;
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else {
				oContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		FunctionStg functionStg = getFunctionStg(component);
		if (iContact != null) {
			ContactStg iStg = getContactStg(iContact);
			if ((functionStg != null) && (iStg != null)) {
				createReadArc(iStg.rdy0, functionStg.o.rdyFs.get(0));
				createReadArc(iStg.rdy1, functionStg.o.rdyRs.get(0));
			}
		}
		if (oContact != null) {
			ContactStg oStg = getContactStg(oContact);
			if ((functionStg != null) && (oStg != null)) {
				createReadArc(oStg.rdy0, functionStg.i.rdyFs.get(0));
				createReadArc(oStg.rdy1, functionStg.i.rdyRs.get(0));
			}
		}
	}

	public FunctionStg getFunctionStg(VisualFunctionComponent component) {
		return functionMap.get(component);
	}


	private ForkStg generateForkStg(VisualForkComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		ContactStg iStg = null;
		ContactStg aStg = null;
		ContactStg bStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateInputContactStg(name + nameItrdy, x, y, 2, 1);
				contactMap.put(contact, iStg);
			} else if (aStg == null) {
				aStg = generateOutputContactStg(name + nameAirdy, x, y - 5.0, 2, 1);
				contactMap.put(contact, aStg);
			} else {
				bStg = generateOutputContactStg(name + nameBirdy, x, y + 5.0, 2, 1);
				contactMap.put(contact, bStg);
			}
		}
		stg.selectNone();
		stg.addToSelection(iStg.getAllNodes());
		stg.addToSelection(aStg.getAllNodes());
		stg.addToSelection(bStg.getAllNodes());
		stg.groupSelection();
		return new ForkStg(iStg, aStg, bStg);
	}

	private void connectForkStg(VisualForkComponent component) throws InvalidConnectionException {
		VisualXmasContact iContact = null;
		VisualXmasContact aContact = null;
		VisualXmasContact bContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else if (aContact == null) {
				aContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else {
				bContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		ForkStg forkStg = getForkStg(component);
		if (iContact != null) {
			ContactStg iStg = getContactStg(iContact);
			if ((forkStg != null) && (iStg != null)) {
				createReadArc(iStg.rdy0, forkStg.a.rdyFs.get(0));
				createReadArc(iStg.rdy0, forkStg.b.rdyFs.get(0));
				createReadArc(iStg.rdy1, forkStg.a.rdyRs.get(0));
				createReadArc(iStg.rdy1, forkStg.b.rdyRs.get(0));
			}
		}
		if (aContact != null) {
			ContactStg aStg = getContactStg(aContact);
			if ((forkStg != null) && (aStg != null)) {
				createReadArc(aStg.rdy0, forkStg.i.rdyFs.get(0));
				createReadArc(aStg.rdy0, forkStg.b.rdyFs.get(1));
				createReadArc(aStg.rdy1, forkStg.i.rdyRs.get(0));
				createReadArc(aStg.rdy1, forkStg.b.rdyRs.get(0));
			}
		}
		if (bContact != null) {
			ContactStg bStg = getContactStg(bContact);
			if ((forkStg != null) && (bStg != null)) {
				createReadArc(bStg.rdy0, forkStg.i.rdyFs.get(1));
				createReadArc(bStg.rdy0, forkStg.a.rdyFs.get(1));
				createReadArc(bStg.rdy1, forkStg.i.rdyRs.get(0));
				createReadArc(bStg.rdy1, forkStg.a.rdyRs.get(0));
			}
		}
	}

	public ForkStg getForkStg(VisualForkComponent component) {
		return forkMap.get(component);
	}


	private JoinStg generateJoinStg(VisualJoinComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		ContactStg aStg = null;
		ContactStg bStg = null;
		ContactStg oStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oStg = generateOutputContactStg(name + nameOirdy, x, y, 2, 1);
				contactMap.put(contact, oStg);
			} else if (aStg == null) {
				aStg = generateInputContactStg(name + nameAtrdy, x, y - 5.0, 2, 1);
				contactMap.put(contact, aStg);
			} else {
				bStg = generateInputContactStg(name + nameBtrdy, x, y + 5.0, 2, 1);
				contactMap.put(contact, bStg);
			}
		}
		stg.selectNone();
		stg.addToSelection(aStg.getAllNodes());
		stg.addToSelection(bStg.getAllNodes());
		stg.addToSelection(oStg.getAllNodes());
		stg.groupSelection();
		return new JoinStg(aStg, bStg, oStg);
	}

	private void connectJoinStg(VisualJoinComponent component) throws InvalidConnectionException {
		VisualXmasContact aContact = null;
		VisualXmasContact bContact = null;
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else if (aContact == null) {
				aContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else {
				bContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		JoinStg joinStg = getJoinStg(component);
		if (aContact != null) {
			ContactStg aStg = getContactStg(aContact);
			if ((joinStg != null) && (aStg != null)) {
				createReadArc(aStg.rdy0, joinStg.b.rdyFs.get(1));
				createReadArc(aStg.rdy0, joinStg.o.rdyFs.get(0));
				createReadArc(aStg.rdy1, joinStg.b.rdyRs.get(0));
				createReadArc(aStg.rdy1, joinStg.o.rdyRs.get(0));
			}
		}
		if (bContact != null) {
			ContactStg bStg = getContactStg(bContact);
			if ((joinStg != null) && (bStg != null)) {
				createReadArc(bStg.rdy0, joinStg.a.rdyFs.get(1));
				createReadArc(bStg.rdy0, joinStg.o.rdyFs.get(1));
				createReadArc(bStg.rdy1, joinStg.a.rdyRs.get(0));
				createReadArc(bStg.rdy1, joinStg.o.rdyRs.get(0));
			}
		}
		if (oContact != null) {
			ContactStg oStg = getContactStg(oContact);
			if ((joinStg != null) && (oStg != null)) {
				createReadArc(oStg.rdy0, joinStg.a.rdyFs.get(0));
				createReadArc(oStg.rdy0, joinStg.b.rdyFs.get(0));
				createReadArc(oStg.rdy1, joinStg.a.rdyRs.get(0));
				createReadArc(oStg.rdy1, joinStg.b.rdyRs.get(0));
			}
		}
	}

	public JoinStg getJoinStg(VisualJoinComponent component) {
		return joinMap.get(component);
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
