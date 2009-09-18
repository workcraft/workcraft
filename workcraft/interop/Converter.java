package org.workcraft.interop;
import java.io.FileFilter;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;

public interface Converter extends Plugin, FileFilter {
	public Class <? extends Model> getSourceModelClass();
	public Class <? extends Model> getTargetModelClass();
	public boolean supportsVisual();
	public Model convert (Model source);
}