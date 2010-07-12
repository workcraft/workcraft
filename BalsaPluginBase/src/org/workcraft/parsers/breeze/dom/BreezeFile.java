package org.workcraft.parsers.breeze.dom;

import java.util.List;

import pcollections.PVector;
import pcollections.TreePVector;

public class BreezeFile
{
	public BreezeFile(List<String> imports, List<BreezeType> typeDefs, List<BreezePart> parts)
	{
		this.imports = TreePVector.from(imports);
		this.typeDefs = TreePVector.from(typeDefs);
		this.parts = TreePVector.from(parts);
	}
	public final PVector<BreezeType> typeDefs;
	public final PVector<String> imports;
	public final PVector<BreezePart> parts;
}
