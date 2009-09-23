package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BinaryFuncHandshakes extends HandshakeMaker<BinaryFunc> {

	@Override
	protected void fillHandshakes(BinaryFunc component, Map<String, Handshake> handshakes) {
		if(isBoolean(component.getOp()))
		handshakes.put("inpA", builder.CreateActivePull(component.getInputAWidth()));
		handshakes.put("inpB", builder.CreateActivePull(component.getInputAWidth()));
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}

	private boolean isBoolean(BinaryOperator op) {
		return
		op == BinaryOperator.EQUALS ||
		op == BinaryOperator.GREATER_OR_EQUALS ||
		op == BinaryOperator.GREATER_THAN ||
		op == BinaryOperator.LESS_OR_EQUALS ||
		op == BinaryOperator.LESS_THAN;
	}

}
