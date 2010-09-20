package org.workcraft;

public interface Module extends Plugin {
	Class<?> [] getPluginClasses();
	public void init(Framework framework);
}
