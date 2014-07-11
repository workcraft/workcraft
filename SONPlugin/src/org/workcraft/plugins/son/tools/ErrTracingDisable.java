package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ErrTracingDisable implements Tool{

	private static boolean showErrorTracing = false;

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Error tracing";
	}

	public String getDisplayName(){
		return "Enable/disable error tracing";
	}

	public void run(WorkspaceEntry we){
		showErrorTracing = !showErrorTracing;
	}

	public static boolean showErrorTracing(){
		return showErrorTracing;
	}

}
