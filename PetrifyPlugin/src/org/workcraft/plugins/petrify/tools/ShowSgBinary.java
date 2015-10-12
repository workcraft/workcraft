package org.workcraft.plugins.petrify.tools;

import org.workcraft.plugins.stg.STG;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ShowSgBinary extends ShowSg {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STG.class);
	}

	@Override
	public String getDisplayName() {
		return "Binary-encoded state graph [write_sg + draw_astg]";
	}

	public boolean isBinaryEncodded() {
		return true;
	}

}
