package org.workcraft.plugins.balsa.stg.codegenerator;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.parsers.breeze.dom.ArrayedDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.ArrayedSyncPortDeclaration;
import org.workcraft.parsers.breeze.dom.BooleanPortDeclaration;
import org.workcraft.parsers.breeze.dom.DataPortDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.PortVisitor;
import org.workcraft.parsers.breeze.dom.SyncPortDeclaration;

import pcollections.PVector;

public class DataPathSplitters
{
	static final Map<String, PrimitiveDataPathSplitter> splitters = createSplitters();

	public static PrimitivePart getControl(PrimitivePart part)
	{
		if("While".equals(part.getName()))
			return new PrimitivePart(part.getName(), part.getParameters(), boolify(part.getPorts(), "guard"), part.getSymbol());
		return part;
	}

	private static PVector<PortDeclaration> boolify(PVector<PortDeclaration> ports, String portName) {
		int portId = -1;
		for(int i=0;i<ports.size();i++)
		{
			PortDeclaration port = ports.get(i);
			if(port.getName().equals(portName))
				portId = i;
		}
		PortDeclaration port = ports.get(portId).accept(new PortVisitor<PortDeclaration>() {
			@Override
			public PortDeclaration visit(ArrayedDataPortDeclaration port) {
				throw new org.workcraft.exceptions.NotSupportedException();
			}

			@Override
			public PortDeclaration visit(ArrayedSyncPortDeclaration port) {
				throw new org.workcraft.exceptions.NotSupportedException();
			}

			@Override
			public PortDeclaration visit(SyncPortDeclaration port) {
				throw new org.workcraft.exceptions.NotSupportedException();
			}

			@Override
			public PortDeclaration visit(DataPortDeclaration port) {
				return new BooleanPortDeclaration(port.getName(), port.isActive(), port.isInput(), port.getWidth());
			}

			@Override
			public PortDeclaration visit(BooleanPortDeclaration port) {
				 throw new org.workcraft.exceptions.NotSupportedException();
			}

		});
		return ports.minus(portId).plus(portId, port);
	}
	public static PrimitiveDataPathSplitter getSplitter(String name) {

		if(name.equals("While"))
		{

		}

		PrimitiveDataPathSplitter result = splitters.get(name);
		if(result == null)
			throw new RuntimeException("Splitter not found for " + name);
		return result;
	}

	private static Map<String, PrimitiveDataPathSplitter> createSplitters() {

		PrimitiveDataPathSplitter defaultSplitter = new PrimitiveDataPathSplitter() {
			@Override public PrimitivePart getControlDefinition(PrimitivePart primitive) {
				return primitive;
			}
		};

		Map<String, PrimitiveDataPathSplitter> map = new HashMap<String, PrimitiveDataPathSplitter>();
		map.put("Fetch", defaultSplitter);
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