package org.workcraft.plugins.son;

import java.util.ArrayList;


public class StructureVerifySettings {

	private boolean errNodesHighlight, outputBefore;
	private ArrayList<ONGroup> selectedGroups;
	private int type;

	public StructureVerifySettings(boolean errNodesHighlight,boolean outputBefore, ArrayList<ONGroup> selectedGroups, int type){
		this.errNodesHighlight = errNodesHighlight;
		this.outputBefore = outputBefore;
		this.selectedGroups = selectedGroups;
		this.type = type;
	}

	public boolean getErrNodesHighlight(){
		return this.errNodesHighlight;
	}

	public boolean getOuputBefore(){
		return this.outputBefore;
	}

	public ArrayList<ONGroup> getSelectedGroups(){
		return this.selectedGroups;
	}

	public int getType(){
		return this.type;
	}
}
