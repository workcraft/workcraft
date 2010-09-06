package org.workcraft;

public interface Plugin {
	Class<?> [] getPluginClasses();
	public void init(Framework framework);
}
