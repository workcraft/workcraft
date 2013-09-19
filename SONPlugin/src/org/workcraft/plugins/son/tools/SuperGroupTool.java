package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SuperGroupTool implements Tool{

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Custom tools";
	}

	public String getDisplayName(){
		return "Super group";
	}

	public void run(WorkspaceEntry we){
		VisualSON net=(VisualSON)we.getModelEntry().getVisualModel();
		net.superGroupSelection();
	}
}
