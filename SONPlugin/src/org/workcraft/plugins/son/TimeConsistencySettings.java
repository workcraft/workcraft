package org.workcraft.plugins.son;

import java.util.ArrayList;

import org.workcraft.dom.Node;

public class TimeConsistencySettings {

	private boolean inconsistencyHighlight, unspecifyHighlight;
	private ArrayList<ONGroup> selectedGroups;
	private Scenario seletedScenario;
	private ArrayList<Node> seletedNodes;
	private int tabIndex;

	public TimeConsistencySettings(boolean inconsistencyHighlight,
			boolean unspecifyHighlight, ArrayList<ONGroup> selectedGroups,
			Scenario seletedScenario, ArrayList<Node> seletedNodes, int tabIndex){
		this.inconsistencyHighlight = 	inconsistencyHighlight;
		this.unspecifyHighlight = inconsistencyHighlight;
		this.selectedGroups = selectedGroups;
		this.seletedScenario = seletedScenario;
		this.seletedNodes = seletedNodes;
		this.tabIndex = tabIndex;
	}

	public boolean getInconsistencyHighlight(){
		return this.inconsistencyHighlight;
	}

	public boolean getUnspecifyHighlight(){
		return this.unspecifyHighlight;
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

	public int getTabIndex(){
		return tabIndex;
	}

}
