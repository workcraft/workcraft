package org.workcraft.framework.plugins;

import org.workcraft.framework.Config;

public interface ConfigurablePlugin extends Plugin {
	public void readConfig(Config config);
}
