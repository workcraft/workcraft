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
		if(args.length < 2)
		{
			printUsage();
			System.err.print("Need at least 2 parameters: path to the Workcraft project to generate code there");
			return 1;
		}
		if(args.length > 3)
		{
			printUsage();
			return 1;
		}
		File outPath = new File(args[0]);
		String[] packagePath = args[1].split("[\\\\\\/\\.]");
		BalsaSystem balsa = args.length == 3 ? new BalsaSystem(new File(args[2])): new BalsaSystem();
		System.out.println("Generating Balsa wrappers to '" + outPath + "'.");
		File packageFile = outPath;
		for(String s : packagePath)
			packageFile = new File(packageFile, s);
		System.out.println("Package directory will be: '" + packageFile.getAbsolutePath() + "'.");
		if(!packageFile.exists())
		{
			System.out.println("Error: the package directory does not exist.");
			return 2;
		}

		if(!packageFile.isDirectory())
		{
			System.out.println("Error: the package directory is not a directory.");
			return 2;
		}

		new CodeGenerator().generateBaseClasses(outPath, packagePath, balsa);

		System.out.println("Classes generated successfully.");
		return 0;
	}

	private static void printUsage()
	{
		System.out.println("This program generates base classes wrapping the Balsa primitive parts");
		System.out.println();
		System.out.println("Usage: GenerateMainClasses <src> <package> [<balsa_path>]");
		System.out.println();
	}
}
