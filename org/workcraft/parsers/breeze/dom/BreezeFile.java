package org.workcraft.parsers.breeze.dom;

import java.util.List;

public class BreezeFile
{
	public BreezeFile(List<String> imports, List<BreezePart> parts)
	{
		this.imports = imports;
		this.parts = parts;
	}
	public final List<String> imports;
	public final List<BreezePart> parts;
}
