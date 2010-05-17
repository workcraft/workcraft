package org.workcraft.parsers.breeze;

public interface PrimitiveFactory<Port> {
	public abstract BreezeInstance<Port> create(PrimitivePart declaration, ParameterScope parameters);
}