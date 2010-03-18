package org.workcraft.plugins.balsa.stg.codegenerator;

import java.io.File;
import java.io.IOException;

import org.workcraft.plugins.balsa.io.BalsaSystem;

public class GenerateMainClasses
{
	public static void main(String[] args) throws IOException
	{
		new CodeGenerator().generateBaseClasses(new File(args[0]), args.length == 2 ? new BalsaSystem(new File(args[1])): new BalsaSystem());
	}
}
