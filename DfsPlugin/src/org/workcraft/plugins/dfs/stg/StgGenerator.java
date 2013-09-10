package org.workcraft.plugins.dfs.stg;

import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dfs.VisualBinaryRegister;
import org.workcraft.plugins.dfs.VisualControlConnection;
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.ControlRegister.SynchronisationType;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public class StgGenerator {
	private static final String nameC 		= "C_";
	private static final String nameFwC 		= "fwC_";
	private static final String nameBwC 		= "bwC_";
	private static final String nameM 		= "M_";
	private static final String nameOrM 		= "orM_";
	private static final String nameAndM		= "andM_";
	private static final String nameTrueM 	= "trueM_";
	private static final String nameFalseM	= "falseM_";
	private static final String name0	 		= "_0";
	private static final String name1 		= "_1";
	private static final String labelC 		= "C(";
	private static final String labelFwC		= "fwC(";
	private static final String labelBwC		= "bwC(";
	private static final String labelM 		= "M(";
	private static final String labelOrM		= "orM(";
	private static final String labelAndM 	= "andM(";
	private static final String labelTrueM	= "trueM(";
	private static final String labelFalseM	= "falseM(";
	private static final String label0 		= ")=0";
	private static final String label1 		= ")=1";
	private static final double xScaling = 6;
	private static final double yScaling = 6;

	private Map<VisualLogic, LogicStg> logicMap = new HashMap<VisualLogic, LogicStg>();
	private Map<VisualRegister, RegisterStg> registerMap = new HashMap<VisualRegister, RegisterStg>();
	private Map<VisualCounterflowLogic, CounterflowLogicStg> counterflowLogicMap = new HashMap<VisualCounterflowLogic, CounterflowLogicStg>();
	private Map<VisualCounterflowRegister, CounterflowRegisterStg> counterflowRegisterMap = new HashMap<VisualCounterflowRegister, CounterflowRegisterStg>();
	private Map<VisualControlRegister, BinaryRegisterStg> controlRegisterMap = new HashMap<VisualControlRegister, BinaryRegisterStg>();
	private Map<VisualPushRegister, BinaryRegisterStg> pushRegisterMap = new HashMap<VisualPushRegister, BinaryRegisterStg>();
	private Map<VisualPopRegister, BinaryRegisterStg> popRegisterMap = new HashMap<VisualPopRegister, BinaryRegisterStg>();
	private final VisualDfs dfs;
	private final VisualSTG stg;

	public VisualSTG getSTG() {
		return stg;
	}

	static void setPosition(Movable node, double x, double y) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
	}

	public StgGenerator(VisualDfs dfs) {
		this.dfs = dfs;
		try {
			this.stg = new VisualSTG(new STG());

			for(VisualLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualLogic.class)) {
				LogicStg lstg = generateLogicSTG(l);
				logicMap.put(l, lstg);
			}
			for(VisualRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualRegister.class)) {
				RegisterStg rstg = generateRegisterSTG(r);
				registerMap.put(r, rstg);
			}

			for(VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowLogic.class)) {
				CounterflowLogicStg lstg = generateCounterflowLogicSTG(l);
				counterflowLogicMap.put(l, lstg);
			}
			for(VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowRegister.class)) {
				CounterflowRegisterStg rstg = generateCounterflowRegisterSTG(r);
				counterflowRegisterMap.put(r, rstg);
			}

			for(VisualControlRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualControlRegister.class)) {
				BinaryRegisterStg rstg = generateControlRegisterSTG(r);
				controlRegisterMap.put(r, rstg);
			}
			for(VisualPushRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualPushRegister.class)) {
				BinaryRegisterStg rstg = generatePushRegisterSTG(r);
				pushRegisterMap.put(r, rstg);
			}
			for(VisualPopRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualPopRegister.class)) {
				BinaryRegisterStg rstg = generatePopRegisterSTG(r);
				popRegisterMap.put(r, rstg);
			}

			for(VisualLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualLogic.class)) {
				connectLogicSTG(l);
			}
			for(VisualRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualRegister.class)) {
				connectRegisterSTG(r);
			}

			for(VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowLogic.class)) {
				connectCounterflowLogicSTG(l);
			}
			for(VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowRegister.class)) {
				connectCounterflowRegisterSTG(r);
			}

			for(VisualControlRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualControlRegister.class)) {
				connectControlRegisterSTG(r);
			}
			for(VisualPushRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualPushRegister.class)) {
				connectPushRegisterSTG(r);
			}
			for(VisualPopRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualPopRegister.class)) {
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

	private void createReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts) throws InvalidConnectionException {
		for (VisualSignalTransition t : new HashSet<VisualSignalTransition>(ts)) {
			createReadArc(p, t);
		}
	}

	private LogicStg generateLogicSTG(VisualLogic l) throws InvalidConnectionException {
		String name = dfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		double x =	xScaling * (transform.getTranslateX() + l.getX());
		double y =	yScaling * (transform.getTranslateY() + l.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		VisualPlace C0 = stg.createPlace(nameC + name + name0);
		C0.setLabel(labelC + name + label0);
		C0.setLabelPositioning(Positioning.BOTTOM);
		if (!l.isComputed()) C0.setTokens(1);
		setPosition(C0, x + 2.0, y + 1.0);
		nodes.add(C0);

		VisualPlace C1 = stg.createPlace(nameC + name + name1);
		C1.setLabel(labelC + name + label1);
		C1.setLabelPositioning(Positioning.TOP);
		if (l.isComputed()) C1.setTokens(1);
		setPosition(C1, x + 2.0, y - 1.0);
		nodes.add(C1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getPreset(l, VisualLogic.class));
		preset.addAll(dfs.getPreset(l, VisualRegister.class));
		preset.addAll(dfs.getPreset(l, VisualControlRegister.class));
		preset.addAll(dfs.getPreset(l, VisualPushRegister.class));
		preset.addAll(dfs.getPreset(l, VisualPopRegister.class));
		if (preset.size() == 0) preset.add(l);
		Map<Node, VisualSignalTransition> CRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> CFs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition CR = null;
		VisualSignalTransition CF = null;
		double dy = 0.0;
		for (Node n: preset) {
			if (CR == null || l.isEarlyEvaluation()) {
				CR = stg.createSignalTransition(nameC + name, type, SignalTransition.Direction.PLUS);
				stg.connect(C0, CR);
				stg.connect(CR, C1);
				setPosition(CR, x - 2.0, y + 1.0 + dy);
				nodes.add(CR);
			}
			CRs.put(n, CR);
			if (CF == null) {
				CF = stg.createSignalTransition(nameC + name, type, SignalTransition.Direction.MINUS);
				stg.connect(C1, CF);
				stg.connect(CF, C0);
				setPosition(CF, x - 2.0, y - 1.0 - dy);
				nodes.add(CF);
			}
			CFs.put(n, CF);
			dy += 1.0;
		}

		stg.groupCollection(nodes);
		return new LogicStg(C0, C1, CRs, CFs);
	}

	private void connectLogicSTG(VisualLogic l) throws InvalidConnectionException {
		LogicStg lstg = getLogicSTG(l);
		for (VisualLogic n: dfs.getPreset(l, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArc(nstg.C1, lstg.CRs.get(n));
			createReadArc(nstg.C0, lstg.CFs.get(n));
		}
		for (VisualRegister n: dfs.getPreset(l, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n));
			createReadArc(nstg.M0, lstg.CFs.get(n));
		}
		for (VisualControlRegister n: dfs.getPreset(l, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n));
			createReadArc(nstg.M0, lstg.CFs.get(n));
		}
		for (VisualPushRegister n: dfs.getPreset(l, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, lstg.CRs.get(n));
			createReadArc(nstg.tM0, lstg.CFs.get(n));
		}
		for (VisualPopRegister n: dfs.getPreset(l, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n));
			createReadArc(nstg.M0, lstg.CFs.get(n));
		}
	}

	public LogicStg getLogicSTG(VisualLogic logic) {
		return logicMap.get(logic);
	}

	private RegisterStg generateRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		String name = dfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;
		if (dfs.getPreset(r).size() == 0) {
			type = SignalTransition.Type.INPUT;
		} else if (dfs.getPostset(r).size() == 0) {
			type = SignalTransition.Type.OUTPUT;
		}

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

		VisualSignalTransition MR = stg.createSignalTransition(nameM + name, type, SignalTransition.Direction.PLUS);
		stg.connect(M0, MR);
		stg.connect(MR, M1);
		setPosition(MR, x - 2.0, y + 1.0);
		nodes.add(MR);

		VisualSignalTransition MF = stg.createSignalTransition(nameM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(M1, MF);
		stg.connect(MF, M0);
		setPosition(MF, x - 2.0, y - 1.0);
		nodes.add(MF);

		stg.groupCollection(nodes);
		return new RegisterStg(M0, M1, MR, MF);
	}

	private void connectRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		RegisterStg rstg = getRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArc(nstg.C1, rstg.MR);
			createReadArc(nstg.C0, rstg.MF);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR);
			createReadArc(nstg.M0, rstg.MF);
		}
		for (VisualCounterflowRegister n: dfs.getRPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.MR);
			createReadArc(nstg.orM0, rstg.MF);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR);
			createReadArc(nstg.M0, rstg.MF);
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MR);
			createReadArc(nstg.tM0, rstg.MF);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR);
			createReadArc(nstg.M0, rstg.MF);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF);
			createReadArc(nstg.M0, rstg.MR);
		}
		for (VisualCounterflowRegister n: dfs.getRPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.andM1, rstg.MF);
			createReadArc(nstg.andM0, rstg.MR);
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF);
			createReadArc(nstg.M0, rstg.MR);
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF);
			createReadArc(nstg.M0, rstg.MR);
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MF);
			createReadArc(nstg.tM0, rstg.MR);
		}
	}

	public RegisterStg getRegisterSTG(VisualRegister register) {
		return registerMap.get(register);
	}

	private CounterflowLogicStg generateCounterflowLogicSTG(VisualCounterflowLogic l) throws InvalidConnectionException {
		String name = dfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		double x =	xScaling * (transform.getTranslateX() + l.getX());
		double y = yScaling * (transform.getTranslateY() + l.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;

		VisualPlace fwC0 = stg.createPlace(nameFwC + name + name0);
		fwC0.setLabel(labelFwC + name + label0);
		fwC0.setLabelPositioning(Positioning.BOTTOM);
		if (!l.isForwardComputed()) fwC0.setTokens(1);
		setPosition(fwC0, x + 2.0, y - 2.0);
		nodes.add(fwC0);

		VisualPlace fwC1 = stg.createPlace(nameFwC + name + name1);
		fwC1.setLabel(labelFwC + name + label1);
		fwC1.setLabelPositioning(Positioning.TOP);
		if (l.isForwardComputed()) fwC1.setTokens(1);
		setPosition(fwC1, x + 2.0, y - 4.0);
		nodes.add(fwC1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getPreset(l, VisualCounterflowLogic.class));
		preset.addAll(dfs.getPreset(l, VisualCounterflowRegister.class));
		if (preset.size() == 0) preset.add(l);
		Map<Node, VisualSignalTransition> fwCRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> fwCFs = new HashMap<Node, VisualSignalTransition>();
		{
			VisualSignalTransition fwCR = null;
			VisualSignalTransition fwCF = null;
			double dy = 0.0;
			for (Node n: preset) {
				if (fwCR == null || l.isForwardEarlyEvaluation()) {
					fwCR = stg.createSignalTransition(nameFwC + name, type, SignalTransition.Direction.PLUS);
					stg.connect(fwC0, fwCR);
					stg.connect(fwCR, fwC1);
					setPosition(fwCR, x - 2.0, y - 2.0 + dy);
					nodes.add(fwCR);
				}
				fwCRs.put(n, fwCR);
				if (fwCF == null) {
					fwCF = stg.createSignalTransition(nameFwC + name, type, SignalTransition.Direction.MINUS);
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
		if (!l.isBackwardComputed()) bwC0.setTokens(1);
		setPosition(bwC0, x + 2.0, y + 4.0);
		nodes.add(bwC0);

		VisualPlace bwC1 = stg.createPlace(nameBwC + name + name1);
		bwC1.setLabel(labelBwC + name + label1);
		bwC1.setLabelPositioning(Positioning.TOP);
		if (l.isBackwardComputed()) bwC1.setTokens(1);
		setPosition(bwC1, x + 2.0, y + 2.0);
		nodes.add(bwC1);

		Set<Node> postset = new HashSet<Node>();
		postset.addAll(dfs.getPostset(l, VisualCounterflowLogic.class));
		postset.addAll(dfs.getPostset(l, VisualCounterflowRegister.class));
		if (postset.size() == 0) postset.add(l);
		Map<Node, VisualSignalTransition> bwCRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> bwCFs = new HashMap<Node, VisualSignalTransition>();
		{
			VisualSignalTransition bwCR = null;
			VisualSignalTransition bwCF = null;
			double dy = 0.0;
			for (Node n: postset) {
				if (bwCR == null || l.isBackwardEarlyEvaluation()) {
					bwCR = stg.createSignalTransition(nameBwC + name, type, SignalTransition.Direction.PLUS);
					stg.connect(bwC0, bwCR);
					stg.connect(bwCR, bwC1);
					setPosition(bwCR, x - 2.0, y + 4.0 + dy);
					nodes.add(bwCR);
				}
				bwCRs.put(n, bwCR);
				if (bwCF == null) {
					bwCF = stg.createSignalTransition(nameBwC + name, type, SignalTransition.Direction.MINUS);
					stg.connect(bwC1, bwCF);
					stg.connect(bwCF, bwC0);
					setPosition(bwCF, x - 2.0, y + 2.0 - dy);
					nodes.add(bwCF);
				}
				bwCFs.put(n, bwCF);
				dy += 1.0;
			}
		}

		stg.groupCollection(nodes);
		return new CounterflowLogicStg(fwC0, fwC1, fwCRs, fwCFs, bwC0, bwC1, bwCRs, bwCFs);
	}

	private void connectCounterflowLogicSTG(VisualCounterflowLogic l) throws InvalidConnectionException {
		CounterflowLogicStg lstg = getCounterflowLogicSTG(l);
		// preset
		for (VisualCounterflowLogic n: dfs.getPreset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, lstg.fwCRs.get(n));
			createReadArc(nstg.fwC0, lstg.fwCFs.get(n));
		}
		for (VisualCounterflowRegister n: dfs.getPreset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.fwCRs.get(n));
			createReadArc(nstg.orM0, lstg.fwCFs.get(n));
		}
		// postset
		for (VisualCounterflowLogic n: dfs.getPostset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, lstg.bwCRs.get(n));
			createReadArc(nstg.bwC0, lstg.bwCFs.get(n));
		}
		for (VisualCounterflowRegister n: dfs.getPostset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.bwCRs.get(n));
			createReadArc(nstg.orM0, lstg.bwCFs.get(n));
		}
	}

	public CounterflowLogicStg getCounterflowLogicSTG(VisualCounterflowLogic logic) {
		return counterflowLogicMap.get(logic);
	}

	private CounterflowRegisterStg generateCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		String name = dfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x = xScaling * (transform.getTranslateX() + r.getX());
		double y = yScaling * (transform.getTranslateY() + r.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;
		if (dfs.getPreset(r).size() == 0 || dfs.getPostset(r).size() == 0) {
			type = SignalTransition.Type.INPUT;
		}

		VisualPlace orM0 = stg.createPlace(nameOrM + name + name0);
		orM0.setLabel(labelOrM + name + label0);
		orM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isOrMarked()) orM0.setTokens(1);
		setPosition(orM0, x + 2.0, y - 2.0);
		nodes.add(orM0);

		VisualPlace orM1 = stg.createPlace(nameOrM + name + name1);
		orM1.setLabel(labelOrM + name + label1);
		orM1.setLabelPositioning(Positioning.TOP);
		if (r.isOrMarked()) orM1.setTokens(	1);
		setPosition(orM1, x + 2.0, y - 4.0);
		nodes.add(orM1);

		VisualSignalTransition orMRfw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.PLUS);
		stg.connect(orM0, orMRfw);
		stg.connect(orMRfw, orM1);
		setPosition(orMRfw, x - 2.0, y - 2.5);
		nodes.add(orMRfw);

		VisualSignalTransition orMRbw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.PLUS);
		stg.connect(orM0, orMRbw);
		stg.connect(orMRbw, orM1);
		setPosition(orMRbw, x - 2.0, y - 1.5);
		nodes.add(orMRbw);

		VisualSignalTransition orMFfw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(orM1, orMFfw);
		stg.connect(orMFfw, orM0);
		setPosition(orMFfw, x - 2.0, y - 4.5);
		nodes.add(orMFfw);

		VisualSignalTransition orMFbw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(orM1, orMFbw);
		stg.connect(orMFbw, orM0);
		setPosition(orMFbw, x - 2.0, y - 3.5);
		nodes.add(orMFbw);

		VisualPlace andM0 = stg.createPlace(nameAndM + name + name0);
		andM0.setLabel(labelAndM + name + label0);
		andM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isAndMarked()) andM0.setTokens(1);
		setPosition(andM0, x + 2.0, y + 4.0);
		nodes.add(andM0);

		VisualPlace andM1 = stg.createPlace(nameAndM + name + name1);
		andM1.setLabel(labelAndM + name + label1);
		andM1.setLabelPositioning(Positioning.TOP);
		if (r.isAndMarked()) andM1.setTokens(1);
		setPosition(andM1, x + 2.0, y + 2.0);
		nodes.add(andM1);

		VisualSignalTransition andMR = stg.createSignalTransition(nameAndM + name, type, SignalTransition.Direction.PLUS);
		stg.connect(andM0, andMR);
		stg.connect(andMR, andM1);
		setPosition(andMR, x - 2.0, y + 4.0);
		nodes.add(andMR);

		VisualSignalTransition andMF = stg.createSignalTransition(nameAndM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(andM1, andMF);
		stg.connect(andMF, andM0);
		setPosition(andMF, x - 2.0, y + 2.0);
		nodes.add(andMF);

		stg.groupCollection(nodes);
		return new CounterflowRegisterStg(orM0, orM1, orMRfw, orMRbw, orMFfw, orMFbw, andM0, andM1, andMR, andMF);
	}

	private void connectCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		CounterflowRegisterStg rstg = getCounterflowRegisterSTG(r);

        for (VisualRegister n: dfs.getPreset(r, VisualRegister.class)) {
            RegisterStg nstg = getRegisterSTG(n);
            createReadArc(nstg.M1, rstg.orMRfw);
            createReadArc(nstg.M0, rstg.orMFfw);
        }
        for (VisualRegister n: dfs.getPostset(r, VisualRegister.class)) {
            RegisterStg nstg = getRegisterSTG(n);
            createReadArc(nstg.M1, rstg.orMRbw);
            createReadArc(nstg.M0, rstg.orMFbw);
        }

        for (VisualCounterflowLogic n: dfs.getPreset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, rstg.orMRfw);
			createReadArc(nstg.fwC0, rstg.orMFfw);
			createReadArc(nstg.fwC1, rstg.andMR);
			createReadArc(nstg.fwC0, rstg.andMF);
		}
		for (VisualCounterflowLogic n: dfs.getPostset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, rstg.orMRbw);
			createReadArc(nstg.bwC0, rstg.orMFbw);
			createReadArc(nstg.bwC1, rstg.andMR);
			createReadArc(nstg.bwC0, rstg.andMF);
		}

		for (VisualCounterflowRegister n: dfs.getPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.orMRfw);
			createReadArc(nstg.orM0, rstg.orMFfw);
		}
		for (VisualCounterflowRegister n: dfs.getPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.orMRbw);
			createReadArc(nstg.orM0, rstg.orMFbw);
		}

		Set<VisualCounterflowRegister> rSet = new HashSet<VisualCounterflowRegister>();
		rSet.add(r);
		rSet.addAll(dfs.getRPreset(r, VisualCounterflowRegister.class));
		rSet.addAll(dfs.getRPostset(r, VisualCounterflowRegister.class));
		for (VisualCounterflowRegister n: rSet) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.andMR);
			createReadArc(nstg.orM0, rstg.andMF);
			createReadArc(nstg.andM1, rstg.orMFfw);
			createReadArc(nstg.andM1, rstg.orMFbw);
			createReadArc(nstg.andM0, rstg.orMRfw);
			createReadArc(nstg.andM0, rstg.orMRbw);
		}
	}

	public CounterflowRegisterStg getCounterflowRegisterSTG(VisualCounterflowRegister register) {
		return counterflowRegisterMap.get(register);
	}

	private BinaryRegisterStg generateBinaryRegisterSTG(VisualBinaryRegister r,
			boolean andSync, boolean orSync) throws InvalidConnectionException {
		Collection<Node> nodes = new LinkedList<Node>();
		String name = dfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		double x =	xScaling * (transform.getTranslateX() + r.getX());
		double y =	yScaling * (transform.getTranslateY() + r.getY());
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;
		if (dfs.getPreset(r, VisualControlRegister.class).size() == 0) {
			type = SignalTransition.Type.INPUT;
		} else if (dfs.getPostset(r).size() == 0) {
			type = SignalTransition.Type.OUTPUT;
		}

		VisualPlace M0 = stg.createPlace(nameM + name + name0);
		M0.setLabel(labelM + name + label0);
		M0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isTrueMarked() && !r.isFalseMarked()) M0.setTokens(1);
		setPosition(M0, x - 4.0, y + 1.0);
		nodes.add(M0);

		VisualPlace M1 = stg.createPlace(nameM + name + name1);
		M1.setLabel(labelM + name + label1);
		M1.setLabelPositioning(Positioning.TOP);
		if (r.isTrueMarked() || r.isFalseMarked()) M1.setTokens(1);
		setPosition(M1, x - 4.0, y - 1.0);
		nodes.add(M1);

		VisualPlace tM0 = stg.createPlace(nameTrueM + name + name0);
		tM0.setLabel(labelTrueM + name + label0);
		tM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isTrueMarked()) tM0.setTokens(1);
		setPosition(tM0, x + 4.0, y - 2.0);
		nodes.add(tM0);

		VisualPlace tM1 = stg.createPlace(nameTrueM + name + name1);
		tM1.setLabel(labelTrueM + name + label1);
		tM1.setLabelPositioning(Positioning.TOP);
		if (r.isTrueMarked()) tM1.setTokens(1);
		setPosition(tM1, x + 4.0, y - 4.0);
		nodes.add(tM1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getRPreset(r, VisualControlRegister.class));
		if (preset.size() == 0) preset.add(r);

		Map<Node, VisualSignalTransition> tMRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition tMR = null;
		double dy = 0.0;
		for (Node n: preset) {
			if (tMR == null || orSync) {
				tMR = stg.createSignalTransition(nameTrueM + name, type, SignalTransition.Direction.PLUS);
				stg.connect(tM0, tMR);
				stg.connect(tMR, tM1);
				stg.connect(M0, tMR);
				stg.connect(tMR, M1);
				setPosition(tMR, x, y - 2.0 + dy);
				nodes.add(tMR);
			}
			tMRs.put(n, tMR);
			dy += 1.0;
		}
		VisualSignalTransition tMF = stg.createSignalTransition(nameTrueM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(tM1, tMF);
		stg.connect(tMF, tM0);
		stg.connect(M1, tMF);
		stg.connect(tMF, M0);
		setPosition(tMF, x, y - 4.0 - dy);
		nodes.add(tMF);

		VisualPlace fM0 = stg.createPlace(nameFalseM + name + name0);
		fM0.setLabel(labelFalseM + name + label0);
		fM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isFalseMarked()) fM0.setTokens(1);
		setPosition(fM0, x + 4.0, y + 4.0);
		nodes.add(fM0);

		VisualPlace fM1 = stg.createPlace(nameFalseM + name + name1);
		fM1.setLabel(labelFalseM + name + label1);
		fM1.setLabelPositioning(Positioning.TOP);
		if (r.isFalseMarked()) fM1.setTokens(1);
		setPosition(fM1, x + 4.0, y + 2.0);
		nodes.add(fM1);

		Map<Node, VisualSignalTransition> fMRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition fMR = null;
		dy = 0.0;
		for (Node n: preset) {
			if (fMR == null || andSync) {
				fMR = stg.createSignalTransition(nameFalseM + name, type, SignalTransition.Direction.PLUS);
				stg.connect(fM0, fMR);
				stg.connect(fMR, fM1);
				stg.connect(M0, fMR);
				stg.connect(fMR, M1);
				setPosition(fMR, x, y + 4.0 + dy);
				nodes.add(fMR);
			}
			fMRs.put(n, fMR);
			dy += 1.0;
		}
		VisualSignalTransition fMF = stg.createSignalTransition(nameFalseM + name, type, SignalTransition.Direction.MINUS);
		stg.connect(fM1, fMF);
		stg.connect(fMF, fM0);
		stg.connect(M1, fMF);
		stg.connect(fMF, M0);
		setPosition(fMF, x, y + 2.0 - dy);
		nodes.add(fMF);

		// mutual exclusion
		createReadArcs(tM0, fMRs.values());
		createReadArcs(fM0, tMRs.values());

		stg.groupCollection(nodes);
		return new BinaryRegisterStg(M0, M1, tM0, tM1, tMRs, tMF, fM0, fM1, fMRs, fMF);
	}

	private BinaryRegisterStg generateControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		boolean andSync = (r.getSynchronisationType() == SynchronisationType.AND);
		boolean orSync = (r.getSynchronisationType() == SynchronisationType.OR);
		return generateBinaryRegisterSTG(r, andSync, orSync);
	}

	private void connectControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		BinaryRegisterStg rstg = getControlRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArcs(nstg.C1, rstg.tMRs.values());
			createReadArcs(nstg.C1, rstg.fMRs.values());
			createReadArc(nstg.C0, rstg.tMF);
			createReadArc(nstg.C0, rstg.fMF);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArcs(nstg.M1, rstg.fMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		Collection<VisualControlRegister> crPreset = dfs.getRPreset(r, VisualControlRegister.class);
		for (VisualControlRegister n: crPreset) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n));
				createReadArc(nstg.fM1, rstg.tMRs.get(n));
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n));
				createReadArc(nstg.fM1, rstg.fMRs.get(n));
			}
			if (r.getSynchronisationType() != SynchronisationType.PLAIN) {
				for (VisualControlRegister m: crPreset) {
					if (m == n) continue;
					BinaryRegisterStg mstg = getControlRegisterSTG(m);
					if (r.getSynchronisationType() == SynchronisationType.OR) {
						createReadArc(mstg.M1, rstg.tMRs.get(n));
					} else {
						createReadArc(mstg.M1, rstg.fMRs.get(n));
					}
				}
			}
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values());
			createReadArcs(nstg.tM1, rstg.fMRs.values());
			createReadArc(nstg.tM0, rstg.tMF);
			createReadArc(nstg.tM0, rstg.fMF);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArcs(nstg.M1, rstg.fMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
/*
			Connection connection = dfs.getConnection(r, n);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArcs(nstg.tM1, rstg.fMFs.values());
				createReadArcs(nstg.fM1, rstg.tMFs.values());
			} else {
				createReadArcs(nstg.tM1, rstg.tMFs.values());
				createReadArcs(nstg.fM1, rstg.fMFs.values());
			}
*/
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			Connection connection = dfs.getConnection(r, n);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArc(nstg.tM1, rstg.fMF);
				createReadArc(nstg.fM1, rstg.tMF);
			} else {
				createReadArc(nstg.tM1, rstg.tMF);
				createReadArc(nstg.fM1, rstg.fMF);
			}
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			Connection connection = dfs.getConnection(r, n);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArc(nstg.tM1, rstg.fMF);
				createReadArc(nstg.fM1, rstg.tMF);
			} else {
				createReadArc(nstg.tM1, rstg.tMF);
				createReadArc(nstg.fM1, rstg.fMF);
			}
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
	}

	public BinaryRegisterStg getControlRegisterSTG(VisualControlRegister register) {
		return controlRegisterMap.get(register);
	}

	private BinaryRegisterStg generatePushRegisterSTG(VisualPushRegister r) throws InvalidConnectionException {
		return generateBinaryRegisterSTG(r, false, false);
	}

	private void connectPushRegisterSTG(VisualPushRegister r) throws InvalidConnectionException {
		BinaryRegisterStg rstg = getPushRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArcs(nstg.C1, rstg.tMRs.values());
			createReadArcs(nstg.C1, rstg.fMRs.values());
			createReadArc(nstg.C0, rstg.tMF);
			createReadArc(nstg.C0, rstg.fMF);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArcs(nstg.M1, rstg.fMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n));
				createReadArc(nstg.fM1, rstg.tMRs.get(n));
				createReadArc(nstg.tM0, rstg.fMF);
				createReadArc(nstg.fM0, rstg.tMF);
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n));
				createReadArc(nstg.fM1, rstg.fMRs.get(n));
				createReadArc(nstg.tM0, rstg.tMF);
				createReadArc(nstg.fM0, rstg.fMF);
			}
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values());
			createReadArcs(nstg.tM1, rstg.fMRs.values());
			createReadArc(nstg.tM0, rstg.tMF);
			createReadArc(nstg.tM0, rstg.fMF);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArcs(nstg.M1, rstg.fMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
			createReadArc(nstg.M0, rstg.fMF);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF); // register M1 in R-postset is read only by tMF
			createReadArcs(nstg.M0, rstg.tMRs.values()); // register M0 in R-postset is read only by tMR
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF); // pop tM1 in R-postset is read only by tMF
			createReadArcs(nstg.tM0, rstg.tMRs.values()); // pop tM0 in R-postset is read only by tMR
		}
	}

	public BinaryRegisterStg getPushRegisterSTG(VisualPushRegister register) {
		return pushRegisterMap.get(register);
	}


	private BinaryRegisterStg generatePopRegisterSTG(VisualPopRegister r) throws InvalidConnectionException {
		return generateBinaryRegisterSTG(r, false, false);
	}

	private void connectPopRegisterSTG(VisualPopRegister r) throws InvalidConnectionException {
		BinaryRegisterStg rstg = getPopRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArcs(nstg.C1, rstg.tMRs.values());
			createReadArc(nstg.C0, rstg.tMF);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n));
				createReadArc(nstg.fM1, rstg.tMRs.get(n));
				createReadArc(nstg.tM0, rstg.fMF);
				createReadArc(nstg.fM0, rstg.tMF);
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n));
				createReadArc(nstg.fM1, rstg.fMRs.get(n));
				createReadArc(nstg.tM0, rstg.tMF);
				createReadArc(nstg.fM0, rstg.fMF);
			}
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values());
			createReadArc(nstg.tM0, rstg.tMF);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values());
			createReadArc(nstg.M0, rstg.tMF);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF);
			createReadArc(nstg.M1, rstg.fMF);
			createReadArcs(nstg.M0, rstg.tMRs.values());
			createReadArcs(nstg.M0, rstg.fMRs.values());
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF);
			createReadArc(nstg.tM1, rstg.fMF);
			createReadArcs(nstg.tM0, rstg.tMRs.values());
			createReadArcs(nstg.tM0, rstg.fMRs.values());
		}
	}

	public BinaryRegisterStg getPopRegisterSTG(VisualPopRegister register) {
		return popRegisterMap.get(register);
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		if (highLevelNode instanceof VisualLogic) {
			VisualLogic logic = (VisualLogic)highLevelNode;
			LogicStg lstg = getLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualRegister) {
			VisualRegister register = (VisualRegister)highLevelNode;
			RegisterStg rstg = getRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowLogic) {
			VisualCounterflowLogic logic = (VisualCounterflowLogic)highLevelNode;
			CounterflowLogicStg lstg = getCounterflowLogicSTG(logic);
			if (lstg != null) 	return lstg.contains(node);
		} else if (highLevelNode instanceof VisualCounterflowRegister) {
			VisualCounterflowRegister register = (VisualCounterflowRegister)highLevelNode;
			CounterflowRegisterStg rstg = getCounterflowRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualControlRegister) {
			VisualControlRegister register = (VisualControlRegister)highLevelNode;
			BinaryRegisterStg rstg = getControlRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualPushRegister) {
			VisualPushRegister register = (VisualPushRegister)highLevelNode;
			BinaryRegisterStg rstg = getPushRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		} else if (highLevelNode instanceof VisualPopRegister) {
			VisualPopRegister register = (VisualPopRegister)highLevelNode;
			BinaryRegisterStg rstg = getPopRegisterSTG(register);
			if (rstg != null) 	return rstg.contains(node);
		}
		return false;
	}

}
