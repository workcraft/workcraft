package org.workcraft.plugins.balsa.stg.codegenerator;

import java.io.File;
import java.io.IOException;

import org.workcraft.plugins.balsa.io.BalsaSystem;

public class GenerateStubs {
	public static int main(String [] args) throws IOException
	{
		if(args.length < 1)
		{
			System.err.print("Need at leas 1 parameter: path to the Workcraft project to generate code there");
			return 1;
		}
		if(args.length > 2)
		{
			System.err.print("Need at most 2 parameters: path to the Workcraft project to generate code there + path to BALSA home folder");
			return 1;
		}
		BalsaSystem system;
		if(args.length == 2)
			system = new BalsaSystem(new File(args[1]));
		else
			system = new BalsaSystem();

		new CodeGenerator().generateStubs(new File(args[0]), system);
		return 0;
	}
}
