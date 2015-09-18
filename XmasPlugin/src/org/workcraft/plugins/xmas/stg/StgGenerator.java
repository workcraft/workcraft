package org.workcraft.plugins.xmas.stg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.XmasUtils;
import org.workcraft.plugins.xmas.components.VisualForkComponent;
import org.workcraft.plugins.xmas.components.VisualFunctionComponent;
import org.workcraft.plugins.xmas.components.VisualJoinComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualXmasComponent;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.util.Hierarchy;

public class StgGenerator {
	private static final String _O_IRDY	= "_Oirdy";
	private static final String _O_IDN  = "_Oidn";
	private static final String _A_IRDY	= "_Airdy";
	private static final String _A_IDN  = "_Aidn";
	private static final String _B_IRDY	= "_Birdy";
	private static final String _B_IDN  = "_Bidn";

	private static final String _I_TRDY	= "_Itrdy";
	private static final String _I_TDN  = "_Itdn";
	private static final String _A_TRDY	= "_Atrdy";
	private static final String _A_TDN  = "_Atdn";
	private static final String _B_TRDY	= "_Btrdy";
	private static final String _B_TDN  = "_Btdn";

	private static final String _ORACLE	= "_oracle";
	private static final String _MEM 	= "_mem";
	private static final String _HEAD	= "_hd";
	private static final String _TAIL	= "_tl";
	private static final String _DONE  	= "_dn";

	private static final String name0	 	= "_0";
	private static final String name1 		= "_1";
	private static final double xScaling = 6;
	private static final double yScaling = 6;

	private SignalStg clockStg = null;
	private Map<VisualXmasContact, SignalStg> contactMap = new HashMap<>();
	private Map<VisualSourceComponent, SourceStg> sourceMap = new HashMap<>();
	private Map<VisualSinkComponent, SinkStg> sinkMap = new HashMap<>();
	private Map<VisualFunctionComponent, FunctionStg> functionMap = new HashMap<>();
	private Map<VisualForkComponent, ForkStg> forkMap = new HashMap<>();
	private Map<VisualJoinComponent, JoinStg> joinMap = new HashMap<>();
	private Map<VisualQueueComponent, QueueStg> queueMap = new HashMap<>();
	private final VisualXmas xmas;
	private final VisualSTG stg;

	public StgGenerator(VisualXmas xmas) {
		this.xmas = xmas;
		this.stg = new VisualSTG(new STG());
		convert();
	}

