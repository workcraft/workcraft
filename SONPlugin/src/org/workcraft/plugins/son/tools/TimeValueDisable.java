package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeValueDisable implements Tool{

	private static boolean showTimeValues = false;

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Time analysis";
	}

	public String getDisplayName(){
		return "Enable/Disable time values";
	}

	public void run(WorkspaceEntry we){
		showTimeValues = !showTimeValues;
	}

	public static boolean showErrorTracing(){
		return showTimeValues;
	}

}
