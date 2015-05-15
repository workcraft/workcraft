package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TokenRefreshTool implements Tool{


	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Custom tools";
	}

	public String getDisplayName(){
		return "Reset tokens";
	}

	public void run(WorkspaceEntry we){
		SON net=(SON)we.getModelEntry().getMathModel();
		for(PlaceNode con : net.getPlaceNodes())
			con.setMarked(false);
	}

}
