package org.workcraft.plugins.balsa.stg.codegenerator;

import java.util.HashMap;
import java.util.Map;

public class DataPathSplitters
{
	static final Map<String, PrimitiveDataPathSplitter> splitters = createSplitters();

	public static PrimitiveDataPathSplitter getSplitter(String name) {
		return splitters.get(name);
	}

	private static Map<String, PrimitiveDataPathSplitter> createSplitters() {
		Map<String, PrimitiveDataPathSplitter> map = new HashMap<String, PrimitiveDataPathSplitter>();
		//map.put("Fetch", value);
		return map;
	}

}


/*private static PVector<PortDeclaration> getControlPorts(PrimitivePart primitive)
{

	if(isPureControl(primitive))
		return primitive.getPorts();
	else
		return primitive.getPorts().plus(defaultGoPort());
}
*/