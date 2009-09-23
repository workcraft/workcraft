package org.workcraft.plugins.balsa.stg;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stg.op.AndStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class BinaryFuncStgBuilder extends
		ComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilder()
	{
		opBuilders.put(BinaryOperator.AND, new AndStgBuilder());
	}

	HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>> opBuilders = new HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>>();

	@Override
	public void buildStg(BinaryFunc component, Map<String, Process> handshakes, StgBuilder builder) {
		ComponentStgBuilder<BinaryFunc> opBuilder = opBuilders.get(component.getOp());
		opBuilder.buildStg(component, handshakes, builder);
	}

}
