package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JOptionPane;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.plugins.son.util.Marking;
import org.workcraft.plugins.son.util.Phase;

public class BSONAlg extends RelationAlgorithm{

    private SON net;
    private static Marking dfsResult =new Marking();
    private Map<Condition, String> phaseCutErr =new HashMap<Condition, String>();

    public BSONAlg(SON net) {
        super(net);
        this.net = net;
    }

    /**
     * get all related behavoural connections for a given set of groups.
     */
    public Collection<SONConnection> getRelatedBhvLine(Collection<ONGroup> groups){
        HashSet<SONConnection> result = new HashSet<SONConnection>();

        for(SONConnection con : net.getSONConnections()){
            if (con.getSemantics() == Semantics.BHVLINE)
                for(ONGroup group : groups){
                    if(group.contains(con.getFirst())){
                        for (ONGroup nextGroup : groups){
                            if(nextGroup.contains(con.getSecond()))
                                result.add(con);
                    }
                }
            }
        }
        return result;
    }

    /**
     * check if a given group is line like. i.e., post/pre set of each node < 1.
     */
    public boolean isLineLikeGroup(ONGroup group){
        for(Node node : group.getComponents()){
            if(net.getPostset(node).size() > 1 && group.containsAll(net.getPostset(node)))
                return false;
            if(net.getPreset(node).size() > 1 && group.containsAll(net.getPreset(node)))
                return false;
        }
        return true;
    }

    /**
     * get phases collection for a given upper-level condition
     * @throws InvalidPhaseException
     */
    public Collection<Phase> getPhases(Condition c, Map<ONGroup, List<Marking>> allMarkings){

        Collection<Phase> result = new ArrayList<Phase>();

        for(ONGroup group : getLowerGroups(net.getGroups())){
            //find all nodes pointing to c
            Marking min = new Marking();
            min.addAll(getPathBounding(getONInitial(group), c, true));
            Marking max = new Marking();
            max.addAll(getPathBounding(getONFinal(group), c, false));

            if(!min.isEmpty() && !max.isEmpty()){
                //group does not have alternative behaviours
                if(!allMarkings.keySet().contains(group)){
                    Phase phase = new Phase();
                    for(Node node : PathAlgorithm.dfs2(min, max, net)){
                        if(node instanceof Condition)
                            phase.add((Condition)node);
                    }
                    result.add(phase);
                }else{
                    List<Marking> markings = allMarkings.get(group);
                    Collection<Marking> minMarkings = new ArrayList<Marking>();
                    Collection<Marking> maxMarkings = new ArrayList<Marking>();

                    Collection<Node> minSet = new ArrayList<Node>();
                    Collection<Node> maxSet = new ArrayList<Node>();

                    for(Marking m : markings){
                        if(min.containsAll(m)){
                            minMarkings.add(m);
                            minSet.addAll(m);
                        }
                        if(max.containsAll(m)){
                            maxMarkings.add(m);
                            maxSet.addAll(m);
                        }
                    }
                    for(Marking min1 : minMarkings){
                        for(Marking max1 : maxMarkings){
                            Phase phase = new Phase();
                            for(Node node : PathAlgorithm.dfs2(min1, max1, net)){
                                if(node instanceof Condition)
                                    phase.add((Condition)node);
                            }
                            result.add(phase);
                        }
                    }
                    min.removeAll(minSet);
                    max.removeAll(maxSet);

                    String err = null;
                    if(!min.isEmpty()){
                        err = "ERROR: Minimal phase " +net.toString(min) + " is not a cut:";
                    }
                    if(!max.isEmpty()){
                        if(err == null)
                            err = "ERROR: Maximal phase " +net.toString(max) + " is not a cut:";
                        else{
                            err += "\nERROR: Maximal phase " +net.toString(max) + " is not a cut:";
                        }
                    }
                    if(err != null )
                        phaseCutErr.put(c, err);
                }
            }
        }
        return result;
    }

    private Marking getPathBounding(Collection<Condition> nodes, Node upper, boolean getMin){
        dfsResult.clear();
        LinkedList<Node> visited = new LinkedList<Node>();
        for(Condition s : nodes){
            visited.add(s);
            dfs(visited, upper, getMin);
        }
        return dfsResult;
    }