	private void convert() {
		try {
			clockStg = generateClockStg();
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
			for(VisualQueueComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualQueueComponent.class)) {
				QueueStg queueStg = generateQueueStg(component);
				queueMap.put(component, queueStg);
			}

			connectClockStg();
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
			for(VisualQueueComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualQueueComponent.class)) {
				connectQueueStg(component);
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
			stg.connectUndirected(p, t);
		}
	}

	private void createReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts) throws InvalidConnectionException {
		if (ts != null) {
			for (VisualSignalTransition t: ts) {
				stg.connectUndirected(p, t);
			}
		}
	}

	private void createReadArcsBetweenSignals(SignalStg fromSignal, SignalStg toSignal) throws InvalidConnectionException {
		if ((fromSignal != null) && (toSignal != null)) {
			createReadArcs(fromSignal.one, toSignal.riseList);
			createReadArcs(fromSignal.zero, toSignal.fallList);
		}
	}


	private void createReplicaReadArc(VisualPlace p, VisualSignalTransition t, Point2D replicaPosition) throws InvalidConnectionException {
		if (p != null && t != null) {
			Container container = Hierarchy.getNearestContainer(t);
			VisualReplicaPlace replicaPlace = stg.createVisualReplica(p, container, VisualReplicaPlace.class);
			if (replicaPosition != null) {
				replicaPlace.setRootSpacePosition(replicaPosition);
			}
			stg.connectUndirected(replicaPlace, t);
		}
	}

	private SignalStg generateInputContactStg(String signalName, double x, double y) throws InvalidConnectionException {
		return generateInputContactStg(signalName, x, y, false, Type.INTERNAL, 1, 1);
	}

	private SignalStg generateInputContactStg(String signalName, double x, double y, boolean initToOne,
			SignalTransition.Type type, int fallCount, int riseCount) throws InvalidConnectionException {

		VisualPlace zero = stg.createPlace(signalName + name0, null);
		zero.setNamePositioning(Positioning.BOTTOM);
		zero.setLabelPositioning(Positioning.TOP);
		setPosition(zero, x - 4.0, y + 1.0);

		VisualPlace one = stg.createPlace(signalName + name1, null);
		one.setNamePositioning(Positioning.TOP);
		one.setLabelPositioning(Positioning.BOTTOM);
		setPosition(one, x - 4.0, y - 1.0);

		if (initToOne) {
			one.getReferencedPlace().setTokens(1);
			zero.getReferencedPlace().setTokens(0);
		} else {
			one.getReferencedPlace().setTokens(0);
			zero.getReferencedPlace().setTokens(1);
		}

		ArrayList<VisualSignalTransition> fallList = new ArrayList<>(fallCount);
		for (int i = 0; i < fallCount; i++) {
			VisualSignalTransition fall = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
			createConsumingArc(one, fall);
			createProducingArc(fall, zero);
			setPosition(fall, x + 0.0, y + 1.0 + i);
			fallList.add(fall);
		}

		ArrayList<VisualSignalTransition> riseList = new ArrayList<>(riseCount);
		for (int i = 0; i < riseCount; i++) {
			VisualSignalTransition rise = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
			createConsumingArc(zero, rise);
			createProducingArc(rise, one);
			setPosition(rise, x + 0.0, y - 1.0 - i);
			riseList.add(rise);
		}

		return new SignalStg(zero, one, fallList, riseList);
	}

	private SignalStg generateOutputContactStg(String signalName, double x, double y) throws InvalidConnectionException {
		return generateOutputContactStg(signalName, x, y, false, Type.INTERNAL, 1, 1);
	}

	private SignalStg generateOutputContactStg(String signalName, double x, double y, boolean initToOne,
			SignalTransition.Type type, int fallCount, int riseCount) throws InvalidConnectionException {

		VisualPlace zero = stg.createPlace(signalName + name0, null);
		zero.setNamePositioning(Positioning.BOTTOM);
		zero.setLabelPositioning(Positioning.TOP);
		setPosition(zero, x + 4.0, y + 1.0);

		VisualPlace one = stg.createPlace(signalName + name1, null);
		one.setNamePositioning(Positioning.TOP);
		one.setLabelPositioning(Positioning.BOTTOM);
		setPosition(one, x + 4.0, y - 1.0);

		if (initToOne) {
			one.getReferencedPlace().setTokens(1);
			zero.getReferencedPlace().setTokens(0);
		} else {
			one.getReferencedPlace().setTokens(0);
			zero.getReferencedPlace().setTokens(1);
		}

		ArrayList<VisualSignalTransition> fallList = new ArrayList<>(fallCount);
		for (int i = 0; i < fallCount; i++) {
			VisualSignalTransition fall = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
			createConsumingArc(one, fall);
			createProducingArc(fall, zero);
			setPosition(fall, x - 0.0, y + 1.0 + i);
			fallList.add(fall);
		}

		ArrayList<VisualSignalTransition> riseList = new ArrayList<>(riseCount);
		for (int i = 0; i < riseCount; i++) {
			VisualSignalTransition rise = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
			createConsumingArc(zero, rise);
			createProducingArc(rise, one);
			setPosition(rise, x - 0.0, y - 1.0 - i);
			riseList.add(rise);
		}

		return new SignalStg(zero, one, fallList, riseList);
	}

	private SignalStg generateSignalStg(String signalName, double x, double y, boolean initToOne, SignalTransition.Type type) throws InvalidConnectionException {
		VisualPlace zero = stg.createPlace(signalName + name0, null);
		zero.setNamePositioning(Positioning.BOTTOM);
		zero.setLabelPositioning(Positioning.TOP);
		setPosition(zero, x + 0.0, y + 2.0);

		VisualPlace one = stg.createPlace(signalName + name1, null);
		one.setNamePositioning(Positioning.TOP);
		one.setLabelPositioning(Positioning.BOTTOM);
		setPosition(one, x + 0.0, y - 2.0);

		if (initToOne) {
			one.getReferencedPlace().setTokens(1);
			zero.getReferencedPlace().setTokens(0);
		} else {
			one.getReferencedPlace().setTokens(0);
			zero.getReferencedPlace().setTokens(1);
		}

		VisualSignalTransition fall = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
		createConsumingArc(one, fall);
		createProducingArc(fall, zero);
		setPosition(fall, x + 3.0, y + 0.0);

		VisualSignalTransition rise = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
		createConsumingArc(zero, rise);
		createProducingArc(rise, one);
		setPosition(rise, x - 3.0, y - 0.0);

		return new SignalStg(zero, one, fall, rise);
	}


	public SignalStg getContactStg(VisualXmasContact contact) {
		return contactMap.get(contact);
	}

	private Point2D getComponentPosition(VisualXmasComponent component) {
		AffineTransform transform = TransformHelper.getTransformToRoot(component);
		double x =	xScaling * (transform.getTranslateX() + component.getX());
		double y =	yScaling * (transform.getTranslateY() + component.getY());
		return new Point2D.Double(x, y);
	}

	private void groupComponentStg(NodeStg nodeStg) {
		stg.select(nodeStg.getAllNodes());
		stg.groupSelection();
	}


	private SignalStg generateClockStg() throws InvalidConnectionException {
		String name = "clk";
		SignalStg clockStg = generateSignalStg(name, 0.0, 0.0, false, Type.INPUT);
		groupComponentStg(clockStg);
		return clockStg;
	}

	private void connectClockStg()  throws InvalidConnectionException {
		for(VisualSourceComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
			SourceStg sourceStg = getSourceStg(component);
			if (sourceStg != null) {
				createReadArcsBetweenSignals(sourceStg.dn, clockStg);
			}
		}
		for(VisualSinkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
			SinkStg sinkStg = getSinkStg(component);
			if (sinkStg != null) {
				createReadArcsBetweenSignals(sinkStg.dn, clockStg);
			}
		}
		for(VisualFunctionComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
			FunctionStg funcStg = getFunctionStg(component);
			if (funcStg != null) {
				createReadArcsBetweenSignals(funcStg.idn, clockStg);
				createReadArcsBetweenSignals(funcStg.odn, clockStg);
			}
		}
		for(VisualForkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualForkComponent.class)) {
			ForkStg forkStg = getForkStg(component);
			if (forkStg != null) {
				createReadArcsBetweenSignals(forkStg.idn, clockStg);
				createReadArcsBetweenSignals(forkStg.adn, clockStg);
				createReadArcsBetweenSignals(forkStg.bdn, clockStg);
			}
		}
		for(VisualJoinComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualJoinComponent.class)) {
		}
		for(VisualQueueComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualQueueComponent.class)) {

		}
	}


	private SourceStg generateSourceStg(VisualSourceComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		SignalStg oracleStg = generateOutputContactStg(name + _ORACLE, pos.getX() - 4.0, pos.getY(), false, Type.INPUT, 1, 1);
		SignalStg oStg = null;
		SignalStg dnStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oStg = generateOutputContactStg(name + _O_IRDY, pos.getX() + 4.0, pos.getY());
				contactMap.put(contact, oStg);
				dnStg = generateOutputContactStg(name + _O_IDN, pos.getX() + 4.0, pos.getY() - 4.0);
			}
		}
		if ((oStg != null) && (oracleStg != null)) {
			createReadArc(oracleStg.zero, oStg.fallList.get(0));
			createReadArc(oracleStg.one, oStg.riseList.get(0));
		}
		SourceStg sourceStg = new SourceStg(oStg, oracleStg, dnStg);
		groupComponentStg(sourceStg);
		return sourceStg;
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
			SignalStg oStg = getContactStg(oContact);
			createReadArc(oStg.one, sourceStg.oracle.fallList.get(0));
		}
	}

	public SourceStg getSourceStg(VisualSourceComponent component) {
		return sourceMap.get(component);
	}


	private SinkStg generateSinkStg(VisualSinkComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		SignalStg oracleStg = generateInputContactStg(name + _ORACLE, pos.getX() + 4.0, pos.getY(), false, Type.INPUT, 1, 1);
		SignalStg iStg = null;
		SignalStg dnStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateInputContactStg(name + _I_TRDY, pos.getX() - 4.0, pos.getY());
				contactMap.put(contact, iStg);
				dnStg = generateOutputContactStg(name + _I_TDN, pos.getX() + 4.0, pos.getY() + 4.0);
			}
		}
		if ((iStg != null) && (oracleStg != null)) {
			createReadArc(oracleStg.zero, iStg.fallList.get(0));
			createReadArc(oracleStg.one, iStg.riseList.get(0));
		}
		SinkStg sinkStg = new SinkStg(iStg, oracleStg, dnStg);
		groupComponentStg(sinkStg);
		return sinkStg;
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
			SignalStg iStg = getContactStg(iContact);
			if ((sinkStg != null) && (iStg !=null)) {
				createReadArc(iStg.one, sinkStg.oracle.fallList.get(0));
			}
		}
	}

	public SinkStg getSinkStg(VisualSinkComponent component) {
		return sinkMap.get(component);
	}


	private FunctionStg generateFunctionStg(VisualFunctionComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		SignalStg iStg = null;
		SignalStg oStg = null;
		SignalStg idnStg = null;
		SignalStg odnStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateInputContactStg(name + _I_TRDY, pos.getX(), pos.getY() + 3.0);
				contactMap.put(contact, iStg);
				idnStg = generateOutputContactStg(name + _I_TDN, pos.getX(), pos.getY() + 9.0);
			} else {
				oStg = generateOutputContactStg(name + _O_IRDY, pos.getX(), pos.getY() - 3.0);
				contactMap.put(contact, oStg);
				odnStg = generateOutputContactStg(name + _O_IDN, pos.getX(), pos.getY() - 9.0);
			}
		}

		FunctionStg functionStg = new FunctionStg(iStg, oStg, idnStg, odnStg);
		groupComponentStg(functionStg);
		return functionStg;
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
			SignalStg iStg = getContactStg(iContact);
			if ((functionStg != null) && (iStg != null)) {
				createReadArc(iStg.zero, functionStg.o.fallList.get(0));
				createReadArc(iStg.one, functionStg.o.riseList.get(0));
			}
		}
		if (oContact != null) {
			SignalStg oStg = getContactStg(oContact);
			if ((functionStg != null) && (oStg != null)) {
				createReadArc(oStg.zero, functionStg.i.fallList.get(0));
				createReadArc(oStg.one, functionStg.i.riseList.get(0));
			}
		}
	}

	public FunctionStg getFunctionStg(VisualFunctionComponent component) {
		return functionMap.get(component);
	}


	private ForkStg generateForkStg(VisualForkComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		SignalStg iStg = null;
		SignalStg aStg = null;
		SignalStg bStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isInput()) {
				iStg = generateInputContactStg(name + _I_TRDY, pos.getX(), pos.getY(), false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, iStg);
			} else if (aStg == null) {
				aStg = generateOutputContactStg(name + _A_IRDY, pos.getX(), pos.getY() - 5.0, false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, aStg);
			} else {
				bStg = generateOutputContactStg(name + _B_IRDY, pos.getX(), pos.getY() + 5.0, false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, bStg);
			}
		}
		ForkStg forkStg = new ForkStg(iStg, aStg, bStg);
		groupComponentStg(forkStg);
		return forkStg;
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
			SignalStg iStg = getContactStg(iContact);
			if ((forkStg != null) && (iStg != null)) {
				createReadArc(iStg.zero, forkStg.a.fallList.get(0));
				createReadArc(iStg.zero, forkStg.b.fallList.get(0));
				createReadArc(iStg.one, forkStg.a.riseList.get(0));
				createReadArc(iStg.one, forkStg.b.riseList.get(0));
			}
		}
		if (aContact != null) {
			SignalStg aStg = getContactStg(aContact);
			if ((forkStg != null) && (aStg != null)) {
				createReadArc(aStg.zero, forkStg.i.fallList.get(0));
				createReadArc(aStg.zero, forkStg.b.fallList.get(1));
				createReadArc(aStg.one, forkStg.i.riseList.get(0));
				createReadArc(aStg.one, forkStg.b.riseList.get(0));
			}
		}
		if (bContact != null) {
			SignalStg bStg = getContactStg(bContact);
			if ((forkStg != null) && (bStg != null)) {
				createReadArc(bStg.zero, forkStg.i.fallList.get(1));
				createReadArc(bStg.zero, forkStg.a.fallList.get(1));
				createReadArc(bStg.one, forkStg.i.riseList.get(0));
				createReadArc(bStg.one, forkStg.a.riseList.get(0));
			}
		}
	}

	public ForkStg getForkStg(VisualForkComponent component) {
		return forkMap.get(component);
	}


	private JoinStg generateJoinStg(VisualJoinComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		SignalStg aStg = null;
		SignalStg bStg = null;
		SignalStg oStg = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oStg = generateOutputContactStg(name + _O_IRDY, pos.getX(), pos.getY(), false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, oStg);
			} else if (aStg == null) {
				aStg = generateInputContactStg(name + _A_TRDY, pos.getX(), pos.getY() - 5.0, false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, aStg);
			} else {
				bStg = generateInputContactStg(name + _B_TRDY, pos.getX(), pos.getY() + 5.0, false, Type.INTERNAL, 2, 1);
				contactMap.put(contact, bStg);
			}
		}
		JoinStg joinStg = new JoinStg(aStg, bStg, oStg);
		groupComponentStg(joinStg);
		return joinStg;
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
			SignalStg aStg = getContactStg(aContact);
			if ((joinStg != null) && (aStg != null)) {
				createReadArc(aStg.zero, joinStg.b.fallList.get(1));
				createReadArc(aStg.zero, joinStg.o.fallList.get(0));
				createReadArc(aStg.one, joinStg.b.riseList.get(0));
				createReadArc(aStg.one, joinStg.o.riseList.get(0));
			}
		}
		if (bContact != null) {
			SignalStg bStg = getContactStg(bContact);
			if ((joinStg != null) && (bStg != null)) {
				createReadArc(bStg.zero, joinStg.a.fallList.get(1));
				createReadArc(bStg.zero, joinStg.o.fallList.get(1));
				createReadArc(bStg.one, joinStg.a.riseList.get(0));
				createReadArc(bStg.one, joinStg.o.riseList.get(0));
			}
		}
		if (oContact != null) {
			SignalStg oStg = getContactStg(oContact);
			if ((joinStg != null) && (oStg != null)) {
				createReadArc(oStg.zero, joinStg.a.fallList.get(0));
				createReadArc(oStg.zero, joinStg.b.fallList.get(0));
				createReadArc(oStg.one, joinStg.a.riseList.get(0));
				createReadArc(oStg.one, joinStg.b.riseList.get(0));
			}
		}
	}

	public JoinStg getJoinStg(VisualJoinComponent component) {
		return joinMap.get(component);
	}


	private QueueStg generateQueueStg(VisualQueueComponent component) throws InvalidConnectionException {
		String name = xmas.getMathName(component);
		Point2D pos = getComponentPosition(component);
		int capacity = component.getReferencedQueueComponent().getCapacity();
		SignalStg iStg = null;
		SignalStg oStg = null;
		ArrayList<SignalStg> memList = new ArrayList<>(capacity);
		ArrayList<SignalStg> headList = new ArrayList<>(capacity);
		ArrayList<SignalStg> tailList = new ArrayList<>(capacity);
		double xContact = 5.0 * capacity + 5.0;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oStg = generateOutputContactStg(name + _O_IRDY, pos.getX() + xContact, pos.getY() - 5.0, false, Type.INTERNAL, 1, capacity);
				contactMap.put(contact, oStg);
			} else {
				iStg = generateInputContactStg(name + _I_TRDY, pos.getX() - xContact, pos.getY() + 5.0, true, Type.INTERNAL, 1, capacity);
				contactMap.put(contact, iStg);
			}
		}
		for (int i = 0; i < capacity; i++) {
			double xSlot = 10.0 * (i - 0.5 * (capacity - 1));
			char c = (char)i;
			c += 'A';
			SignalStg memStg = generateSignalStg(name + _MEM + c, pos.getX() + xSlot, pos.getY(), false, SignalTransition.Type.INPUT);
			SignalStg headStg = generateSignalStg(name + _HEAD + c, pos.getX() + xSlot, pos.getY() - 8.0, (i == 0), SignalTransition.Type.INTERNAL);
			SignalStg tailStg = generateSignalStg(name + _TAIL + c, pos.getX() + xSlot, pos.getY() + 8.0, false, SignalTransition.Type.INTERNAL);
			memList.add(memStg);
			headList.add(headStg);
			tailList.add(tailStg);
		}
		// Internal connections
		for (int i = 0; i < capacity; i++) {
			SignalStg mem = memList.get(i);
			createReadArc(iStg.one, mem.riseList.get(0));
			createReadArc(oStg.one, mem.fallList.get(0));
			createReadArc(mem.one, iStg.fallList.get(0));
			createReadArc(mem.one, oStg.riseList.get(i));
			createReadArc(mem.zero, iStg.riseList.get(i));
			createReadArc(mem.zero, oStg.fallList.get(0));
			for (int j = 0; j < capacity; j++) {
				SignalStg head = headList.get(j);
				SignalStg tail = tailList.get(j);
				if (i == j) {
					createReadArc(mem.one, head.fallList.get(0));
					createReadArc(mem.one, tail.riseList.get(0));
					createReadArc(mem.zero, head.riseList.get(0));
					createReadArc(mem.zero, tail.fallList.get(0));
					createReadArc(head.one, mem.riseList.get(0));
					createReadArc(tail.one, mem.fallList.get(0));
				} else {
					createReadArc(mem.one, head.riseList.get(0));
					createReadArc(mem.zero, tail.riseList.get(0));
				}
			}
		}
		QueueStg queueStg = new QueueStg(iStg, oStg, memList, headList, tailList);
		groupComponentStg(queueStg);
		return queueStg;
	}

	private void connectQueueStg(VisualQueueComponent component) throws InvalidConnectionException {
		VisualXmasContact iContact = null;
		VisualXmasContact oContact = null;
		for (VisualXmasContact contact: component.getContacts()) {
			if (contact.isOutput()) {
				oContact =  XmasUtils.getConnectedContact(xmas, contact);
			} else {
				iContact =  XmasUtils.getConnectedContact(xmas, contact);
			}
		}
		QueueStg queueStg = getQueueStg(component);
		if (iContact != null) {
			SignalStg iStg = getContactStg(iContact);
			if ((queueStg != null) && (iStg != null)) {
				for (SignalStg mem: queueStg.memList) {
					createReadArc(iStg.one, mem.riseList.get(0));
				}
			}
		}
		if (oContact != null) {
			SignalStg oStg = getContactStg(oContact);
			if ((queueStg != null) && (oStg != null)) {
				for (SignalStg mem: queueStg.memList) {
					createReadArc(oStg.one, mem.fallList.get(0));
				}
			}
		}
	}

	public QueueStg getQueueStg(VisualQueueComponent component) {
		return queueMap.get(component);
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
