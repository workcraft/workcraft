package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;

public class TimeAlg extends RelationAlgorithm{

    private SON net;

    public TimeAlg(SON net) {
        super(net);
        this.net = net;
    }

    public void setProperties(){
        for(Condition c : net.getConditions()){
            if(net.getInputPNConnections(c).isEmpty()){
                ((Condition) c).setInitial(true);
            }
            if(net.getOutputPNConnections(c).isEmpty()){
                ((Condition) c).setFinal(true);
            }
        }
    }

    public void removeProperties(){
        for(PlaceNode c : net.getPlaceNodes()){
            if(c instanceof Condition){
                ((Condition) c).setInitial(false);
                ((Condition) c).setFinal(false);
            }
        }
    }

    public void setDefaultTime(Node node){
        Interval input = new Interval(0000, 9999);
        if(node instanceof Condition){
            Condition c = (Condition) node;
            if(c.isInitial() && !c.isFinal()){
                c.setEndTime(input);
                return;
            }else if(c.isFinal() && !c.isInitial()){
                c.setStartTime(input);
                return;
            }else if(!c.isFinal() && !c.isInitial()){
                c.setStartTime(input);
                c.setEndTime(input);
            }else{
                return;
            }
        }else if(node instanceof Time){
            ((Time) node).setStartTime(input);
            ((Time) node).setEndTime(input);
        }
    }
}
