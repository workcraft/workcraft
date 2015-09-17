package org.workcraft.plugins.son;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;

public class TimeConsistencySettings {

	private boolean inconsistencyHighlight, unspecifyHighlight;
	private ArrayList<ONGroup> selectedGroups;
	private ScenarioRef seletedScenario;
	private ArrayList<Node> seletedNodes;
	private int tabIndex;
	private Granularity granularity;

	public TimeConsistencySettings(boolean inconsistencyHighlight,
			boolean unspecifyHighlight, ArrayList<ONGroup> selectedGroups,
			ScenarioRef seletedScenario, ArrayList<Node> seletedNodes, int tabIndex,
			Granularity granularity){
		this.inconsistencyHighlight = 	inconsistencyHighlight;
		this.unspecifyHighlight = unspecifyHighlight;
		this.selectedGroups = selectedGroups;
		this.seletedScenario = seletedScenario;
		this.seletedNodes = seletedNodes;
		this.tabIndex = tabIndex;
		this.granularity = granularity;
	}

	public boolean getInconsistencyHighlight(){
		return inconsistencyHighlight;
	}

	public boolean getUnspecifyHighlight(){
		return unspecifyHighlight;
	}

	public ArrayList<ONGroup> getSelectedGroups(){
		return selectedGroups;
	}

	public ScenarioRef getSeletedScenario(){
		return seletedScenario;
	}

	public ArrayList<Node> getSeletedNodes(){
		return seletedNodes;
	}

	public int getTabIndex(){
		return tabIndex;
	}

	public Granularity getGranularity(){
		return granularity;
	}

}
