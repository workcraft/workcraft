package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;

public class SONAlg extends RelationAlgorithm{

    private BSONAlg bsonAlg;
    private Collection<ONGroup> upperGroups;
    private Collection<ONGroup> lowerGroups;

    public SONAlg(SON net) {
        super(net);
        bsonAlg = new BSONAlg(net);

        upperGroups = bsonAlg.getUpperGroups(net.getGroups());
        lowerGroups = bsonAlg.getLowerGroups(net.getGroups());
    }

    //get SON initial marking
    public Collection<PlaceNode> getSONInitial(){
        Collection<PlaceNode> result = new ArrayList<PlaceNode>();

        for(ONGroup group : net.getGroups()){
            if(upperGroups.contains(group))
                for(Condition c : getONInitial(group)){
                    result.add(c);
                }
            //an initial state of a lower group is the initial state of SON
            //if all of its upper conditions are the initial states.
            else if(lowerGroups.contains(group)){
                for(Condition c : getONInitial(group)){
                    boolean isInitial = true;
                    Collection<Condition> set = bsonAlg.getUpperConditions(c);
                    for(Condition c2 : set){
                        if(!isInitial(c2)){
                            ONGroup group2 = net.getGroup(c2);
                            if(!set.containsAll(getONInitial(group2)))
                                isInitial = false;
                        }
                    }
                    if(isInitial) result.add(c);
                }
            } else{
                for(Condition c : getONInitial(group)){
                    result.add(c);
                }
            }
        }
        return result;
    }


    //get SON final marking
    public Collection<PlaceNode> getSONFinal(){
        Collection<PlaceNode> result = new ArrayList<PlaceNode>();

        for(ONGroup group : net.getGroups()){
            if(upperGroups.contains(group))
                for(Condition c : getONFinal(group)){
                    result.add(c);
                }

            else if(lowerGroups.contains(group)){
                for(Condition c : getONFinal(group)){
                    boolean isFinal = true;
                    Collection<Condition> set = bsonAlg.getUpperConditions(c);
                    for(Condition c2 : set){
                        if(!isInitial(c2)){
                            ONGroup group2 = net.getGroup(c2);
                            if(!set.containsAll(getONFinal(group2)))
                                isFinal = false;
                        }
                    }
                    if(isFinal) result.add(c);
                }
            } else{
                for(Condition c : getONFinal(group)){
                    result.add(c);
                }
            }
        }
        return result;
    }
}
