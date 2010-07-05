package org.workcraft.plugins.balsa.stg.codegenerator;

import java.io.File;
import java.io.IOException;

import org.workcraft.plugins.balsa.io.BalsaSystem;

public class GenerateStubs {
	public static void main(String [] args) throws IOException
	{
		if(args.length < 1)
		{
			System.err.print("Need at least 1 parameter: path to the Workcraft project to generate code there");
			System.exit(1);
		}
		else
		if(args.length > 2)
		{
			System.err.print("Need at most 2 parameters: path to the Workcraft project to generate code there + path to BALSA home folder");
			System.exit(1);
		}
		else
		{
			BalsaSystem system  = args.length == 2 ? new BalsaSystem(new File(args[1])) : new BalsaSystem();

			new CodeGenerator().generateStubs(new File(args[0]), system);
			System.exit(0);
		}
	}
}
