package org.workcraft.framework;
import java.io.FileFilter;

import org.workcraft.dom.MathModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Converter extends Plugin, FileFilter {
	public Class <? extends MathModel> getSourceModelClass();
	public Class <? extends MathModel> getTargetModelClass();
	public boolean supportsVisual();
	public Model convert (Model source);
}