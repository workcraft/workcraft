package org.workcraft.plugins.balsa.stg;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stg.op.AndStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class BinaryFuncStgBuilderStgBuilder extends
		ComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilderStgBuilder()
	{
		opBuilders.put(BinaryOperator.AND, new AndStgBuilder());
	}

	HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>> opBuilders = new HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>>();

	@Override
	public void buildStg(BinaryFunc component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		ComponentStgBuilder<BinaryFunc> opBuilder = opBuilders.get(component.getOp());
		opBuilder.buildStg(component, handshakes, builder);
	}

}
