package org.workcraft.gui.propertyeditor;

import java.util.List;

import org.workcraft.framework.Config;

public interface PersistentPropertyEditable {
	public List<PropertyDescriptor> getPersistentPropertyDeclarations();

	public void storePersistentProperties (Config config);
	public void loadPersistentProperties (Config config);
}
