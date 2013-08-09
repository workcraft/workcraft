package org.workcraft.plugins.sdfs.tools;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.sdfs.VisualLogic;
import org.workcraft.plugins.sdfs.VisualRegister;
import org.workcraft.plugins.sdfs.VisualSDFS;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public class STGGenerator {
	private static final String nameE = "E_";
	private static final String nameM = "M_";
	private static final String nameC = "C_";
	private static final String name0 = "_0";
	private static final String name1 = "_1";
	private static final String labelE = "E(";
	private static final String labelM = "M(";
	private static final String labelC = "C(";
	private static final String label0 = ")=0";
	private static final String label1 = ")=1";
	private static final double xScaling = 4;
	private static final double yScaling = 4;

	private static Map<VisualLogic, LogicSTG> logicMap = new HashMap<VisualLogic, LogicSTG>();
	private static Map<VisualRegister, RegisterSTG> registerMap = new HashMap<VisualRegister, RegisterSTG>();
	private final VisualSDFS sdfs;
	private final VisualSTG stg;

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

			for(VisualLogic l : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualLogic.class)) {
				connectLogicSTG(l);
			}
			for(VisualRegister r : Hierarchy.getDescendantsOfType(sdfs.getRoot(), VisualRegister.class)) {
				connectRegisterSTG(r);
			}
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	public VisualSTG getSTG() {
		return stg;
	}

	static void setPosition(Movable node, double x, double y) {
		TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
	}

	private LogicSTG generateLogicSTG(VisualLogic l) throws InvalidConnectionException {
		String name = sdfs.getName(l);
		AffineTransform transform = TransformHelper.getTransformToRoot(l);
		Point2D center = new Point2D.Double(
				xScaling * (transform.getTranslateX() + l.getX()),
				yScaling * (transform.getTranslateY() + l.getY()));
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace c0 = stg.createPlace(nameC + name + name0);
		c0.setLabel(labelC + name + label0);
		c0.setLabelPositioning(Positioning.BOTTOM);
		c0.setTokens(1);
		setPosition(c0, center.getX() + 2.0, center.getY() + 1.0);
		nodes.add(c0);

		VisualPlace c1 = stg.createPlace(nameC + name + name1);
		c1.setLabel(labelC + name + label1);
		c1.setLabelPositioning(Positioning.TOP);
		c1.setTokens(0);
		setPosition(c1, center.getX() + 2.0, center.getY() - 1.0);
		nodes.add(c1);

		VisualSignalTransition cr = stg.createSignalTransition(nameC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(c0, cr);
		stg.connect(cr, c1);
		setPosition(cr, center.getX() - 2.0, center.getY() + 1.0);
		nodes.add(cr);

		VisualSignalTransition cf = stg.createSignalTransition(nameC + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(c1, cf);
		stg.connect(cf, c0);
		setPosition(cf, center.getX() - 2.0, center.getY() - 1.0);
		nodes.add(cf);

		stg.groupCollection(nodes);
		return new LogicSTG(c0, c1, cr, cf);
	}

	private void connectLogicSTG(VisualLogic l) throws InvalidConnectionException {
		LogicSTG lstg = logicMap.get(l);
		for (Node n: sdfs.getPreset(l)) {
			if (n instanceof VisualLogic) {
				LogicSTG nstg = logicMap.get(n);
				stg.connect(nstg.c1, lstg.cr);
				stg.connect(lstg.cr, nstg.c1);
				stg.connect(nstg.c0, lstg.cf);
				stg.connect(lstg.cf, nstg.c0);
			} else if (n instanceof VisualRegister) {
				RegisterSTG nstg = registerMap.get(n);
				stg.connect(nstg.m1, lstg.cr);
				stg.connect(lstg.cr, nstg.m1);
				stg.connect(nstg.m0, lstg.cf);
				stg.connect(lstg.cf, nstg.m0);
			}
		}
	}

	private void connectRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		RegisterSTG rstg = registerMap.get(r);
		// enabling - disabling
		for (Node n: sdfs.getPreset(r)) {
			if (n instanceof VisualLogic) {
				LogicSTG nstg = logicMap.get(n);
				stg.connect(nstg.c1, rstg.er);
				stg.connect(rstg.er, nstg.c1);
				stg.connect(nstg.c0, rstg.ef);
				stg.connect(rstg.ef, nstg.c0);
			} else if (n instanceof VisualRegister) {
				RegisterSTG nstg = registerMap.get(n);
				stg.connect(nstg.m1, rstg.er);
				stg.connect(rstg.er, nstg.m1);
				stg.connect(nstg.m0, rstg.ef);
				stg.connect(rstg.ef, nstg.m0);
			}
		}
		// marking - unmarking
		stg.connect(rstg.e1, rstg.mr);
		stg.connect(rstg.mr, rstg.e1);
		stg.connect(rstg.e0, rstg.mf);
		stg.connect(rstg.mf, rstg.e0);
		for (VisualRegister n: sdfs.getRegisterPreset(r)) {
			if (n instanceof VisualRegister) {
				RegisterSTG nstg = registerMap.get(n);
				stg.connect(nstg.m1, rstg.mr);
				stg.connect(rstg.mr, nstg.m1);
				stg.connect(nstg.m0, rstg.mf);
				stg.connect(rstg.mf, nstg.m0);
			}
		}
		stg.connect(rstg.m1, rstg.ef);
		stg.connect(rstg.ef, rstg.m1);
		stg.connect(rstg.m0, rstg.er);
		stg.connect(rstg.er, rstg.m0);
		for (VisualRegister n: sdfs.getRegisterPostset(r)) {
			if (n instanceof VisualRegister) {
				RegisterSTG nstg = registerMap.get(n);
				stg.connect(nstg.m0, rstg.mr);
				stg.connect(rstg.mr, nstg.m0);
				stg.connect(nstg.m1, rstg.mf);
				stg.connect(rstg.mf, nstg.m1);
			}
		}
	}

	private RegisterSTG generateRegisterSTG(VisualRegister r) throws InvalidConnectionException {
		String name = sdfs.getName(r);
		AffineTransform transform = TransformHelper.getTransformToRoot(r);
		Point2D center = new Point2D.Double(
				xScaling * (transform.getTranslateX() + r.getX()),
				yScaling * (transform.getTranslateY() + r.getY()));
		Collection<Node> nodes = new LinkedList<Node>();

		VisualPlace e0 = stg.createPlace(nameE + name + name0);
		e0.setLabel(labelE + name + label0);
		e0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isEnabled()) e0.setTokens(1);
		setPosition(e0, center.getX() + 2.0, center.getY() - 2.0);
		nodes.add(e0);

		VisualPlace e1 = stg.createPlace(nameE + name + name1);
		e1.setLabel(labelE + name + label1);
		e1.setLabelPositioning(Positioning.TOP);
		if (r.isEnabled()) e1.setTokens(1);
		setPosition(e1, center.getX() + 2.0, center.getY() - 4.0);
		nodes.add(e1);

		VisualSignalTransition er = stg.createSignalTransition(nameE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(e0, er);
		stg.connect(er, e1);
		setPosition(er, center.getX() - 2.0, center.getY() - 2.0);
		nodes.add(er);

		VisualSignalTransition ef = stg.createSignalTransition(nameE + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(e1, ef);
		stg.connect(ef, e0);
		setPosition(ef, center.getX() - 2.0, center.getY() - 4.0);
		nodes.add(ef);

		VisualPlace m0 = stg.createPlace(nameM + name + name0);
		m0.setLabel(labelM + name + label0);
		m0.setLabelPositioning(Positioning.BOTTOM);
		if (!r.isMarked()) m0.setTokens(1);
		setPosition(m0, center.getX() + 2.0, center.getY() + 4.0);
		nodes.add(m0);

		VisualPlace m1 = stg.createPlace(nameM + name + name1);
		m1.setLabel(labelM + name + label1);
		m1.setLabelPositioning(Positioning.TOP);
		if (r.isMarked()) m1.setTokens(1);
		setPosition(m1, center.getX() + 2.0, center.getY() + 2.0);
		nodes.add(m1);

		VisualSignalTransition mr = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.PLUS);
		stg.connect(m0, mr);
		stg.connect(mr, m1);
		setPosition(mr, center.getX() - 2.0, center.getY() + 4.0);
		nodes.add(mr);

		VisualSignalTransition mf = stg.createSignalTransition(nameM + name, SignalTransition.Type.INTERNAL, SignalTransition.Direction.MINUS);
		stg.connect(m1, mf);
		stg.connect(mf, m0);
		setPosition(mf, center.getX() - 2.0, center.getY() + 2.0);
		nodes.add(mf);

		stg.groupCollection(nodes);
		return new RegisterSTG(e0, e1, er, ef, m0, m1, mr, mf);
	}


	public LogicSTG getLogicSTG(VisualLogic logic) {
		return logicMap.get(logic);
	}

	public boolean isRelated(VisualLogic logic, Node node) {
		LogicSTG lstg = getLogicSTG(logic);
		if (lstg != null) {
			return lstg.contains(node);
		}
		return false;
	}

	public RegisterSTG getRegisterSTG(VisualRegister register) {
		return registerMap.get(register);
	}

	public boolean isRelated(VisualRegister register, Node node) {
		RegisterSTG rstg = getRegisterSTG(register);
		if (rstg != null) {
			return rstg.contains(node);
		}
		return false;
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		if (highLevelNode instanceof VisualLogic) {
			return isRelated((VisualLogic)highLevelNode, node);
		} else if (highLevelNode instanceof VisualRegister) {
			return isRelated((VisualRegister)highLevelNode, node);
		}
		return false;
	}
}
