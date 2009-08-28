package org.workcraft.framework;
import java.io.FileFilter;

import org.workcraft.dom.AbstractMathModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Converter extends Plugin, FileFilter {
	public Class <? extends AbstractMathModel> getSourceModelClass();
	public Class <? extends AbstractMathModel> getTargetModelClass();
	public boolean supportsVisual();
	public Model convert (Model source);
}