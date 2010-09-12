package org.workcraft.plugins.balsa.io;

import java.io.File;

public class BalsaSystem
{
	private static final String balsaHomeStatic = System.getenv("BALSAHOME");

	// there are 2 possible paths to account for balsa versions 3.5 and 4.0
	private static final String[][] ABSPATHS = new String[][]{
		new String[]{"share", "tech", "common", "components"},
		new String[]{"share", "tech", "common"}
		//new String[]{"share", "style", "four_b_rb"}
	};

	public static BalsaSystem DEFAULT() { return new BalsaSystem(); }

	private final File balsaHome;

	public BalsaSystem()
	{
		if(balsaHomeStatic == null || balsaHomeStatic.isEmpty())
			throw new NullPointerException("BALSAHOME environment variable not set -- cannot load primitive parts definitions.");
		this.balsaHome = new File(balsaHomeStatic);
	}

	public BalsaSystem(File balsaHome) {
		if(balsaHome == null)
			throw new NullPointerException("balsaHome");
		this.balsaHome = balsaHome;
	}

	public File getBalsaHome() {
		return balsaHome;
	}

	public File getDefinitionsDir()
	{

		for(String[] path : ABSPATHS)
		{
			File file = balsaHome;
			for(String s : path)
				file = new File(file, s);
			if(file.exists())
				return file;
			System.out.println(file.getAbsolutePath() + " does not exist");
		}
		throw new RuntimeException("Balsa definitions directory does not exist!");
	}
}
