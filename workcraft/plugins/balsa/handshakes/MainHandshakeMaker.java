package org.workcraft.plugins.balsa.handshakes;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.ActiveEagerFalseVariable;
import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainHandshakeMaker {
	static Map<Class<? extends Component>, HandshakeMaker<?>> map = getMap();

	public static Map<String, Handshake> getHandshakes(Component component)
	{
		HandshakeMaker<?> maker = map.get(component.getClass());
		if(maker == null)
			return  new HashMap<String, Handshake>();

		return maker.getComponentHandshakes(component);
	}

	private static Map<Class<? extends Component>, HandshakeMaker<?>> getMap() {
		HashMap<Class<? extends Component>, HandshakeMaker<?>> map = new HashMap<Class<? extends Component>, HandshakeMaker<?>>();

		map.put(While.class, new WhileHandshakes());
		map.put(Adapt.class, new AdaptHandshakes());
		map.put(BinaryFunc.class, new BinaryFuncHandshakes());
		map.put(Concur.class, new ConcurHandshakes());
		map.put(SequenceOptimised.class, new SequenceOptimisedHandshakes());

		return map;
	}
}

class ActiveEagerFalseVariableHandshakes extends HandshakeMaker<ActiveEagerFalseVariable>
{
	@Override
	protected void fillHandshakes(ActiveEagerFalseVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("write", builder.CreateActivePull(component.getWidth()));
		handshakes.put("signal", builder.CreateActiveSync());
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(8));//TODO: find out how the "specification" should be used
	}
}
