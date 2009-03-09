package org.workcraft.plugins.modelchecking;

import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface ModelChecker extends Plugin {
	public String getDisplayName();
	public boolean isApplicableTo(Model model);
	public void run (Model model);
}