    private void dfs(LinkedList<Node> visited, Node upper, boolean getMin) {
        Node n = visited.getLast();
        if((n instanceof Condition) && getPostBhvSet((Condition)n).contains(upper)){
              dfsResult.add((Condition)n);
        }else{
            Collection<Node> neighbours = null;
            if(getMin)
                neighbours = getPostPNSet(n);
            else
                neighbours = getPrePNSet(n);

            for (Node node : neighbours) {
                if (!visited.contains(node)) {
                    visited.addLast(node);
                    dfs(visited, upper, getMin);
                }
            }
        }
    }

    public Map<ONGroup, List<Marking>> getReachableMarking(){
        Map<ONGroup, List<Marking>> result = new HashMap<ONGroup, List<Marking>>();

        ASONAlg alg = new ASONAlg(net);
        Collection<ONGroup> lowerGroups =getLowerGroups(net.getGroups());
        boolean b = false;

        for(ONGroup group : lowerGroups){
            for(Condition c : group.getConditions()){
                if(hasPreConflictEvents(c) || hasPostConflictEvents(c)){
                    try {
                        result.put(group, alg.getReachableMarkings(group));
                    } catch (UnboundedException e) {
                        b = true;
                    }
                    break;
                }
            }
            if(b)
                errMsg("Fail to get phase: occurrence net is unsafe " + net.getNodeReference(group));
        }

        return result;
    }

    /**
     * get the phase collection for all upper-level conditions.
     * @throws UnboundedException
     */
    public Map<Condition, Collection<Phase>> getAllPhases(){
        Map<Condition, Collection<Phase>> result = new HashMap<Condition, Collection<Phase>>();

        //if reachable markings are not provided, get markings.
        Map<ONGroup, List<Marking>> allMarkings = new HashMap<ONGroup, List<Marking>>();

        allMarkings.putAll(getReachableMarking());

        Collection<ONGroup> upperGroups =getUpperGroups(net.getGroups());

        for(ONGroup group : upperGroups){
            for(Condition c : group.getConditions())
                result.put(c, getPhases(c, allMarkings));
        }
        return result;
    }

    public Map<Condition, Collection<Phase>> getAllPhases(Map<ONGroup, List<Marking>> allMarkings){
        Map<Condition, Collection<Phase>> result = new HashMap<Condition, Collection<Phase>>();

        if(allMarkings == null)
            return getAllPhases();

        Collection<ONGroup> upperGroups =getUpperGroups(net.getGroups());

        for(ONGroup group : upperGroups){
            for(Condition c : group.getConditions())
                result.put(c, getPhases(c, allMarkings));
        }
        return result;
    }

    protected void errMsg(String msg){
        JOptionPane.showMessageDialog(null, msg, "Fail to get phase", JOptionPane.ERROR_MESSAGE);
    }

//    public Map<Condition, Collection<Phase>> getAllPhases(){
//        try {
//            return getAllPhases(null);
//        } catch (UnboundedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }

