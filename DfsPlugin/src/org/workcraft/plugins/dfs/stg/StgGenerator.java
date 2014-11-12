package org.workcraft.plugins.dfs.stg;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dfs.ControlRegister.SynchronisationType;
import org.workcraft.plugins.dfs.DfsSettings;
import org.workcraft.plugins.dfs.VisualBinaryRegister;
import org.workcraft.plugins.dfs.VisualControlConnection;
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.CieColorUtils;
import org.workcraft.util.ColorGenerator;
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

	static Color[] tokenColors = DfsSettings.getTokenPalette().colors;
	private ColorGenerator createColorGenerator(boolean required) {
		ColorGenerator result = null;
		if (required) {
			if (tokenColors == null) {
				tokenColors = CieColorUtils.getLabPalette(5, 5, 5, 0.2f, 0.7f);
			}
			result = new ColorGenerator(tokenColors);
		}
		return result;
	}

	public VisualSTG getSTG() {
		return stg;
	}

	static void setPosition(Movable node, double x, double y) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
	}

	private void createConsumingArc(VisualPlace p, VisualSignalTransition t, boolean propagateTokenColor) throws InvalidConnectionException {
		stg.connect(p, t);
		for (Connection c: stg.getConnections(p)) {
			if ((c.getSecond() == t) && (c instanceof VisualConnection)) {
				VisualConnection vc = (VisualConnection)c;
				vc.setTokenColorPropagator(propagateTokenColor);
			}
		}
	}

	private void createProducingArc(VisualSignalTransition t, VisualPlace p, boolean propagateTokenColor) throws InvalidConnectionException {
		stg.connect(t, p);
		for (Connection c: stg.getConnections(t)) {
			if ((c.getSecond() == p) && (c instanceof VisualConnection)) {
				VisualConnection vc = (VisualConnection)c;
				vc.setTokenColorPropagator(propagateTokenColor);
			}
		}
	}

	private void createReadArc(VisualPlace p, VisualSignalTransition t, boolean propagateTokenColor) throws InvalidConnectionException {
		if (p != null && t != null) {
			createConsumingArc(p, t, propagateTokenColor);
			createProducingArc(t, p, false);
		}
	}

	private void createReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts, boolean propagateTokenColor) throws InvalidConnectionException {
		for (VisualSignalTransition t : new HashSet<VisualSignalTransition>(ts)) {
			createReadArc(p, t, propagateTokenColor);
		}
	}

	private LogicStg generateLogicSTG(VisualLogic l) throws InvalidConnectionException {
		String name = dfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		double x =	xScaling * (transform.getTranslateX() + l.getX());
		double y =	yScaling * (transform.getTranslateY() + l.getY());
		Collection<Node> nodes = new LinkedList<Node>();
		SignalTransition.Type type = SignalTransition.Type.INTERNAL;
		ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(l).size() == 0);


		Container curContainer = null;

		VisualPlace C0 = stg.createPlace(nameC + name + name0, curContainer);
		C0.setLabel(labelC + name + label0);
		C0.setLabelPositioning(Positioning.BOTTOM);
		if (!l.getReferencedLogic().isComputed()) {
			C0.getReferencedPlace().setTokens(1);
		}
		C0.setForegroundColor(l.getForegroundColor());
		C0.setFillColor(l.getFillColor());
		setPosition(C0, x + 2.0, y + 1.0);
		nodes.add(C0);

		VisualPlace C1 = stg.createPlace(nameC + name + name1, curContainer);
		C1.setLabel(labelC + name + label1);
		C1.setLabelPositioning(Positioning.TOP);
		if (l.getReferencedLogic().isComputed()) {
			C1.getReferencedPlace().setTokens(1);
		}
		C1.setForegroundColor(l.getForegroundColor());
		C1.setFillColor(l.getFillColor());
		setPosition(C1, x + 2.0, y - 1.0);
		nodes.add(C1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getPreset(l, VisualLogic.class));
		preset.addAll(dfs.getPreset(l, VisualRegister.class));
		preset.addAll(dfs.getPreset(l, VisualControlRegister.class));
		preset.addAll(dfs.getPreset(l, VisualPushRegister.class));
		preset.addAll(dfs.getPreset(l, VisualPopRegister.class));
		if (preset.size() == 0) {
			preset.add(l);
		}
		Map<Node, VisualSignalTransition> CRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> CFs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition CR = null;
		VisualSignalTransition CF = null;
		double dy = 0.0;
		for (Node n: preset) {
			if (CR == null || l.getReferencedLogic().isEarlyEvaluation()) {
				CR = stg.createSignalTransition(nameC + name, type, SignalTransition.Direction.PLUS, curContainer);
				CR.setTokenColorGenerator(tokenColorGenerator);
				createConsumingArc(C0, CR, false);
				createProducingArc(CR, C1, true);
				setPosition(CR, x - 2.0, y + 1.0 + dy);
				nodes.add(CR);
			}
			CRs.put(n, CR);
			if (CF == null) {
				CF = stg.createSignalTransition(nameC + name, type, SignalTransition.Direction.MINUS, curContainer);
				createConsumingArc(C1, CF, false);
				createProducingArc(CF, C0, false);
				setPosition(CF, x - 2.0, y - 1.0 - dy);
				nodes.add(CF);
			}
			CFs.put(n, CF);
			dy += 1.0;
		}

		stg.select(nodes);
		stg.groupSelection();
		return new LogicStg(C0, C1, CRs, CFs);
	}

	private void connectLogicSTG(VisualLogic l) throws InvalidConnectionException {
		LogicStg lstg = getLogicSTG(l);
		for (VisualLogic n: dfs.getPreset(l, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArc(nstg.C1, lstg.CRs.get(n), true);
			createReadArc(nstg.C0, lstg.CFs.get(n), false);
		}
		for (VisualRegister n: dfs.getPreset(l, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n), true);
			createReadArc(nstg.M0, lstg.CFs.get(n), false);
		}
		for (VisualControlRegister n: dfs.getPreset(l, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n), true);
			createReadArc(nstg.M0, lstg.CFs.get(n), false);
		}
		for (VisualPushRegister n: dfs.getPreset(l, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, lstg.CRs.get(n), true);
			createReadArc(nstg.tM0, lstg.CFs.get(n), false);
		}
		for (VisualPopRegister n: dfs.getPreset(l, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.M1, lstg.CRs.get(n), true);
			createReadArc(nstg.M0, lstg.CFs.get(n), false);
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
		ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(r).size() == 0);
		Container curContainer = null;

		VisualPlace M0 = stg.createPlace(nameM + name + name0, curContainer);
		M0.setLabel(labelM + name + label0);
		M0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedRegister().isMarked()) {
			M0.getReferencedPlace().setTokens(1);
		}
		M0.setForegroundColor(r.getForegroundColor());
		M0.setFillColor(r.getFillColor());
		setPosition(M0, x + 2.0, y + 1.0);
		nodes.add(M0);

		VisualPlace M1 = stg.createPlace(nameM + name + name1, curContainer);
		M1.setLabel(labelM + name + label1);
		M1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedRegister().isMarked()) {
			M1.getReferencedPlace().setTokens(1);
		}
		M1.setTokenColor(r.getTokenColor());
		setPosition(M1, x + 2.0, y - 1.0);
		nodes.add(M1);

		VisualSignalTransition MR = stg.createSignalTransition(nameM + name, type, SignalTransition.Direction.PLUS, curContainer);
		MR.setTokenColorGenerator(tokenColorGenerator);
		createConsumingArc(M0, MR, false);
		createProducingArc(MR, M1, true);
		setPosition(MR, x - 2.0, y + 1.0);
		nodes.add(MR);

		VisualSignalTransition MF = stg.createSignalTransition(nameM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(M1, MF, false);
		createProducingArc(MF, M0, false);
		setPosition(MF, x - 2.0, y - 1.0);
		nodes.add(MF);

		stg.select(nodes);
		stg.groupSelection();
		return new RegisterStg(M0, M1, MR, MF);
	}

	private void connectRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		RegisterStg rstg = getRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArc(nstg.C1, rstg.MR, true);
			createReadArc(nstg.C0, rstg.MF, false);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR, true);
			createReadArc(nstg.M0, rstg.MF, false);
		}
		for (VisualCounterflowRegister n: dfs.getRPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.MR, true);
			createReadArc(nstg.orM0, rstg.MF, false);
			createReadArc(nstg.andM1, rstg.MF, false);
			createReadArc(nstg.andM0, rstg.MR, false);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR, true);
			createReadArc(nstg.M0, rstg.MF, false);
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MR, true);
			createReadArc(nstg.tM0, rstg.MF, false);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MR, true);
			createReadArc(nstg.M0, rstg.MF, false);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF, false);
			createReadArc(nstg.M0, rstg.MR, false);
		}
		for (VisualCounterflowRegister n: dfs.getRPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.andM1, rstg.MF, false);
			createReadArc(nstg.andM0, rstg.MR, false);
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF, false);
			createReadArc(nstg.M0, rstg.MR, false);
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.MF, false);
			createReadArc(nstg.M0, rstg.MR, false);
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.MF, false);
			createReadArc(nstg.tM0, rstg.MR, false);
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
		ColorGenerator presetTokenColorGenerator = createColorGenerator(dfs.getPreset(l).size() == 0);
		ColorGenerator postsetTokenColorGenerator = createColorGenerator(dfs.getPostset(l).size() == 0);

		Container curContainer = null;

		VisualPlace fwC0 = stg.createPlace(nameFwC + name + name0, curContainer);
		fwC0.setLabel(labelFwC + name + label0);
		fwC0.setLabelPositioning(Positioning.BOTTOM);
		if (!l.getReferencedCounterflowLogic().isForwardComputed()) {
			fwC0.getReferencedPlace().setTokens(1);
		}
		fwC0.setForegroundColor(l.getForegroundColor());
		fwC0.setFillColor(l.getFillColor());
		setPosition(fwC0, x + 2.0, y - 2.0);
		nodes.add(fwC0);

		VisualPlace fwC1 = stg.createPlace(nameFwC + name + name1, curContainer);
		fwC1.setLabel(labelFwC + name + label1);
		fwC1.setLabelPositioning(Positioning.TOP);
		if (l.getReferencedCounterflowLogic().isForwardComputed()) {
			fwC1.getReferencedPlace().setTokens(1);
		}
		fwC1.setForegroundColor(l.getForegroundColor());
		fwC1.setFillColor(l.getFillColor());
		setPosition(fwC1, x + 2.0, y - 4.0);
		nodes.add(fwC1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getPreset(l, VisualCounterflowLogic.class));
		preset.addAll(dfs.getPreset(l, VisualCounterflowRegister.class));
		if (preset.size() == 0) {
			preset.add(l);
		}
		Map<Node, VisualSignalTransition> fwCRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> fwCFs = new HashMap<Node, VisualSignalTransition>();
		{
			VisualSignalTransition fwCR = null;
			VisualSignalTransition fwCF = null;
			double dy = 0.0;
			for (Node n: preset) {
				if (fwCR == null || l.getReferencedCounterflowLogic().isForwardEarlyEvaluation()) {
					fwCR = stg.createSignalTransition(nameFwC + name, type, SignalTransition.Direction.PLUS, curContainer);
					fwCR.setTokenColorGenerator(presetTokenColorGenerator);
					createConsumingArc(fwC0, fwCR, false);
					createProducingArc(fwCR, fwC1, true);
					setPosition(fwCR, x - 2.0, y - 2.0 + dy);
					nodes.add(fwCR);
				}
				fwCRs.put(n, fwCR);
				if (fwCF == null) {
					fwCF = stg.createSignalTransition(nameFwC + name, type, SignalTransition.Direction.MINUS, curContainer);
					createConsumingArc(fwC1, fwCF, false);
					createProducingArc(fwCF, fwC0, false);
					setPosition(fwCF, x - 2.0, y - 4.0 - dy);
					nodes.add(fwCF);
				}
				fwCFs.put(n, fwCF);
				dy += 1.0;
			}
		}

		VisualPlace bwC0 = stg.createPlace(nameBwC + name + name0, curContainer);
		bwC0.setLabel(labelBwC + name + label0);
		bwC0.setLabelPositioning(Positioning.BOTTOM);
		if (!l.getReferencedCounterflowLogic().isBackwardComputed()) {
			bwC0.getReferencedPlace().setTokens(1);
		}
		bwC0.setForegroundColor(l.getForegroundColor());
		bwC0.setFillColor(l.getFillColor());
		setPosition(bwC0, x + 2.0, y + 4.0);
		nodes.add(bwC0);

		VisualPlace bwC1 = stg.createPlace(nameBwC + name + name1, curContainer);
		bwC1.setLabel(labelBwC + name + label1);
		bwC1.setLabelPositioning(Positioning.TOP);
		if (l.getReferencedCounterflowLogic().isBackwardComputed()) {
			bwC1.getReferencedPlace().setTokens(1);
		}
		bwC1.setForegroundColor(l.getForegroundColor());
		bwC1.setFillColor(l.getFillColor());
		setPosition(bwC1, x + 2.0, y + 2.0);
		nodes.add(bwC1);

		Set<Node> postset = new HashSet<Node>();
		postset.addAll(dfs.getPostset(l, VisualCounterflowLogic.class));
		postset.addAll(dfs.getPostset(l, VisualCounterflowRegister.class));
		if (postset.size() == 0) {
			postset.add(l);
		}
		Map<Node, VisualSignalTransition> bwCRs = new HashMap<Node, VisualSignalTransition>();
		Map<Node, VisualSignalTransition> bwCFs = new HashMap<Node, VisualSignalTransition>();
		{
			VisualSignalTransition bwCR = null;
			VisualSignalTransition bwCF = null;
			double dy = 0.0;
			for (Node n: postset) {
				if (bwCR == null || l.getReferencedCounterflowLogic().isBackwardEarlyEvaluation()) {
					bwCR = stg.createSignalTransition(nameBwC + name, type, SignalTransition.Direction.PLUS, curContainer);
					bwCR.setTokenColorGenerator(postsetTokenColorGenerator);
					createConsumingArc(bwC0, bwCR, false);
					createProducingArc(bwCR, bwC1, false);
					setPosition(bwCR, x - 2.0, y + 4.0 + dy);
					nodes.add(bwCR);
				}
				bwCRs.put(n, bwCR);
				if (bwCF == null) {
					bwCF = stg.createSignalTransition(nameBwC + name, type, SignalTransition.Direction.MINUS, curContainer);
					createConsumingArc(bwC1, bwCF, false);
					createProducingArc(bwCF, bwC0, false);
					setPosition(bwCF, x - 2.0, y + 2.0 - dy);
					nodes.add(bwCF);
				}
				bwCFs.put(n, bwCF);
				dy += 1.0;
			}
		}

		stg.select(nodes);
		stg.groupSelection();
		return new CounterflowLogicStg(fwC0, fwC1, fwCRs, fwCFs, bwC0, bwC1, bwCRs, bwCFs);
	}

	private void connectCounterflowLogicSTG(VisualCounterflowLogic l) throws InvalidConnectionException {
		CounterflowLogicStg lstg = getCounterflowLogicSTG(l);
		// preset
		for (VisualCounterflowLogic n: dfs.getPreset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, lstg.fwCRs.get(n), true);
			createReadArc(nstg.fwC0, lstg.fwCFs.get(n), false);
		}
		for (VisualCounterflowRegister n: dfs.getPreset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.fwCRs.get(n), true);
			createReadArc(nstg.orM0, lstg.fwCFs.get(n), false);
		}
		// postset
		for (VisualCounterflowLogic n: dfs.getPostset(l, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, lstg.bwCRs.get(n), false);
			createReadArc(nstg.bwC0, lstg.bwCFs.get(n), false);
		}
		for (VisualCounterflowRegister n: dfs.getPostset(l, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, lstg.bwCRs.get(n), false);
			createReadArc(nstg.orM0, lstg.bwCFs.get(n), false);
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
		ColorGenerator presetTokenColorGenerator = createColorGenerator(dfs.getPreset(r).size() == 0);
		ColorGenerator postsetTokenColorGenerator = createColorGenerator(dfs.getPostset(r).size() == 0);

		Container curContainer = null;

		VisualPlace orM0 = stg.createPlace(nameOrM + name + name0, curContainer);
		orM0.setLabel(labelOrM + name + label0);
		orM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedCounterflowRegister().isOrMarked()) {
			orM0.getReferencedPlace().setTokens(1);
		}
		orM0.setForegroundColor(r.getForegroundColor());
		orM0.setFillColor(r.getFillColor());
		setPosition(orM0, x + 2.0, y - 2.0);
		nodes.add(orM0);

		VisualPlace orM1 = stg.createPlace(nameOrM + name + name1, curContainer);
		orM1.setLabel(labelOrM + name + label1);
		orM1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedCounterflowRegister().isOrMarked()) {
			orM1.getReferencedPlace().setTokens(	1);
		}
		orM1.setForegroundColor(r.getForegroundColor());
		orM1.setFillColor(r.getFillColor());
		setPosition(orM1, x + 2.0, y - 4.0);
		nodes.add(orM1);

		VisualSignalTransition orMRfw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.PLUS, curContainer);
		orMRfw.setTokenColorGenerator(presetTokenColorGenerator);
		createConsumingArc(orM0, orMRfw, false);
		createProducingArc(orMRfw, orM1, true);
		setPosition(orMRfw, x - 2.0, y - 2.5);
		nodes.add(orMRfw);

		VisualSignalTransition orMRbw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.PLUS, curContainer);
		orMRbw.setTokenColorGenerator(postsetTokenColorGenerator);
		createConsumingArc(orM0, orMRbw, false);
		createProducingArc(orMRbw, orM1, true);
		setPosition(orMRbw, x - 2.0, y - 1.5);
		nodes.add(orMRbw);

		VisualSignalTransition orMFfw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(orM1, orMFfw, false);
		createProducingArc(orMFfw, orM0, false);
		setPosition(orMFfw, x - 2.0, y - 4.5);
		nodes.add(orMFfw);

		VisualSignalTransition orMFbw = stg.createSignalTransition(nameOrM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(orM1, orMFbw, false);
		createProducingArc(orMFbw, orM0, false);
		setPosition(orMFbw, x - 2.0, y - 3.5);
		nodes.add(orMFbw);

		VisualPlace andM0 = stg.createPlace(nameAndM + name + name0, curContainer);
		andM0.setLabel(labelAndM + name + label0);
		andM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedCounterflowRegister().isAndMarked()) {
			andM0.getReferencedPlace().setTokens(1);
		}
		andM0.setForegroundColor(r.getForegroundColor());
		andM0.setFillColor(r.getFillColor());
		setPosition(andM0, x + 2.0, y + 4.0);
		nodes.add(andM0);

		VisualPlace andM1 = stg.createPlace(nameAndM + name + name1, curContainer);
		andM1.setLabel(labelAndM + name + label1);
		andM1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedCounterflowRegister().isAndMarked()) {
			andM1.getReferencedPlace().setTokens(1);
		}
		andM1.setForegroundColor(r.getForegroundColor());
		andM1.setFillColor(r.getFillColor());
		setPosition(andM1, x + 2.0, y + 2.0);
		nodes.add(andM1);

		VisualSignalTransition andMR = stg.createSignalTransition(nameAndM + name, type, SignalTransition.Direction.PLUS, curContainer);
		createConsumingArc(andM0, andMR, false);
		createProducingArc(andMR, andM1, false);
		setPosition(andMR, x - 2.0, y + 4.0);
		nodes.add(andMR);

		VisualSignalTransition andMF = stg.createSignalTransition(nameAndM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(andM1, andMF, false);
		createProducingArc(andMF, andM0, false);
		setPosition(andMF, x - 2.0, y + 2.0);
		nodes.add(andMF);

		stg.select(nodes);
		stg.groupSelection();
		return new CounterflowRegisterStg(orM0, orM1, orMRfw, orMRbw, orMFfw, orMFbw, andM0, andM1, andMR, andMF);
	}

	private void connectCounterflowRegisterSTG(VisualCounterflowRegister r) throws InvalidConnectionException {
		CounterflowRegisterStg rstg = getCounterflowRegisterSTG(r);

        for (VisualRegister n: dfs.getPreset(r, VisualRegister.class)) {
            RegisterStg nstg = getRegisterSTG(n);
            createReadArc(nstg.M1, rstg.orMRfw, true);
            createReadArc(nstg.M1, rstg.andMR, false);
            createReadArc(nstg.M0, rstg.orMFfw, false);
            createReadArc(nstg.M0, rstg.andMF, false);
        }
        for (VisualRegister n: dfs.getPostset(r, VisualRegister.class)) {
            RegisterStg nstg = getRegisterSTG(n);
            createReadArc(nstg.M1, rstg.orMRbw, true);
            createReadArc(nstg.M1, rstg.andMR, false);
            createReadArc(nstg.M0, rstg.orMFbw, false);
            createReadArc(nstg.M0, rstg.andMF, false);
        }

        for (VisualCounterflowLogic n: dfs.getPreset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.fwC1, rstg.orMRfw, true);
			createReadArc(nstg.fwC0, rstg.orMFfw, false);
			createReadArc(nstg.fwC1, rstg.andMR, false);
			createReadArc(nstg.fwC0, rstg.andMF, false);
		}
		for (VisualCounterflowLogic n: dfs.getPostset(r, VisualCounterflowLogic.class)) {
			CounterflowLogicStg nstg = getCounterflowLogicSTG(n);
			createReadArc(nstg.bwC1, rstg.orMRbw, true);
			createReadArc(nstg.bwC0, rstg.orMFbw, false);
			createReadArc(nstg.bwC1, rstg.andMR, false);
			createReadArc(nstg.bwC0, rstg.andMF, false);
		}

		for (VisualCounterflowRegister n: dfs.getPreset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.orMRfw, true);
			createReadArc(nstg.orM0, rstg.orMFfw, false);
		}
		for (VisualCounterflowRegister n: dfs.getPostset(r, VisualCounterflowRegister.class)) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.orMRbw, true);
			createReadArc(nstg.orM0, rstg.orMFbw, false);
		}

		Set<VisualCounterflowRegister> rSet = new HashSet<VisualCounterflowRegister>();
		rSet.add(r);
		rSet.addAll(dfs.getRPreset(r, VisualCounterflowRegister.class));
		rSet.addAll(dfs.getRPostset(r, VisualCounterflowRegister.class));
		for (VisualCounterflowRegister n: rSet) {
			CounterflowRegisterStg nstg = getCounterflowRegisterSTG(n);
			createReadArc(nstg.orM1, rstg.andMR, true);
			createReadArc(nstg.orM0, rstg.andMF, false);
			createReadArc(nstg.andM1, rstg.orMFfw, false);
			createReadArc(nstg.andM1, rstg.orMFbw, false);
			createReadArc(nstg.andM0, rstg.orMRfw, false);
			createReadArc(nstg.andM0, rstg.orMRbw, false);
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
		ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(r).size() == 0);
		Container curContainer = null;

		VisualPlace M0 = stg.createPlace(nameM + name + name0, curContainer);
		M0.setLabel(labelM + name + label0);
		M0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedBinaryRegister().isTrueMarked() && !r.getReferencedBinaryRegister().isFalseMarked()) {
			M0.getReferencedPlace().setTokens(1);
		}
		M0.setForegroundColor(r.getForegroundColor());
		M0.setFillColor(r.getFillColor());
		setPosition(M0, x - 4.0, y + 1.0);
		nodes.add(M0);

		VisualPlace M1 = stg.createPlace(nameM + name + name1, curContainer);
		M1.setLabel(labelM + name + label1);
		M1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedBinaryRegister().isTrueMarked() || r.getReferencedBinaryRegister().isFalseMarked()) {
			M1.getReferencedPlace().setTokens(1);
		}
		M1.setForegroundColor(r.getForegroundColor());
		M1.setFillColor(r.getFillColor());
		setPosition(M1, x - 4.0, y - 1.0);
		nodes.add(M1);

		VisualPlace tM0 = stg.createPlace(nameTrueM + name + name0, curContainer);
		tM0.setLabel(labelTrueM + name + label0);
		tM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedBinaryRegister().isTrueMarked()) {
			tM0.getReferencedPlace().setTokens(1);
		}
		tM0.setForegroundColor(r.getForegroundColor());
		tM0.setFillColor(r.getFillColor());
		setPosition(tM0, x + 4.0, y - 2.0);
		nodes.add(tM0);

		VisualPlace tM1 = stg.createPlace(nameTrueM + name + name1, curContainer);
		tM1.setLabel(labelTrueM + name + label1);
		tM1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedBinaryRegister().isTrueMarked()) {
			tM1.getReferencedPlace().setTokens(1);
		}
		tM1.setForegroundColor(r.getForegroundColor());
		tM1.setFillColor(r.getFillColor());
		setPosition(tM1, x + 4.0, y - 4.0);
		nodes.add(tM1);

		Set<Node> preset = new HashSet<Node>();
		preset.addAll(dfs.getRPreset(r, VisualControlRegister.class));
		if (preset.size() == 0) {
			preset.add(r);
		}

		Map<Node, VisualSignalTransition> tMRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition tMR = null;
		double dy = 0.0;
		for (Node n: preset) {
			if (tMR == null || orSync) {
				tMR = stg.createSignalTransition(nameTrueM + name, type, SignalTransition.Direction.PLUS, curContainer);
				tMR.setTokenColorGenerator(tokenColorGenerator);
				createConsumingArc(tM0, tMR, false);
				createProducingArc(tMR, tM1, true);
				createConsumingArc(M0, tMR, false);
				createProducingArc(tMR, M1, true);
				setPosition(tMR, x, y - 2.0 + dy);
				nodes.add(tMR);
			}
			tMRs.put(n, tMR);
			dy += 1.0;
		}
		VisualSignalTransition tMF = stg.createSignalTransition(nameTrueM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(tM1, tMF, false);
		createProducingArc(tMF, tM0, false);
		createConsumingArc(M1, tMF, false);
		createProducingArc(tMF, M0, false);
		setPosition(tMF, x, y - 4.0 - dy);
		nodes.add(tMF);


		VisualPlace fM0 = stg.createPlace(nameFalseM + name + name0, curContainer);
		fM0.setLabel(labelFalseM + name + label0);
		fM0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.getReferencedBinaryRegister().isFalseMarked()) {
			fM0.getReferencedPlace().setTokens(1);
		}
		fM0.setForegroundColor(r.getForegroundColor());
		fM0.setFillColor(r.getFillColor());
		setPosition(fM0, x + 4.0, y + 4.0);
		nodes.add(fM0);

		VisualPlace fM1 = stg.createPlace(nameFalseM + name + name1, curContainer);
		fM1.setLabel(labelFalseM + name + label1);
		fM1.setLabelPositioning(Positioning.TOP);
		if (r.getReferencedBinaryRegister().isFalseMarked()) {
			fM1.getReferencedPlace().setTokens(1);
		}
		fM1.setForegroundColor(r.getForegroundColor());
		fM1.setFillColor(r.getFillColor());
		setPosition(fM1, x + 4.0, y + 2.0);
		nodes.add(fM1);

		Map<Node, VisualSignalTransition> fMRs = new HashMap<Node, VisualSignalTransition>();
		VisualSignalTransition fMR = null;
		dy = 0.0;
		for (Node n: preset) {
			if (fMR == null || andSync) {
				fMR = stg.createSignalTransition(nameFalseM + name, type, SignalTransition.Direction.PLUS, curContainer);
				fMR.setTokenColorGenerator(tokenColorGenerator);
				createConsumingArc(fM0, fMR, false);
				createProducingArc(fMR, fM1, true);
				createConsumingArc(M0, fMR, false);
				createProducingArc(fMR, M1, true);
				setPosition(fMR, x, y + 4.0 + dy);
				nodes.add(fMR);
			}
			fMRs.put(n, fMR);
			dy += 1.0;
		}
		VisualSignalTransition fMF = stg.createSignalTransition(nameFalseM + name, type, SignalTransition.Direction.MINUS, curContainer);
		createConsumingArc(fM1, fMF, false);
		createProducingArc(fMF, fM0, false);
		createConsumingArc(M1, fMF, false);
		createProducingArc(fMF, M0, false);
		setPosition(fMF, x, y + 2.0 - dy);
		nodes.add(fMF);

		// mutual exclusion
		createReadArcs(tM0, fMRs.values(), false);
		createReadArcs(fM0, tMRs.values(), false);

		stg.select(nodes);
		stg.groupSelection();
		return new BinaryRegisterStg(M0, M1, tM0, tM1, tMRs, tMF, fM0, fM1, fMRs, fMF);
	}

	private BinaryRegisterStg generateControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		boolean andSync = (r.getReferencedControlRegister().getSynchronisationType() == SynchronisationType.AND);
		boolean orSync = (r.getReferencedControlRegister().getSynchronisationType() == SynchronisationType.OR);
		return generateBinaryRegisterSTG(r, andSync, orSync);
	}

	private void connectControlRegisterSTG(VisualControlRegister r) throws InvalidConnectionException {
		BinaryRegisterStg rstg = getControlRegisterSTG(r);
		// preset
		for (VisualLogic n: dfs.getPreset(r, VisualLogic.class)) {
			LogicStg nstg = getLogicSTG(n);
			createReadArcs(nstg.C1, rstg.tMRs.values(), true);
			createReadArcs(nstg.C1, rstg.fMRs.values(), true);
			createReadArc(nstg.C0, rstg.tMF, false);
			createReadArc(nstg.C0, rstg.fMF, false);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArcs(nstg.M1, rstg.fMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
			createReadArc(nstg.M0, rstg.fMF, false);
		}
		Collection<VisualControlRegister> crPreset = dfs.getRPreset(r, VisualControlRegister.class);
		for (VisualControlRegister n: crPreset) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).getReferencedControlConnection().isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.tMRs.get(n), true);
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.fMRs.get(n), true);
			}
			if (r.getReferencedControlRegister().getSynchronisationType() != SynchronisationType.PLAIN) {
				for (VisualControlRegister m: crPreset) {
					if (m == n) continue;
					BinaryRegisterStg mstg = getControlRegisterSTG(m);
					if (r.getReferencedControlRegister().getSynchronisationType() == SynchronisationType.OR) {
						createReadArc(mstg.M1, rstg.tMRs.get(n), true);
					}
					if (r.getReferencedControlRegister().getSynchronisationType() == SynchronisationType.AND) {
						createReadArc(mstg.M1, rstg.fMRs.get(n), true);
					}
				}
			}
			createReadArc(nstg.M0, rstg.tMF, false);
			createReadArc(nstg.M0, rstg.fMF, false);
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values(), true);
			createReadArcs(nstg.tM1, rstg.fMRs.values(), true);
			createReadArc(nstg.tM0, rstg.tMF, false);
			createReadArc(nstg.tM0, rstg.fMF, false);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArcs(nstg.M1, rstg.fMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
			createReadArc(nstg.M0, rstg.fMF, false);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArc(nstg.M1, rstg.fMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArc(nstg.M1, rstg.fMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			Connection connection = dfs.getConnection(r, n);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).getReferencedControlConnection().isInverting()) {
				createReadArc(nstg.tM1, rstg.fMF, false);
				createReadArc(nstg.fM1, rstg.tMF, false);
			} else {
				createReadArc(nstg.tM1, rstg.tMF, false);
				createReadArc(nstg.fM1, rstg.fMF, false);
			}
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			Connection connection = dfs.getConnection(r, n);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).getReferencedControlConnection().isInverting()) {
				createReadArc(nstg.tM1, rstg.fMF, false);
				createReadArc(nstg.fM1, rstg.tMF, false);
			} else {
				createReadArc(nstg.tM1, rstg.tMF, false);
				createReadArc(nstg.fM1, rstg.fMF, false);
			}
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
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
			createReadArcs(nstg.C1, rstg.tMRs.values(), true);
			createReadArcs(nstg.C1, rstg.fMRs.values(), true);
			createReadArc(nstg.C0, rstg.tMF, false);
			createReadArc(nstg.C0, rstg.fMF, false);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArcs(nstg.M1, rstg.fMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
			createReadArc(nstg.M0, rstg.fMF, false);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).getReferencedControlConnection().isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.tMRs.get(n), true);
				createReadArc(nstg.tM0, rstg.fMF, false);
				createReadArc(nstg.fM0, rstg.tMF, false);
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.fMRs.get(n), true);
				createReadArc(nstg.tM0, rstg.tMF, false);
				createReadArc(nstg.fM0, rstg.fMF, false);
			}
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values(), true);
			createReadArcs(nstg.tM1, rstg.fMRs.values(), true);
			createReadArc(nstg.tM0, rstg.tMF, false);
			createReadArc(nstg.tM0, rstg.fMF, false);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArcs(nstg.M1, rstg.fMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
			createReadArc(nstg.M0, rstg.fMF, false);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false); // register M1 in R-postset is read only by tMF
			createReadArcs(nstg.M0, rstg.tMRs.values(), false); // register M0 in R-postset is read only by tMR
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF, false); // pop tM1 in R-postset is read only by tMF
			createReadArcs(nstg.tM0, rstg.tMRs.values(), false); // pop tM0 in R-postset is read only by tMR
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
			createReadArcs(nstg.C1, rstg.tMRs.values(), true);
			createReadArc(nstg.C0, rstg.tMF, false);
		}
		// R-preset
		for (VisualRegister n: dfs.getRPreset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
		}
		for (VisualControlRegister n: dfs.getRPreset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			Connection connection = dfs.getConnection(n, r);
			if (connection instanceof VisualControlConnection && ((VisualControlConnection)connection).getReferencedControlConnection().isInverting()) {
				createReadArc(nstg.tM1, rstg.fMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.tMRs.get(n), true);
				createReadArc(nstg.tM0, rstg.fMF, false);
				createReadArc(nstg.fM0, rstg.tMF, false);
			} else {
				createReadArc(nstg.tM1, rstg.tMRs.get(n), true);
				createReadArc(nstg.fM1, rstg.fMRs.get(n), true);
				createReadArc(nstg.tM0, rstg.tMF, false);
				createReadArc(nstg.fM0, rstg.fMF, false);
			}
		}
		for (VisualPushRegister n: dfs.getRPreset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArcs(nstg.tM1, rstg.tMRs.values(), true);
			createReadArc(nstg.tM0, rstg.tMF, false);
		}
		for (VisualPopRegister n: dfs.getRPreset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArcs(nstg.M1, rstg.tMRs.values(), true);
			createReadArc(nstg.M0, rstg.tMF, false);
		}
		// R-postset
		for (VisualRegister n: dfs.getRPostset(r, VisualRegister.class)) {
			RegisterStg nstg = getRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArc(nstg.M1, rstg.fMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualControlRegister n: dfs.getRPostset(r, VisualControlRegister.class)) {
			BinaryRegisterStg nstg = getControlRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArc(nstg.M1, rstg.fMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualPushRegister n: dfs.getRPostset(r, VisualPushRegister.class)) {
			BinaryRegisterStg nstg = getPushRegisterSTG(n);
			createReadArc(nstg.M1, rstg.tMF, false);
			createReadArc(nstg.M1, rstg.fMF, false);
			createReadArcs(nstg.M0, rstg.tMRs.values(), false);
			createReadArcs(nstg.M0, rstg.fMRs.values(), false);
		}
		for (VisualPopRegister n: dfs.getRPostset(r, VisualPopRegister.class)) {
			BinaryRegisterStg nstg = getPopRegisterSTG(n);
			createReadArc(nstg.tM1, rstg.tMF, false);
			createReadArc(nstg.tM1, rstg.fMF, false);
			createReadArcs(nstg.tM0, rstg.tMRs.values(), false);
			createReadArcs(nstg.tM0, rstg.fMRs.values(), false);
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
