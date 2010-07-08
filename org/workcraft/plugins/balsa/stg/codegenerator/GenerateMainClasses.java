package org.workcraft.plugins.balsa.stg.codegenerator;

import java.io.File;
import java.io.IOException;

import org.workcraft.plugins.balsa.io.BalsaSystem;

public class GenerateMainClasses
{
	public static void main(String[] args) throws IOException
	{
		System.exit(_main(args));
	}
	public static int _main(String[] args) throws IOException
	{
		if(args.length < 1)
		{
			System.err.print("Need at least 1 parameter: path to the Workcraft project to generate code there");
			return 1;
		}
		if(args.length > 2)
		{
			System.err.print("Need at most 2 parameters: path to the Workcraft project to generate code there + path to BALSA home folder");
			return 1;
		}
		new CodeGenerator().generateBaseClasses(new File(args[0]), args.length == 2 ? new BalsaSystem(new File(args[1])): new BalsaSystem());
		System.out.println("Generated!");
		return 0;
	}
}
