package org.workcraft.plugins.sdfs.stg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.sdfs.VisualCounterflowLogic;
import org.workcraft.plugins.sdfs.VisualCounterflowRegister;
import org.workcraft.plugins.sdfs.VisualSpreadtokenLogic;
import org.workcraft.plugins.sdfs.VisualSpreadtokenRegister;
import org.workcraft.plugins.sdfs.VisualSDFS;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public class STGGenerator {
	private static final String nameC 		= "C_";
	private static final String nameFwC 	= "fwC_";
	private static final String nameBwC 	= "bwC_";
	private static final String nameE 		= "E_";
	private static final String nameFwE 	= "fwE_";
	private static final String nameBwE 	= "bwE_";
	private static final String nameM 		= "M_";
	private static final String nameOrM 	= "orM_";
	private static final String nameAndM 	= "andM_";
	private static final String name0		= "_0";
	private static final String name1 		= "_1";
	private static final String labelC 		= "C(";
	private static final String labelFwC	= "fwC(";
	private static final String labelBwC	= "bwC(";
	private static final String labelE 		= "E(";
	private static final String labelFwE	= "fwE(";
	private static final String labelBwE	= "bwE(";
	private static final String labelM 		= "M(";
	private static final String labelOrM 	= "orM(";
	private static final String labelAndM 	= "andM(";
	private static final String label0 		= ")=0";
	private static final String label1 		= ")=1";
	private static final double xScaling = 10;
	private static final double yScaling = 10;

	private Map<VisualSpreadtokenLogic, SpreadtokenLogicSTG> logicMap = new HashMap<VisualSpreadtokenLogic, SpreadtokenLogicSTG>();
	private Map<VisualSpreadtokenRegister, SpreadtokenRegisterSTG> registerMap = new HashMap<VisualSpreadtokenRegister, SpreadtokenRegisterSTG>();
	private Map<VisualCounterflowLogic, CounterflowLogicSTG> counterflowLogicMap = new HashMap<VisualCounterflowLogic, CounterflowLogicSTG>();
	private Map<VisualCounterflowRegister, CounterflowRegisterSTG> counterflowRegisterMap = new HashMap<VisualCounterflowRegister, CounterflowRegisterSTG>();
	private final VisualSDFS sdfs;
	private final VisualSTG stg;

	public VisualSTG getSTG() {
		return stg;
	}

	static void setPosition(Movable node, double x, double y) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
	}

	public STGGenerator(VisualSDFS sdfs) {
		this.sdfs = sdfs;
		try {
			this.stg = new VisualSTG(new STG());

			for(VisualSpreadtokenLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualSpreadtokenLogic.class)) {
				SpreadtokenLogicSTG lstg = generateSpreadtokenLogicSTG(l);
				logicMap.put(l, lstg);
			}
			for(VisualSpreadtokenRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualSpreadtokenRegister.class)) {
				SpreadtokenRegisterSTG rstg = generateSpreadtokenRegisterSTG(r);
				registerMap.put(r, rstg);
			}

			for(VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowLogic.class)) {
				CounterflowLogicSTG lstg = generateCounterflowLogicSTG(l);
				counterflowLogicMap.put(l, lstg);
			}
			for(VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowRegister.class)) {
				CounterflowRegisterSTG rstg = generateCounterflowRegisterSTG(r);
				counterflowRegisterMap.put(r, rstg);
			}

			for(VisualSpreadtokenLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualSpreadtokenLogic.class)) {
				connectSpreadtokenLogicSTG(l);
			}
			for(VisualSpreadtokenRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualSpreadtokenRegister.class)) {
				connectSpreadtokenRegisterSTG(r);
			}

			for(VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowLogic.class)) {
				connectCounterflowLogicSTG(l);
			}
			for(VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowRegister.class)) {
				connectCounterflowRegisterSTG(r);
			}
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private void createReadArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
		stg.connect(p, t);
		stg.connect(t, p);
	}

	private SpreadtokenLogicSTG generateSpreadtokenLogicSTG(VisualSpreadtokenLogic l) throws InvalidConnectionException {
		String name = sdfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		double x =	xScaling * (transform.getTranslateX() + l.getX());
		double y =	yScaling * (transform.getTranslateY() + l.getY());
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace C0 = stg.createPlace(nameC + name + name0);
		C0.setLabel(labelC + name + label0);
		C0.setLabelPositioning(Positioning.BOTTOM);
		C0.setTokens(1);
		setPosition(C0, x + 2.0, y + 1.0);
		nodes.add(C0);

		VisualPlace C1 = stg.createPlace(nameC + name + name1);
		C1.setLabel(labelC + name + label1);
		C1.setLabelPositioning(Positioning.TOP);
		C1.setTokens(0);
		setPosition(C1, x + 2.0, y - 1.0);
		nodes.add(C1);

		Map<Node, VisualSignalTransition> CRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition CR = null;
		Map<Node, VisualSignalTransition> CFs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition CF = null;
		double dy = 0.0;
		for (Node n: sdfs.getPreset(l)) {
			if (n instanceof VisualSpreadtokenLogic || n instanceof VisualSpreadtokenRegister) {
				if (CR == null || !l.isIndicating()) {
					CR = stg.createSignalTransition(nameC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
					stg.connect(C0, CR);
					stg.connect(CR, C1);
					setPosition(CR, x - 2.0, y + 1.0 + dy);
					nodes.add(CR);
				}
				CRs.put(n, CR);
				if (CF == null) {
					CF = stg.createSignalTransition(nameC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
					stg.connect(C1, CF);
					stg.connect(CF, C0);
					setPosition(CF, x - 2.0, y - 1.0 - dy);
					nodes.add(CF);
				}
				CFs.put(n, CF);
				dy += 1.0;
			}
		}

		stg.groupCollection(nodes);
		return new SpreadtokenLogicSTG(C0, C1, CRs, CFs);
	}

	private void connectSpreadtokenLogicSTG(VisualSpreadtokenLogic l) throws InvalidConnectionException {
		SpreadtokenLogicSTG lstg = getSpreadtokenLogicSTG(l);
		for (Node n: sdfs.getPreset(l)) {
			if (n instanceof VisualSpreadtokenLogic) {
				SpreadtokenLogicSTG nstg = getSpreadtokenLogicSTG((VisualSpreadtokenLogic)n);
				createReadArc(nstg.C1, lstg.CRs.get(n));
				createReadArc(nstg.C0, lstg.CFs.get(n));
			} else if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M1, lstg.CRs.get(n));
				createReadArc(nstg.M0, lstg.CFs.get(n));
			}
		}
	}

	public SpreadtokenLogicSTG getSpreadtokenLogicSTG(VisualSpreadtokenLogic logic) {
		return logicMap.get(logic);
	}

	private SpreadtokenRegisterSTG generateSpreadtokenRegisterSTG(VisualSpreadtokenRegister r) throws InvalidConnectionException {
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		return generateSpreadtokenRegisterSTG(r, sdfs.getName(r), new Point2D.Double(x, y));
	}

	private SpreadtokenRegisterSTG generateSpreadtokenRegisterSTG(
			VisualSpreadtokenRegister r, String name, Point2D center) throws InvalidConnectionException {
		double x =	center.getX();
		double y =	center.getY();
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace e0 = stg.createPlace(nameE + name + name0);
		e0.setLabel(labelE + name + label0);
		e0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isEnabled()) e0.setTokens(1);
		setPosition(e0, x + 2.0, y - 2.0);
		nodes.add(e0);

		VisualPlace e1 = stg.createPlace(nameE + name + name1);
		e1.setLabel(labelE + name + label1);
		e1.setLabelPositioning(Positioning.TOP);
		if (r.isEnabled()) e1.setTokens(1);
		setPosition(e1, x + 2.0, y - 4.0);
		nodes.add(e1);

		VisualSignalTransition er = stg.createSignalTransition(nameE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(e0, er);
		stg.connect(er, e1);
		setPosition(er, x - 2.0, y - 2.0);
		nodes.add(er);

		VisualSignalTransition ef = stg.createSignalTransition(nameE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(e1, ef);
		stg.connect(ef, e0);
		setPosition(ef, x - 2.0, y - 4.0);
		nodes.add(ef);

		VisualPlace m0 = stg.createPlace(nameM + name + name0);
		m0.setLabel(labelM + name + label0);
		m0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isMarked()) m0.setTokens(1);
		setPosition(m0, x + 2.0, y + 4.0);
		nodes.add(m0);

		VisualPlace m1 = stg.createPlace(nameM + name + name1);
		m1.setLabel(labelM + name + label1);
		m1.setLabelPositioning(Positioning.TOP);
		if (r.isMarked()) m1.setTokens(1);
		setPosition(m1, x + 2.0, y + 2.0);
		nodes.add(m1);

		VisualSignalTransition mr = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(m0, mr);
		stg.connect(mr, m1);
		setPosition(mr, x - 2.0, y + 4.0);
		nodes.add(mr);

		VisualSignalTransition mf = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(m1, mf);
		stg.connect(mf, m0);
		setPosition(mf, x - 2.0, y + 2.0);
		nodes.add(mf);

		stg.groupCollection(nodes);
		return new SpreadtokenRegisterSTG(e0, e1, er, ef, m0, m1, mr, mf);
	}

	private void connectSpreadtokenRegisterSTG(VisualSpreadtokenRegister r) throws InvalidConnectionException {
		SpreadtokenRegisterSTG rstg = getSpreadtokenRegisterSTG(r);
		// enabling / disabling
		for (Node n: sdfs.getPreset(r)) {
			if (n instanceof VisualSpreadtokenLogic) {
				SpreadtokenLogicSTG nstg = getSpreadtokenLogicSTG((VisualSpreadtokenLogic)n);
				createReadArc(nstg.C1, rstg.ER);
				createReadArc(nstg.C0, rstg.EF);
			} else if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M1, rstg.ER);
				createReadArc(nstg.M0, rstg.EF);
			}
			if (n instanceof VisualCounterflowLogic) {
				CounterflowLogicSTG nstg = getCounterflowLogicSTG((VisualCounterflowLogic)n);
				createReadArc(nstg.fwC1, rstg.ER);
				createReadArc(nstg.fwC0, rstg.EF);
			} else if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, rstg.ER);
				createReadArc(nstg.orM0, rstg.EF);
			}
		}
		// marking / unmarking
		createReadArc(rstg.E1, rstg.MR);
		createReadArc(rstg.E0, rstg.MF);
		for (VisualSpreadtokenRegister n: getSpreadtokenRegisterPreset(r)) {
			if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M1, rstg.MR);
				createReadArc(nstg.M0, rstg.MF);
			}
		}
		for (VisualSpreadtokenRegister n: getSpreadtokenRegisterPostset(r)) {
			if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M0, rstg.MR);
				createReadArc(nstg.M1, rstg.MF);
			}
		}
		for (VisualCounterflowRegister n: getCounterflowRegisterPostset(r)) {
			if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.andM0, rstg.MR);
				createReadArc(nstg.andM1, rstg.MF);
			}
		}
		createReadArc(rstg.M1, rstg.EF);
		createReadArc(rstg.M0, rstg.ER);
	}

	public SpreadtokenRegisterSTG getSpreadtokenRegisterSTG(VisualSpreadtokenRegister register) {
		return registerMap.get(register);
	}

	public Set<VisualSpreadtokenRegister> getSpreadtokenRegisterPreset(VisualComponent component) {
		Set<VisualSpreadtokenRegister> result = new HashSet<VisualSpreadtokenRegister>();
		Set<VisualComponent> visited = new HashSet<VisualComponent>();
		Queue<VisualComponent> queue = new LinkedList<VisualComponent>();
		queue.add(component);
		while (!queue.isEmpty()) {
			VisualComponent currentComponent = queue.remove();
			if (visited.contains(currentComponent)) continue;
			visited.add(currentComponent);
			for (Node prevNode: sdfs.getPreset(currentComponent)) {
				if (prevNode instanceof VisualComponent) {
					VisualComponent prevComponent = (VisualComponent) prevNode;
					if (prevComponent instanceof VisualSpreadtokenRegister) {
						result.add((VisualSpreadtokenRegister)prevComponent);
					} else if ( !(prevComponent instanceof VisualCounterflowRegister) ) {
						queue.add(prevComponent);
					}
				}
			}
		}
		return result;
	}

	public Set<VisualSpreadtokenRegister> getSpreadtokenRegisterPostset(VisualComponent component) {
		Set<VisualSpreadtokenRegister> result = new HashSet<VisualSpreadtokenRegister>();
		Set<VisualComponent> visited = new HashSet<VisualComponent>();
		Queue<VisualComponent> queue = new LinkedList<VisualComponent>();
		queue.add(component);
		while (!queue.isEmpty()) {
			VisualComponent currentComponent = queue.remove();
			if (visited.contains(currentComponent)) continue;
			visited.add(currentComponent);
			for (Node succNode: sdfs.getPostset(currentComponent)) {
				if (succNode instanceof VisualComponent) {
					VisualComponent succComponent = (VisualComponent) succNode;
					if (succComponent instanceof VisualSpreadtokenRegister) {
						result.add((VisualSpreadtokenRegister)succComponent);
					} else if ( !(succComponent instanceof VisualCounterflowRegister) ) {
						queue.add(succComponent);
					}
				}
			}
		}
		return result;
	}

	private CounterflowLogicSTG generateCounterflowLogicSTG(VisualCounterflowLogic l) throws InvalidConnectionException {
		String name = sdfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		double x =	xScaling * (transform.getTranslateX() + l.getX());
		double y = yScaling * (transform.getTranslateY() + l.getY());
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace fwC0 = stg.createPlace(nameFwC + name + name0);
		fwC0.setLabel(labelFwC + name + label0);
		fwC0.setLabelPositioning(Positioning.BOTTOM);
		fwC0.setTokens(1);
		setPosition(fwC0, x + 2.0, y - 2.0);
		nodes.add(fwC0);

		VisualPlace fwC1 = stg.createPlace(nameFwC + name + name1);
		fwC1.setLabel(labelFwC + name + label1);
		fwC1.setLabelPositioning(Positioning.TOP);
		fwC1.setTokens(0);
		setPosition(fwC1, x + 2.0, y - 4.0);
		nodes.add(fwC1);

		Map<Node, VisualSignalTransition> fwCRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition fwCR = null;
		Map<Node, VisualSignalTransition> fwCFs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition fwCF = null;
		double dy = 0.0;
		for (Node n: sdfs.getPreset(l)) {
			if (n instanceof VisualCounterflowLogic || n instanceof VisualCounterflowRegister) {
				if (fwCR == null || !l.isForwardIndicating()) {
					fwCR = stg.createSignalTransition(nameFwC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
					stg.connect(fwC0, fwCR);
					stg.connect(fwCR, fwC1);
					setPosition(fwCR, x - 2.0, y - 2.0 + dy);
					nodes.add(fwCR);
				}
				fwCRs.put(n, fwCR);
				if (fwCF == null) {
					fwCF = stg.createSignalTransition(nameFwC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
					stg.connect(fwC1, fwCF);
					stg.connect(fwCF, fwC0);
					setPosition(fwCF, x - 2.0, y - 4.0 - dy);
					nodes.add(fwCF);
				}
				fwCFs.put(n, fwCF);
				dy += 1.0;
			}
		}

		VisualPlace bwC0 = stg.createPlace(nameBwC + name + name0);
		bwC0.setLabel(labelBwC + name + label0);
		bwC0.setLabelPositioning(Positioning.BOTTOM);
		bwC0.setTokens(1);
		setPosition(bwC0, x - 2.0, y + 4.0);
		nodes.add(bwC0);

		VisualPlace bwC1 = stg.createPlace(nameBwC + name + name1);
		bwC1.setLabel(labelBwC + name + label1);
		bwC1.setLabelPositioning(Positioning.TOP);
		bwC1.setTokens(0);
		setPosition(bwC1, x - 2.0, y + 2.0);
		nodes.add(bwC1);

		Map<Node, VisualSignalTransition> bwCRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition bwCR = null;
		Map<Node, VisualSignalTransition> bwCFs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition bwCF = null;
		dy = 0.0;
		for (Node n: sdfs.getPostset(l)) {
			if (n instanceof VisualCounterflowLogic || n instanceof VisualCounterflowRegister) {
				if (bwCR == null || !l.isBackwardIndicating()) {
					bwCR = stg.createSignalTransition(nameBwC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
					stg.connect(bwC0, bwCR);
					stg.connect(bwCR, bwC1);
					setPosition(bwCR, x + 2.0, y + 4.0 + dy);
					nodes.add(bwCR);
				}
				bwCRs.put(n, bwCR);
				if (bwCF == null) {
					bwCF = stg.createSignalTransition(nameBwC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
					stg.connect(bwC1, bwCF);
					stg.connect(bwCF, bwC0);
					setPosition(bwCF, x + 2.0, y + 2.0 - dy);
					nodes.add(bwCF);
				}
				bwCFs.put(n, bwCF);
				dy += 1.0;
			}
		}

		stg.groupCollection(nodes);
		return new CounterflowLogicSTG(fwC0, fwC1, fwCRs, fwCFs, bwC0, bwC1, bwCRs, bwCFs);
	}

	private void connectCounterflowLogicSTG(VisualCounterflowLogic l) throws InvalidConnectionException {
		CounterflowLogicSTG lstg = getCounterflowLogicSTG(l);
		for (Node n: sdfs.getPreset(l)) {
			if (n instanceof VisualCounterflowLogic) {
				CounterflowLogicSTG nstg = getCounterflowLogicSTG((VisualCounterflowLogic)n);
				createReadArc(nstg.fwC1, lstg.fwCRs.get(n));
				createReadArc(nstg.fwC0, lstg.fwCFs.get(n));
			} else if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, lstg.fwCRs.get(n));
				createReadArc(nstg.orM0, lstg.fwCFs.get(n));
			}
		}
		for (Node n: sdfs.getPostset(l)) {
			if (n instanceof VisualCounterflowLogic) {
				CounterflowLogicSTG nstg = getCounterflowLogicSTG((VisualCounterflowLogic)n);
				createReadArc(nstg.bwC1, lstg.bwCRs.get(n));
				createReadArc(nstg.bwC0, lstg.bwCFs.get(n));
			} else if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, lstg.bwCRs.get(n));
				createReadArc(nstg.orM0, lstg.bwCFs.get(n));
			}
		}
	}

	public CounterflowLogicSTG getCounterflowLogicSTG(VisualCounterflowLogic logic) {
		return counterflowLogicMap.get(logic);
	}

	private CounterflowRegisterSTG generateCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x = xScaling * (transform.getTranslateX() + r.getX());
		double y = yScaling * (transform.getTranslateY() + r.getY());
		return generateCounterflowRegisterSTG(r, sdfs.getName(r), new Point2D.Double(x, y));
	}

	private CounterflowRegisterSTG generateCounterflowRegisterSTG(
				VisualCounterflowRegister r, String name, Point2D center) throws InvalidConnectionException {
		double x = center.getX();
		double y = center.getY();
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace fwE0 = stg.createPlace(nameFwE + name + name0);
		fwE0.setLabel(labelFwE + name + label0);
		fwE0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isForwardEnabled()) fwE0.setTokens(1);
		setPosition(fwE0, x - 2.0, y - 2.0);
		nodes.add(fwE0);

		VisualPlace fwE1 = stg.createPlace(nameFwE + name + name1);
		fwE1.setLabel(labelFwE + name + label1);
		fwE1.setLabelPositioning(Positioning.TOP);
		if (r.isForwardEnabled()) fwE1.setTokens(1);
		setPosition(fwE1, x - 2.0, y - 4.0);
		nodes.add(fwE1);

		VisualSignalTransition fwER = stg.createSignalTransition(nameFwE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(fwE0, fwER);
		stg.connect(fwER, fwE1);
		setPosition(fwER, x - 6.0, y - 2.0);
		nodes.add(fwER);

		VisualSignalTransition fwEF = stg.createSignalTransition(nameFwE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(fwE1, fwEF);
		stg.connect(fwEF, fwE0);
		setPosition(fwEF, x - 6.0, y - 4.0);
		nodes.add(fwEF);

		VisualPlace bwE0 = stg.createPlace(nameBwE + name + name0);
		bwE0.setLabel(labelBwE + name + label0);
		bwE0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isBackwardEnabled()) bwE0.setTokens(1);
		setPosition(bwE0, x - 6.0, y + 4.0);
		nodes.add(bwE0);

		VisualPlace bwE1 = stg.createPlace(nameBwE + name + name1);
		bwE1.setLabel(labelBwE + name + label1);
		bwE1.setLabelPositioning(Positioning.TOP);
		if (r.isBackwardEnabled()) bwE1.setTokens(1);
		setPosition(bwE1, x - 6.0, y + 2.0);
		nodes.add(bwE1);

		VisualSignalTransition bwER = stg.createSignalTransition(nameBwE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(bwE0, bwER);
		stg.connect(bwER, bwE1);
		setPosition(bwER, x - 2.0, y + 4.0);
		nodes.add(bwER);

		VisualSignalTransition bwEF = stg.createSignalTransition(nameBwE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(bwE1, bwEF);
		stg.connect(bwEF, bwE0);
		setPosition(bwEF, x - 2.0, y + 2.0);
		nodes.add(bwEF);

		VisualPlace orM0 = stg.createPlace(nameOrM + name + name0);
		orM0.setLabel(labelOrM + name + label0);
		orM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isOrMarked()) orM0.setTokens(1);
		setPosition(orM0, x + 6.0, y - 2.0);
		nodes.add(orM0);

		VisualPlace orM1 = stg.createPlace(nameOrM + name + name1);
		orM1.setLabel(labelOrM + name + label1);
		orM1.setLabelPositioning(Positioning.TOP);
		if (r.isOrMarked()) orM1.setTokens(1);
		setPosition(orM1, x + 6.0, y - 4.0);
		nodes.add(orM1);

		VisualSignalTransition orMRfw = stg.createSignalTransition(nameOrM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(orM0, orMRfw);
		stg.connect(orMRfw, orM1);
		setPosition(orMRfw, x + 2.0, y - 2.5);
		nodes.add(orMRfw);

		VisualSignalTransition orMRbw = stg.createSignalTransition(nameOrM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(orM0, orMRbw);
		stg.connect(orMRbw, orM1);
		setPosition(orMRbw, x + 2.0, y - 1.5);
		nodes.add(orMRbw);

		VisualSignalTransition orMFfw = stg.createSignalTransition(nameOrM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(orM1, orMFfw);
		stg.connect(orMFfw, orM0);
		setPosition(orMFfw, x + 2.0, y - 4.5);
		nodes.add(orMFfw);

		VisualSignalTransition orMFbw = stg.createSignalTransition(nameOrM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(orM1, orMFbw);
		stg.connect(orMFbw, orM0);
		setPosition(orMFbw, x + 2.0, y - 3.5);
		nodes.add(orMFbw);

		VisualPlace andM0 = stg.createPlace(nameAndM + name + name0);
		andM0.setLabel(labelAndM + name + label0);
		andM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isAndMarked()) andM0.setTokens(1);
		setPosition(andM0, x + 6.0, y + 4.0);
		nodes.add(andM0);

		VisualPlace andM1 = stg.createPlace(nameAndM + name + name1);
		andM1.setLabel(labelAndM + name + label1);
		andM1.setLabelPositioning(Positioning.TOP);
		if (r.isAndMarked()) andM1.setTokens(1);
		setPosition(andM1, x + 6.0, y + 2.0);
		nodes.add(andM1);

		VisualSignalTransition andMR = stg.createSignalTransition(nameAndM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(andM0, andMR);
		stg.connect(andMR, andM1);
		setPosition(andMR, x + 2.0, y + 4.0);
		nodes.add(andMR);

		VisualSignalTransition andMF = stg.createSignalTransition(nameAndM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(andM1, andMF);
		stg.connect(andMF, andM0);
		setPosition(andMF, x + 2.0, y + 2.0);
		nodes.add(andMF);

		stg.groupCollection(nodes);
		return new CounterflowRegisterSTG(fwE0, fwE1, fwER, fwEF, bwE0, bwE1, bwER, bwEF,
				orM0, orM1, orMRfw, orMRbw, orMFfw, orMFbw, andM0, andM1, andMR, andMF);
	}

	private void connectCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		CounterflowRegisterSTG rstg = getCounterflowRegisterSTG(r);
		Set<VisualCounterflowRegister> cfSet = getCounterflowRegisterPreset(r);
		cfSet.addAll(getCounterflowRegisterPostset(r));
		// forward - enabling / disabling
		createReadArc(rstg.andM0, rstg.fwER);
		createReadArc(rstg.andM1, rstg.fwEF);
		for (Node n: sdfs.getPreset(r)) {
			if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M1, rstg.fwER);
				createReadArc(nstg.M0, rstg.fwEF);
			} else if (n instanceof VisualCounterflowLogic) {
				CounterflowLogicSTG nstg = getCounterflowLogicSTG((VisualCounterflowLogic)n);
				createReadArc(nstg.fwC1, rstg.fwER);
				createReadArc(nstg.fwC0, rstg.fwEF);
			} else if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, rstg.fwER);
				createReadArc(nstg.orM0, rstg.fwEF);
			}
		}
		// backward - enabling / disabling
		createReadArc(rstg.andM0, rstg.bwER);
		createReadArc(rstg.andM1, rstg.bwEF);
		for (Node n: sdfs.getPostset(r)) {
			if (n instanceof VisualSpreadtokenRegister) {
				SpreadtokenRegisterSTG nstg = getSpreadtokenRegisterSTG((VisualSpreadtokenRegister)n);
				createReadArc(nstg.M1, rstg.bwER);
				createReadArc(nstg.M0, rstg.bwEF);
			} else if (n instanceof VisualCounterflowLogic) {
				CounterflowLogicSTG nstg = getCounterflowLogicSTG((VisualCounterflowLogic)n);
				createReadArc(nstg.bwC1, rstg.bwER);
				createReadArc(nstg.bwC0, rstg.bwEF);
			} else if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, rstg.bwER);
				createReadArc(nstg.orM0, rstg.bwEF);
			}
		}
		// OR - marking / unmarking
		createReadArc(rstg.andM0, rstg.orMRfw);
		createReadArc(rstg.andM0, rstg.orMRbw);
		createReadArc(rstg.fwE1, rstg.orMRfw);
		createReadArc(rstg.bwE1, rstg.orMRbw);
		createReadArc(rstg.andM1, rstg.orMFfw);
		createReadArc(rstg.andM1, rstg.orMFbw);
		createReadArc(rstg.fwE0, rstg.orMFfw);
		createReadArc(rstg.bwE0, rstg.orMFbw);
		for (VisualCounterflowRegister n: cfSet) {
			if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.andM0, rstg.orMRfw);
				createReadArc(nstg.andM0, rstg.orMRbw);
				createReadArc(nstg.andM1, rstg.orMFfw);
				createReadArc(nstg.andM1, rstg.orMFbw);
			}
		}
		// AND - marking / unmarking
		createReadArc(rstg.orM1, rstg.andMR);
		createReadArc(rstg.fwE1, rstg.andMR);
		createReadArc(rstg.bwE1, rstg.andMR);
		createReadArc(rstg.orM0, rstg.andMF);
		createReadArc(rstg.fwE0, rstg.andMF);
		createReadArc(rstg.bwE0, rstg.andMF);
		for (VisualCounterflowRegister n: cfSet) {
			if (n instanceof VisualCounterflowRegister) {
				CounterflowRegisterSTG nstg = getCounterflowRegisterSTG((VisualCounterflowRegister)n);
				createReadArc(nstg.orM1, rstg.andMR);
				createReadArc(nstg.orM0, rstg.andMF);
			}
		}
	}

	public CounterflowRegisterSTG getCounterflowRegisterSTG(VisualCounterflowRegister register) {
		return counterflowRegisterMap.get(register);
	}

	public Set<VisualCounterflowRegister> getCounterflowRegisterPreset(VisualComponent component) {
		Set<VisualCounterflowRegister> result = new HashSet<VisualCounterflowRegister>();
		Set<VisualComponent> visited = new HashSet<VisualComponent>();
		Queue<VisualComponent> queue = new LinkedList<VisualComponent>();
		queue.add(component);
		while (!queue.isEmpty()) {
			VisualComponent currentComponent = queue.remove();
			if (visited.contains(currentComponent)) continue;
			visited.add(currentComponent);
			for (Node prevNode: sdfs.getPreset(currentComponent)) {
				if (prevNode instanceof VisualComponent) {
					VisualComponent prevComponent = (VisualComponent) prevNode;
					if (prevComponent instanceof VisualCounterflowRegister) {
						result.add((VisualCounterflowRegister)prevComponent);
					} else if ( !(prevComponent instanceof VisualSpreadtokenRegister) ) {
						queue.add(prevComponent);
					}
				}
			}
		}
		return result;
	}

	public Set<VisualCounterflowRegister> getCounterflowRegisterPostset(VisualComponent component) {
		Set<VisualCounterflowRegister> result = new HashSet<VisualCounterflowRegister>();
		Set<VisualComponent> visited = new HashSet<VisualComponent>();
		Queue<VisualComponent> queue = new LinkedList<VisualComponent>();
		queue.add(component);
		while (!queue.isEmpty()) {
			VisualComponent currentComponent = queue.remove();
			if (visited.contains(currentComponent)) continue;
			visited.add(currentComponent);
			for (Node succNode: sdfs.getPostset(currentComponent)) {
				if (succNode instanceof VisualComponent) {
					VisualComponent succComponent = (VisualComponent) succNode;
					if (succComponent instanceof VisualCounterflowRegister) {
						result.add((VisualCounterflowRegister)succComponent);
					} else if ( !(succComponent instanceof VisualSpreadtokenRegister) ) {
						queue.add(succComponent);
					}
				}
			}
		}
		return result;
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		if (highLevelNode instanceof VisualSpreadtokenLogic) {
			VisualSpreadtokenLogic logic = (VisualSpreadtokenLogic)highLevelNode;
			SpreadtokenLogicSTG lstg = getSpreadtokenLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualSpreadtokenRegister) {
			VisualSpreadtokenRegister register = (VisualSpreadtokenRegister)highLevelNode;
			SpreadtokenRegisterSTG rstg = getSpreadtokenRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowLogic) {
			VisualCounterflowLogic logic = (VisualCounterflowLogic)highLevelNode;
			CounterflowLogicSTG lstg = getCounterflowLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowRegister) {
			VisualCounterflowRegister register = (VisualCounterflowRegister)highLevelNode;
			CounterflowRegisterSTG rstg = getCounterflowRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		}
		return false;
	}

}
