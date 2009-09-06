package org.workcraft.framework;
import java.io.FileFilter;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Converter extends Plugin, FileFilter {
	public Class <? extends AbstractModel> getSourceModelClass();
	public Class <? extends AbstractModel> getTargetModelClass();
	public boolean supportsVisual();
	public Model convert (Model source);
}