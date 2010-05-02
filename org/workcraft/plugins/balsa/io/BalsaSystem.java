package org.workcraft.plugins.balsa.io;

import java.io.File;

public class BalsaSystem
{
	private static final String balsaHomeStatic = System.getenv("BALSAHOME");

	private static final String[] ABSPATH = new String[]{"share", "tech", "common", "components"};

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
		File result = balsaHome;
		for(String s : ABSPATH)
			result = new File(result, s);
		return result;
	}
}
