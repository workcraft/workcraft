package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class GateRenderer {
	public enum ComponentType {
		BASIC, GATE, C_ELEMENT, LATCH, FLIP_FLOP
	}


	public static ComponentType determineType(BooleanFormula set, BooleanFormula reset) {
		// first, check the number of outputs, if more than one, then return BASIC
		return ComponentType.BASIC;
	}

	public static GateRenderingResult renderGate(BooleanFormula formula) {
		return formula.accept(new BooleanVisitor<GateRenderingResult>(){

			@Override
			public GateRenderingResult visit(And node) {
				return null;
			}

			@Override
			public GateRenderingResult visit(Iff node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(Xor node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(Zero node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(One node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(Not node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(Imply node) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(BooleanVariable variable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GateRenderingResult visit(Or node) {
				// TODO Auto-generated method stub
				return null;
			}

		});

	}

	public static void drawCElement(BooleanFormula formula, Graphics2D g) {

	}
}