    private Collection<Condition> forwardSearch(Node node){
        Collection<Condition> result = new HashSet<Condition>();
        Stack<Node> stack = new Stack<Node>();
        Collection<Node> visit = new HashSet<Node>();

        stack.push(node);

        while(!stack.isEmpty()){
            node = stack.pop();
            visit.add(node);

            if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
                result.addAll(getPostBhvSet((Condition)node));
            }else{
                Collection<Node> postSet = getPostPNSet(node);
                if(!postSet.isEmpty()){
                    for(Node post : postSet){
                        if(!visit.contains(post)){
                            stack.push(post);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Collection<Condition> backWardSearch(Node node){
        Collection<Condition> result = new HashSet<Condition>();
        Stack<Node> stack = new Stack<Node>();
        Collection<Node> visit = new HashSet<Node>();

        stack.push(node);

        while(!stack.isEmpty()){
            node = stack.pop();
            visit.add(node);

            if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
                result.addAll(getPostBhvSet((Condition)node));
            }else{
                Collection<Node> preSet = getPrePNSet(node);
                if(!preSet.isEmpty()){
                    for(Node pre : preSet){
                        if(!visit.contains(pre)){
                            stack.push(pre);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * get the set of corresponding upper-level conditions for a given node
     */
    public Collection<Condition> getUpperConditions(Node node){
        Collection<Condition> result = new HashSet<Condition>();

        if(isUpperNode(node)){
            if(node instanceof Condition)
                result.add((Condition)node);
            else
                return result;
        }

        Collection<Condition> min = backWardSearch(node);
        Collection<Condition> max = forwardSearch(node);

        for(Condition c : max){
            if(min.contains(c))
                result.add(c);
        }
        return result;
    }

    public Collection<Condition> getUpperConditions(Collection<? extends Node> nodes){
        Collection<Condition> result = new HashSet<Condition>();

        for(Node node : nodes){
            result.addAll(getUpperConditions(node));
        }

        return result;
    }

    /**
     * return true if the given node is an upper-level condition.
     */
    public boolean isUpperCondition(Node node){
        if((node instanceof Condition)
                && !(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
                &&(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)))
            return true;

        return false;
    }

    /**
     * return true if the given node is in upper-level group.
     */
    public boolean isUpperNode(Node node){
        if(isUpperCondition(node))
            return true;
        for(Node pre : getPrePNSet(node))
            if(isUpperCondition(pre))
                return true;

        return false;
    }

    /**
     * get lower-level group for a set of phase bounds
     */
    public ONGroup getLowerGroup(Phase phase){
        if(phase.isEmpty())
            return null;

        return net.getGroup(phase.iterator().next());
    }

    /**
     * get all lower-level groups for a given upper-level group
     */
    public Collection<ONGroup> getLowerGroups(ONGroup upperGroup){
        Collection<ONGroup> result = new HashSet<ONGroup>();

        for(Condition c : upperGroup.getConditions()){
            result.addAll(getLowerGroups(c));
        }

        return result;
    }

    /**
     * get all lower-level groups for a given upper-level condition;
     *
     */
    public Collection<ONGroup> getLowerGroups(Condition upperCondition){
        Collection<ONGroup> result = new HashSet<ONGroup>();

        for(Node pre : getPreBhvSet(upperCondition)){
            result.add(net.getGroup(pre));
        }

        return result;
    }


    /**
     * get lower-level groups for a given group set.
     */
    public Collection<ONGroup> getLowerGroups(Collection<ONGroup> groups){
        Collection<ONGroup> result = new HashSet<ONGroup>();
        for(ONGroup group : groups){
            boolean isInput = false;
            boolean isOutput = false;
            for(Node node : group.getComponents()){
                if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
                    isInput = true;
                if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
                    isOutput = true;
            }
            if(!isInput && isOutput)
                result.add(group);

        }
        return result;
    }


    /**
     * get upper-level groups for a given group set.
     */
    public Collection<ONGroup> getUpperGroups(Collection<ONGroup> groups){
        Collection<ONGroup> result = new HashSet<ONGroup>();
        for(ONGroup group : groups){
            boolean isInput = false;
            boolean isOutput = false;
            if(this.isLineLikeGroup(group)){
                for(Node node : group.getComponents()){
                    if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
                        isInput = true;
                    if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
                        isOutput = true;
                }
                if(isInput && !isOutput)
                    result.add(group);
            }
        }
        return result;
    }

    /**
     * get minimal phase for a given phase
     */
    public ArrayList<Condition> getMinimalPhase(Phase phase){
        ArrayList<Condition> result = new ArrayList<Condition>();
        for(Condition c : phase){
            boolean isMinimal = true;
            for(Condition pre : this.getPrePNCondition(c)){
                if(phase.contains(pre)){
                    isMinimal = false;
                    break;
                }
            }
            if(isMinimal)
                result.add(c);
        }
        return result;
    }

    /**
     * get minimal phase collection for a set of phase
     */
    public ArrayList<Condition> getMinimalPhase(Collection<Phase> phases){
        ArrayList<Condition> result = new ArrayList<Condition>();
        for(Phase phase : phases){
            result.addAll(getMinimalPhase(phase));
        }
        return result;
    }

    /**
     * get maximal phase for a given phase
     */
    public ArrayList<Condition> getMaximalPhase(Phase phase){
        ArrayList<Condition> result = new ArrayList<Condition>();
        for(Condition c : phase){
            boolean isMaximal = true;
            for(Condition pre : this.getPostPNCondition(c)){
                if(phase.contains(pre)){
                    isMaximal = false;
                    break;
                }
            }
            if(isMaximal)
                result.add(c);
        }
        return result;
    }

    /**
     * get maximal phase collection for a set of phase
     */
    public ArrayList<Condition> getMaximalPhase(Collection<Phase> phases){
        ArrayList<Condition> result = new ArrayList<Condition>();
        for(Phase phase : phases){
            result.addAll(getMaximalPhase(phase));
        }
        return result;
    }

    /**
     * return true if a transitionNode is in upper-level group
     */
    public boolean isUpperEvent(TransitionNode n){
        if(getPrePNSet(n).size() == 1){
            Condition c = (Condition)getPrePNSet(n).iterator().next();
            if(net.getInputSONConnectionTypes(c).contains(Semantics.BHVLINE)
                    && !net.getOutputSONConnectionTypes(c).contains(Semantics.BHVLINE)){
                return true;
            }else
                return false;
        }else
            return false;
    }

    /**
     * get before(e) relation for a given upper-level transition node
     */
    public Before before(TransitionNode e, Map<Condition, Collection<Phase>> phases){
        Before result = new Before();

        Collection<Condition> PRE = getPREset(e);
        Collection<Condition> POST = getPOSTset(e);

        //get Pre(e)
        for(Condition c : PRE){
            //get phase collection for each Pre(e)
            Collection<Phase> prePhases = null;

            if(phases.containsKey(c)){
                prePhases = phases.get(c);
            }else{
                return result;
            }

            //get maximal phase
            for(Phase phase : prePhases){
                Collection<Condition> max = getMaximalPhase(phase);
                for(Condition c1 : max){
                    //get pre(c1)
                    Collection<Node> pre = getPrePNSet(c1);
                    for(Node e1 : pre){
                        if(e1 instanceof TransitionNode){
                            TransitionNode[] subResult = new TransitionNode[2];
                            subResult[0] = (TransitionNode)e1;
                            subResult[1] = (TransitionNode)e;
                            result.add(subResult);
                        }
                    }
                }
            }
        }

        //get Post(e)
        for(Condition c : POST){
            //get phase collection for each Pre(e)
            Collection<Phase> postPhases =  null;
            if(phases.containsKey(c)){
                postPhases = phases.get(c);
            }else{
                return result;
            }

            //get minimal phase
            for(Phase phase : postPhases){
                Collection<Condition> min = getMinimalPhase(phase);
                for(Condition c1 : min){
                    //get pre(c1)
                    Collection<Node> post = getPostPNSet(c1);
                    for(Node e1 : post){
                        if(e1 instanceof TransitionNode){
                            TransitionNode[] subResult = new TransitionNode[2];
                            subResult[0] = (TransitionNode)e;
                            subResult[1] = (TransitionNode)e1;
                            result.add(subResult);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Map<TransitionNode, Before> getBeforeMap(){
        Map<TransitionNode, Before> result = new HashMap<TransitionNode, Before>();

        Map<Condition, Collection<Phase>> phases = getAllPhases();
        Collection<ONGroup> upperGroups = getUpperGroups(net.getGroups());

        for(ONGroup group : upperGroups)
            for(TransitionNode e : group.getTransitionNodes()){
                result.put(e, before(e, phases));
        }

        return result;
    }

    public Before getBeforeList(){
        Before result = new Before();

        Map<Condition, Collection<Phase>> phases = getAllPhases();
        Collection<ONGroup> upperGroups = getUpperGroups(net.getGroups());

        for(ONGroup group : upperGroups){
            for(TransitionNode e : group.getTransitionNodes()){
                result.addAll(before(e, phases));
            }
        }
        return result;
    }

    public Map<Condition, String> getPhaseCutErr() {
        return phaseCutErr;
    }
}
