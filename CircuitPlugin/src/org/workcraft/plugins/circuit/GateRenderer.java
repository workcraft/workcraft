package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;

public class GateRenderer {
	public enum ComponentType {
		BASIC, GATE, C_ELEMENT, LATCH, FLIP_FLOP
	}

	public static ComponentType determineType(BooleanFormula set, BooleanFormula reset) {
		// first, check the number of outputs, if more than one, then return BASIC
		return ComponentType.BASIC;
	}

	public static void drawGate(BooleanFormula formula, Graphics2D g) {

	}

	public static void drawCElement(BooleanFormula formula, Graphics2D g) {

	}
}
