package org.workcraft.plugins.son;

import java.util.ArrayList;

import org.workcraft.dom.Node;

public class TimeConsistencySettings {

	private boolean errNodesHighlight;
	private ArrayList<ONGroup> selectedGroups;
	private Scenario seletedScenario;
	private ArrayList<Node> seletedNodes;

	public TimeConsistencySettings(boolean errNodesHighlight,ArrayList<ONGroup> selectedGroups, Scenario seletedScenario, ArrayList<Node> seletedNodes){
		this.errNodesHighlight = errNodesHighlight;
		this.selectedGroups = selectedGroups;
		this.seletedScenario = seletedScenario;
		this.seletedNodes = seletedNodes;
	}

	public boolean getErrNodesHighlight(){
		return this.errNodesHighlight;
	}

	public ArrayList<ONGroup> getSelectedGroups(){
		return this.selectedGroups;
	}

	public Scenario getSeletedScenario(){
		return this.seletedScenario;
	}

	public ArrayList<Node> getSeletedNodes(){
		return this.seletedNodes;
	}

}
