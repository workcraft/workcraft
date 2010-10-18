package org.workcraft;

public interface Module extends Plugin {
	public String getDescription();
	public void init(Framework framework);
}
