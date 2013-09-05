package org.workcraft.plugins.sdfs.stg;

import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.sdfs.VisualControlRegister;
import org.workcraft.plugins.sdfs.VisualCounterflowLogic;
import org.workcraft.plugins.sdfs.VisualCounterflowRegister;
import org.workcraft.plugins.sdfs.VisualLogic;
import org.workcraft.plugins.sdfs.VisualPopRegister;
import org.workcraft.plugins.sdfs.VisualPushRegister;
import org.workcraft.plugins.sdfs.VisualRegister;
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
	private static final String nameFwE 	= "fwE_";
	private static final String nameBwE 	= "bwE_";
	private static final String nameM 		= "M_";
	private static final String nameOrM 	= "orM_";
	private static final String nameAndM	= "andM_";
	private static final String nameTrueM 	= "trueM_";
	private static final String nameFalseM	= "falseM_";
	private static final String name0	 	= "_0";
	private static final String name1 		= "_1";
	private static final String labelC 		= "C(";
	private static final String labelFwC	= "fwC(";
	private static final String labelBwC	= "bwC(";
	private static final String labelFwE	= "fwE(";
	private static final String labelBwE	= "bwE(";
	private static final String labelM 		= "M(";
	private static final String labelOrM	= "orM(";
	private static final String labelAndM 	= "andM(";
	private static final String labelTrueM	= "trueM(";
	private static final String labelFalseM	= "falseM(";
	private static final String label0 		= ")=0";
	private static final String label1 		= ")=1";
	private static final double xScaling = 6;
	private static final double yScaling = 6;

	private Map<VisualLogic, LogicSTG> logicMap = new HashMap<VisualLogic, LogicSTG>();
	private Map<VisualRegister, RegisterSTG> registerMap = new HashMap<VisualRegister, RegisterSTG>();
	private Map<VisualCounterflowLogic, CounterflowLogicSTG> counterflowLogicMap = new HashMap<VisualCounterflowLogic, CounterflowLogicSTG>();
	private Map<VisualCounterflowRegister, CounterflowRegisterSTG> counterflowRegisterMap = new HashMap<VisualCounterflowRegister, CounterflowRegisterSTG>();
	private Map<VisualControlRegister, BinaryRegisterSTG> controlRegisterMap = new HashMap<VisualControlRegister, BinaryRegisterSTG>();
	private Map<VisualPushRegister, BinaryRegisterSTG> pushRegisterMap = new HashMap<VisualPushRegister, BinaryRegisterSTG>();
	private Map<VisualPopRegister, BinaryRegisterSTG> popRegisterMap = new HashMap<VisualPopRegister, BinaryRegisterSTG>();
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

			for(VisualLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualLogic.class)) {
				LogicSTG lstg = generateLogicSTG(l);
				logicMap.put(l, lstg);
			}
			for(VisualRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualRegister.class)) {
				RegisterSTG rstg = generateRegisterSTG(r);
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

			for(VisualControlRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualControlRegister.class)) {
				BinaryRegisterSTG rstg = generateControlRegisterSTG(r);
				controlRegisterMap.put(r, rstg);
			}
			for(VisualPushRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualPushRegister.class)) {
				BinaryRegisterSTG rstg = generatePushRegisterSTG(r);
				pushRegisterMap.put(r, rstg);
			}
			for(VisualPopRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualPopRegister.class)) {
				BinaryRegisterSTG rstg = generatePopRegisterSTG(r);
				popRegisterMap.put(r, rstg);
			}

			for(VisualLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualLogic.class)) {
				connectLogicSTG(l);
			}
			for(VisualRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualRegister.class)) {
				connectRegisterSTG(r);
			}

			for(VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowLogic.class)) {
				connectCounterflowLogicSTG(l);
			}
			for(VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualCounterflowRegister.class)) {
				connectCounterflowRegisterSTG(r);
			}

			for(VisualControlRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualControlRegister.class)) {
				connectControlRegisterSTG(r);
			}
			for(VisualPushRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualPushRegister.class)) {
				connectPushRegisterSTG(r);
			}
			for(VisualPopRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualPopRegister.class)) {
				connectPopRegisterSTG(r);
			}
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private void createReadArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
		if (p != null && t != null) {
			stg.connect(p, t);
			stg.connect(t, p);
		}
	}

	private LogicSTG generateLogicSTG(VisualLogic l) throws InvalidConnectionException {
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
			if (n instanceof VisualLogic || n instanceof VisualRegister || n instanceof VisualPushRegister) {
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
		return new LogicSTG(C0, C1, CRs, CFs);
	}

	private void connectLogicSTG(VisualLogic l) throws InvalidConnectionException {
		LogicSTG lstg = getLogicSTG(l);
		for (VisualLogic n: sdfs.getPreset(l, VisualLogic.class)) {
			LogicSTG nstg = getLogicSTG(n);
			createReadArc(nstg.C1, lstg.CRs.get(n));
			createReadArc(nstg.C0, lstg.CFs.get(n));
		}
		for (VisualRegister n: sdfs.getPreset(l, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n));
			createReadArc(nstg.M0, lstg.CFs.get(n));
		}
		for (VisualPushRegister n: sdfs.getPreset(l, VisualPushRegister.class)) {
			BinaryRegisterSTG nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, lstg.CRs.get(n));
			createReadArc(nstg.tM0, lstg.CFs.get(n));
		}
	}

	public LogicSTG getLogicSTG(VisualLogic logic) {
		return logicMap.get(logic);
	}

	private RegisterSTG generateRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		String name = sdfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace M0 = stg.createPlace(nameM + name + name0);
		M0.setLabel(labelM + name + label0);
		M0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isMarked()) M0.setTokens(1);
		setPosition(M0, x + 2.0, y + 1.0);
		nodes.add(M0);

		VisualPlace M1 = stg.createPlace(nameM + name + name1);
		M1.setLabel(labelM + name + label1);
		M1.setLabelPositioning(Positioning.TOP);
		if (r.isMarked()) M1.setTokens(1);
		setPosition(M1, x + 2.0, y - 1.0);
		nodes.add(M1);

		VisualSignalTransition MR = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(M0, MR);
		stg.connect(MR, M1);
		setPosition(MR, x - 2.0, y + 1.0);
		nodes.add(MR);

		VisualSignalTransition MF = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(M1, MF);
		stg.connect(MF, M0);
		setPosition(MF, x - 2.0, y - 1.0);
		nodes.add(MF);

		VisualSignalTransition fMR = null;
		if (sdfs.getRPreset(r, VisualPopRegister.class).size() != 0) {
			fMR = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
			stg.connect(M0, fMR);
			stg.connect(fMR, M1);
			setPosition(fMR, x - 2.0, y + 2.0);
			nodes.add(fMR);
		}
		VisualSignalTransition fMF = null;
		if (sdfs.getRPostset(r, VisualPushRegister.class).size() != 0) {
			fMF = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
			stg.connect(M1, fMF);
			stg.connect(fMF, M0);
			setPosition(fMF, x - 2.0, y - 2.0);
			nodes.add(fMF);
		}

		stg.groupCollection(nodes);
		return new RegisterSTG(M0, M1, MR, MF, fMR, fMF);
	}

	private void connectRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		RegisterSTG rstg = getRegisterSTG(r);
		// preset
		for (VisualLogic n: sdfs.getPreset(r, VisualLogic.class)) {
			LogicSTG nstg = getLogicSTG(n);
			createReadArc(nstg.C1, rstg.MR);
			createReadArc(nstg.C1, rstg.fMR);
			createReadArc(nstg.C0, rstg.MF);
			createReadArc(nstg.C0, rstg.fMF);
		}
		// R-preset
		for (VisualRegister n: sdfs.getRPreset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR);
			createReadArc(nstg.M1, rstg.fMR);
			createReadArc(nstg.M0, rstg.MF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		for (VisualCounterflowRegister n: sdfs.getRPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.MR);
			createReadArc(nstg.orM1, rstg.fMR);
			createReadArc(nstg.orM0, rstg.MF);
			createReadArc(nstg.orM0, rstg.fMF);
		}
		for (VisualPushRegister n: sdfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterSTG nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MR);
			createReadArc(nstg.tM1, rstg.fMR);
			createReadArc(nstg.tM0, rstg.MF);
			createReadArc(nstg.tM0, rstg.fMF);
		}
		for (VisualPopRegister n: sdfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterSTG nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MR);
			createReadArc(nstg.tM0, rstg.MF);
			createReadArc(nstg.tM0, rstg.fMF); // pop M0 in R-preset is read by both MF and fMF
			createReadArc(nstg.fM1, rstg.fMR);
			createReadArc(nstg.fM0, rstg.fMF);
			createReadArc(nstg.fM0, rstg.MF); // pop fM0 in R-preset is read by both MF and fMF
		}
		// R-postset
		for (VisualRegister n: sdfs.getRPostset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArc(nstg.M0, rstg.MR);
			createReadArc(nstg.M0, rstg.fMR);
		}
		for (VisualCounterflowRegister n: sdfs.getRPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.andM1, rstg.MF);
			createReadArc(nstg.andM1, rstg.fMF);
			createReadArc(nstg.andM0, rstg.MR);
			createReadArc(nstg.andM0, rstg.fMR);
		}
		for (VisualPushRegister n: sdfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterSTG nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MF);
			createReadArc(nstg.tM0, rstg.MR);
			createReadArc(nstg.tM0, rstg.fMR); // push M0 in R-postset is read by both MR and fMR
			createReadArc(nstg.fM1, rstg.fMF);
			createReadArc(nstg.fM0, rstg.MR);
			createReadArc(nstg.fM0, rstg.fMR); // push fM0 in R-postset is read by both MR and fMR
		}
		for (VisualPopRegister n: sdfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterSTG nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MF);
			createReadArc(nstg.tM1, rstg.fMF);
			createReadArc(nstg.tM0, rstg.MR);
			createReadArc(nstg.tM0, rstg.fMR);
		}
	}

	public RegisterSTG getRegisterSTG(VisualRegister register) {
		return registerMap.get(register);
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
		// preset
		for (VisualCounterflowLogic n: sdfs.getPreset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicSTG nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, lstg.fwCRs.get(n));
			createReadArc(nstg.fwC0, lstg.fwCFs.get(n));
		}
		for (VisualCounterflowRegister n: sdfs.getPreset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.fwCRs.get(n));
			createReadArc(nstg.orM0, lstg.fwCFs.get(n));
		}
		// postset
		for (VisualCounterflowLogic n: sdfs.getPostset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicSTG nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, lstg.bwCRs.get(n));
			createReadArc(nstg.bwC0, lstg.bwCFs.get(n));
		}
		for (VisualCounterflowRegister n: sdfs.getPostset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.bwCRs.get(n));
			createReadArc(nstg.orM0, lstg.bwCFs.get(n));
		}
	}

	public CounterflowLogicSTG getCounterflowLogicSTG(VisualCounterflowLogic logic) {
		return counterflowLogicMap.get(logic);
	}

	private CounterflowRegisterSTG generateCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		String name = sdfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x = xScaling * (transform.getTranslateX() + r.getX());
		double y = yScaling * (transform.getTranslateY() + r.getY());
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
		Set<VisualCounterflowRegister> cfSet = sdfs.getRPreset(r, VisualCounterflowRegister.class);
		cfSet.addAll(sdfs.getRPostset(r, VisualCounterflowRegister.class));
		// forward - enabling / disabling
		createReadArc(rstg.andM0, rstg.fwER);
		createReadArc(rstg.andM1, rstg.fwEF);
		for (VisualRegister n: sdfs.getPreset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.fwER);
			createReadArc(nstg.M0, rstg.fwEF);
		}
		for (VisualCounterflowLogic n: sdfs.getPreset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicSTG nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, rstg.fwER);
			createReadArc(nstg.fwC0, rstg.fwEF);
		}
		for (VisualCounterflowRegister n: sdfs.getPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.fwER);
			createReadArc(nstg.orM0, rstg.fwEF);
		}
		// backward - enabling / disabling
		createReadArc(rstg.andM0, rstg.bwER);
		createReadArc(rstg.andM1, rstg.bwEF);
		for (VisualRegister n: sdfs.getPostset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.bwER);
			createReadArc(nstg.M0, rstg.bwEF);
		}
		for (VisualCounterflowLogic n: sdfs.getPostset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicSTG nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, rstg.bwER);
			createReadArc(nstg.bwC0, rstg.bwEF);
		}
		for (VisualCounterflowRegister n: sdfs.getPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.bwER);
			createReadArc(nstg.orM0, rstg.bwEF);
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
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.andM0, rstg.orMRfw);
			createReadArc(nstg.andM0, rstg.orMRbw);
			createReadArc(nstg.andM1, rstg.orMFfw);
			createReadArc(nstg.andM1, rstg.orMFbw);
		}
		// AND - marking / unmarking
		createReadArc(rstg.orM1, rstg.andMR);
		createReadArc(rstg.fwE1, rstg.andMR);
		createReadArc(rstg.bwE1, rstg.andMR);
		createReadArc(rstg.orM0, rstg.andMF);
		createReadArc(rstg.fwE0, rstg.andMF);
		createReadArc(rstg.bwE0, rstg.andMF);
		for (VisualCounterflowRegister n: cfSet) {
			CounterflowRegisterSTG nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.andMR);
			createReadArc(nstg.orM0, rstg.andMF);
		}
	}

	public CounterflowRegisterSTG getCounterflowRegisterSTG(VisualCounterflowRegister register) {
		return counterflowRegisterMap.get(register);
	}

	private BinaryRegisterSTG generateControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		String name = sdfs.getName(r);
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace tM0 = stg.createPlace(nameTrueM + name + name0);
		tM0.setLabel(labelTrueM + name + label0);
		tM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isTrueMarked()) tM0.setTokens(1);
		setPosition(tM0, x + 2.0, y - 2.0);
		nodes.add(tM0);

		VisualPlace tM1 = stg.createPlace(nameTrueM + name + name1);
		tM1.setLabel(labelTrueM + name + label1);
		tM1.setLabelPositioning(Positioning.TOP);
		if (r.isTrueMarked()) tM1.setTokens(1);
		setPosition(tM1, x + 2.0, y - 4.0);
		nodes.add(tM1);

		VisualSignalTransition tMR = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(tM0, tMR);
		stg.connect(tMR, tM1);
		setPosition(tMR, x - 2.0, y - 2.0);
		nodes.add(tMR);

		VisualSignalTransition tMF = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(tM1, tMF);
		stg.connect(tMF, tM0);
		setPosition(tMF, x - 2.0, y - 4.0);
		nodes.add(tMF);

		VisualPlace fM0 = stg.createPlace(nameFalseM + name + name0);
		fM0.setLabel(labelFalseM + name + label0);
		fM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isFalseMarked()) fM0.setTokens(1);
		setPosition(fM0, x + 2.0, y + 4.0);
		nodes.add(fM0);

		VisualPlace fM1 = stg.createPlace(nameFalseM + name + name1);
		fM1.setLabel(labelFalseM + name + label1);
		fM1.setLabelPositioning(Positioning.TOP);
		if (r.isFalseMarked()) fM1.setTokens(1);
		setPosition(fM1, x + 2.0, y + 2.0);
		nodes.add(fM1);

		VisualSignalTransition fMR = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(fM0, fMR);
		stg.connect(fMR, fM1);
		setPosition(fMR, x - 2.0, y + 4.0);
		nodes.add(fMR);

		VisualSignalTransition fMF = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(fM1, fMF);
		stg.connect(fMF, fM0);
		setPosition(fMF, x - 2.0, y + 2.0);
		nodes.add(fMF);

		stg.groupCollection(nodes);
		return new BinaryRegisterSTG(tM0, tM1, tMR, tMF, fM0, fM1, fMR, fMF);
	}

	private void connectControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		BinaryRegisterSTG rstg = getControlRegisterSTG(r);
		createReadArc(rstg.tM0, rstg.fMR);
		createReadArc(rstg.fM0, rstg.tMR);
		// R-preset
		for (VisualControlRegister n: sdfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterSTG nstg = getControlRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMR);
			createReadArc(nstg.tM0, rstg.tMF);
			createReadArc(nstg.fM1, rstg.fMR);
			createReadArc(nstg.fM0, rstg.fMF);
		}
		// R-postset
		for (VisualControlRegister n: sdfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterSTG nstg = getControlRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF);
			createReadArc(nstg.tM0, rstg.tMR);
			createReadArc(nstg.tM0, rstg.fMR); // R-postset control tM0 is read by both tMR and fMR
			createReadArc(nstg.fM1, rstg.fMF);
			createReadArc(nstg.fM0, rstg.fMR);
			createReadArc(nstg.fM0, rstg.tMR); // R-postset control fM0 is read by both fMR and tMR
		}
		for (VisualPushRegister n: sdfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterSTG nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF);
			createReadArc(nstg.tM0, rstg.tMR);
			createReadArc(nstg.tM0, rstg.fMR); // R-postset push M0 is read by both tMR and fMR
			createReadArc(nstg.fM1, rstg.fMF);
			createReadArc(nstg.fM0, rstg.fMR);
			createReadArc(nstg.fM0, rstg.tMR); // R-postset push fM0 is read by both fMR and tMR
		}
		for (VisualPopRegister n: sdfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterSTG nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF);
			createReadArc(nstg.tM0, rstg.tMR);
			createReadArc(nstg.tM0, rstg.fMR); // R-postset pop M0 is read by both tMR and fMR
			createReadArc(nstg.fM1, rstg.fMF);
			createReadArc(nstg.fM0, rstg.fMR);
			createReadArc(nstg.fM0, rstg.tMR); // R-postset pop fM0 is read by both fMR and tMR
		}
	}

	public BinaryRegisterSTG getControlRegisterSTG(VisualControlRegister register) {
		return controlRegisterMap.get(register);
	}

	private BinaryRegisterSTG generatePushRegisterSTG(VisualPushRegister r) throws InvalidConnectionException {
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		String name = sdfs.getName(r);
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace tM0 = stg.createPlace(nameTrueM + name + name0);
		tM0.setLabel(labelTrueM + name + label0);
		tM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isTrueMarked()) tM0.setTokens(1);
		setPosition(tM0, x + 2.0, y - 2.0);
		nodes.add(tM0);

		VisualPlace tM1 = stg.createPlace(nameTrueM + name + name1);
		tM1.setLabel(labelTrueM + name + label1);
		tM1.setLabelPositioning(Positioning.TOP);
		if (r.isTrueMarked()) tM1.setTokens(1);
		setPosition(tM1, x + 2.0, y - 4.0);
		nodes.add(tM1);

		VisualSignalTransition tMR = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(tM0, tMR);
		stg.connect(tMR, tM1);
		setPosition(tMR, x - 2.0, y - 2.0);
		nodes.add(tMR);

		VisualSignalTransition tMF = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(tM1, tMF);
		stg.connect(tMF, tM0);
		setPosition(tMF, x - 2.0, y - 4.0);
		nodes.add(tMF);

		VisualPlace fM0 = stg.createPlace(nameFalseM + name + name0);
		fM0.setLabel(labelFalseM + name + label0);
		fM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isFalseMarked()) fM0.setTokens(1);
		setPosition(fM0, x + 2.0, y + 4.0);
		nodes.add(fM0);

		VisualPlace fM1 = stg.createPlace(nameFalseM + name + name1);
		fM1.setLabel(labelFalseM + name + label1);
		fM1.setLabelPositioning(Positioning.TOP);
		if (r.isFalseMarked()) fM1.setTokens(1);
		setPosition(fM1, x + 2.0, y + 2.0);
		nodes.add(fM1);

		VisualSignalTransition fMR = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(fM0, fMR);
		stg.connect(fMR, fM1);
		setPosition(fMR, x - 2.0, y + 4.0);
		nodes.add(fMR);

		VisualSignalTransition fMF = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(fM1, fMF);
		stg.connect(fMF, fM0);
		setPosition(fMF, x - 2.0, y + 2.0);
		nodes.add(fMF);

		stg.groupCollection(nodes);
		return new BinaryRegisterSTG(tM0, tM1, tMR, tMF, fM0, fM1, fMR, fMF);
	}

	private void connectPushRegisterSTG(VisualPushRegister r) throws InvalidConnectionException {
		BinaryRegisterSTG rstg = getPushRegisterSTG(r);
		createReadArc(rstg.tM0, rstg.fMR);
		createReadArc(rstg.fM0, rstg.tMR);
		// preset
		for (VisualLogic n: sdfs.getPreset(r, VisualLogic.class)) {
			LogicSTG nstg = getLogicSTG(n);
			createReadArc(nstg.C1, rstg.tMR);
			createReadArc(nstg.C1, rstg.fMR);
			createReadArc(nstg.C0, rstg.tMF);
			createReadArc(nstg.C0, rstg.fMF);
		}
		// R-preset
		for (VisualRegister n: sdfs.getRPreset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMR);
			createReadArc(nstg.M1, rstg.fMR);
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		for (VisualControlRegister n: sdfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterSTG nstg = getControlRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMR);
			createReadArc(nstg.fM1, rstg.fMR);
			createReadArc(nstg.tM0, rstg.tMF);
			createReadArc(nstg.fM0, rstg.fMF);
		}
		// R-postset
		for (VisualRegister n: sdfs.getRPostset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF); // register M1 in R-postset is read only by tMF
			createReadArc(nstg.M0, rstg.tMR); // register M0 in R-postset is read only by tMR
		}
		for (VisualPopRegister n: sdfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterSTG nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF); // pop tM1 in R-postset is read only by tMF
			createReadArc(nstg.tM0, rstg.tMR); // pop tM0 in R-postset is read only by tMR
		}
	}

	public BinaryRegisterSTG getPushRegisterSTG(VisualPushRegister register) {
		return pushRegisterMap.get(register);
	}


	private BinaryRegisterSTG generatePopRegisterSTG(VisualPopRegister r) throws InvalidConnectionException {
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		String name = sdfs.getName(r);
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace tM0 = stg.createPlace(nameTrueM + name + name0);
		tM0.setLabel(labelTrueM + name + label0);
		tM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isTrueMarked()) tM0.setTokens(1);
		setPosition(tM0, x + 2.0, y - 2.0);
		nodes.add(tM0);

		VisualPlace tM1 = stg.createPlace(nameTrueM + name + name1);
		tM1.setLabel(labelTrueM + name + label1);
		tM1.setLabelPositioning(Positioning.TOP);
		if (r.isTrueMarked()) tM1.setTokens(1);
		setPosition(tM1, x + 2.0, y - 4.0);
		nodes.add(tM1);

		VisualSignalTransition tMR = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(tM0, tMR);
		stg.connect(tMR, tM1);
		setPosition(tMR, x - 2.0, y - 2.0);
		nodes.add(tMR);

		VisualSignalTransition tMF = stg.createSignalTransition(nameTrueM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(tM1, tMF);
		stg.connect(tMF, tM0);
		setPosition(tMF, x - 2.0, y - 4.0);
		nodes.add(tMF);

		VisualPlace fM0 = stg.createPlace(nameFalseM + name + name0);
		fM0.setLabel(labelFalseM + name + label0);
		fM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isFalseMarked()) fM0.setTokens(1);
		setPosition(fM0, x + 2.0, y + 4.0);
		nodes.add(fM0);

		VisualPlace fM1 = stg.createPlace(nameFalseM + name + name1);
		fM1.setLabel(labelFalseM + name + label1);
		fM1.setLabelPositioning(Positioning.TOP);
		if (r.isFalseMarked()) fM1.setTokens(1);
		setPosition(fM1, x + 2.0, y + 2.0);
		nodes.add(fM1);

		VisualSignalTransition fMR = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(fM0, fMR);
		stg.connect(fMR, fM1);
		setPosition(fMR, x - 2.0, y + 4.0);
		nodes.add(fMR);

		VisualSignalTransition fMF = stg.createSignalTransition(nameFalseM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(fM1, fMF);
		stg.connect(fMF, fM0);
		setPosition(fMF, x - 2.0, y + 2.0);
		nodes.add(fMF);

		stg.groupCollection(nodes);
		return new BinaryRegisterSTG(tM0, tM1, tMR, tMF, fM0, fM1, fMR, fMF);
	}

	private void connectPopRegisterSTG(VisualPopRegister r) throws InvalidConnectionException {
		BinaryRegisterSTG rstg = getPopRegisterSTG(r);
		createReadArc(rstg.tM0, rstg.fMR);
		createReadArc(rstg.fM0, rstg.tMR);
		// preset
		for (VisualLogic n: sdfs.getPreset(r, VisualLogic.class)) {
			LogicSTG nstg = getLogicSTG(n);
			createReadArc(nstg.C1, rstg.tMR);
			createReadArc(nstg.C0, rstg.tMF);
		}
		// R-preset
		for (VisualRegister n: sdfs.getRPreset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMR);
			createReadArc(nstg.M0, rstg.tMF);
		}
		for (VisualControlRegister n: sdfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterSTG nstg = getControlRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMR);
			createReadArc(nstg.tM0, rstg.tMF);
			createReadArc(nstg.fM1, rstg.fMR);
			createReadArc(nstg.fM0, rstg.fMF);
		}
		// R-postset
		for (VisualRegister n: sdfs.getRPostset(r, VisualRegister.class)) {
			RegisterSTG nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArc(nstg.M0, rstg.tMR);
			createReadArc(nstg.M0, rstg.fMR);
		}
	}

	public BinaryRegisterSTG getPopRegisterSTG(VisualPopRegister register) {
		return popRegisterMap.get(register);
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		if (highLevelNode instanceof VisualLogic) {
			VisualLogic logic = (VisualLogic)highLevelNode;
			LogicSTG lstg = getLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualRegister) {
			VisualRegister register = (VisualRegister)highLevelNode;
			RegisterSTG rstg = getRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowLogic) {
			VisualCounterflowLogic logic = (VisualCounterflowLogic)highLevelNode;
			CounterflowLogicSTG lstg = getCounterflowLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowRegister) {
			VisualCounterflowRegister register = (VisualCounterflowRegister)highLevelNode;
			CounterflowRegisterSTG rstg = getCounterflowRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualControlRegister) {
			VisualControlRegister register = (VisualControlRegister)highLevelNode;
			BinaryRegisterSTG rstg = getControlRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualPushRegister) {
			VisualPushRegister register = (VisualPushRegister)highLevelNode;
			BinaryRegisterSTG rstg = getPushRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualPopRegister) {
			VisualPopRegister register = (VisualPopRegister)highLevelNode;
			BinaryRegisterSTG rstg = getPopRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		}
		return false;
	}

}
