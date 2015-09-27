package org.workcraft.plugins.son.algorithm;

import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;

public class TimeAlg extends RelationAlgorithm{

	private SON net;

	public TimeAlg(SON net) {
		super(net);
		this.net = net;
	}

	public void setProperties(){
		for(Condition c : net.getConditions()){
			if(net.getInputPNConnections(c).isEmpty()){
				((Condition)c).setInitial(true);
			}
			if(net.getOutputPNConnections(c).isEmpty()){
				((Condition)c).setFinal(true);
			}
		}
	}

	public void removeProperties(){
		for(PlaceNode c : net.getPlaceNodes()){
			if(c instanceof Condition){
				((Condition)c).setInitial(false);
				((Condition)c).setFinal(false);
			}
		}
	}

}
