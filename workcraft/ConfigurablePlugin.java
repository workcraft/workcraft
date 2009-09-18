package org.workcraft;


public interface ConfigurablePlugin extends Plugin {
	public void readConfig(Config config);
}
