package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ColorRefreshTool implements Tool {

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Custom tools";
	}

	public String getDisplayName(){
		return "Refresh color";
	}

	public void run(WorkspaceEntry we){
		SONModel net=(SONModel)we.getModelEntry().getMathModel();
		net.refreshColor();
	}

}
