package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.ASONAlg;
import org.workcraft.plugins.son.algorithm.ONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.Marking;


public class ONStructureTask extends AbstractStructuralVerification{

    private SON net;

    private Collection<Node> relationErrors = new HashSet<Node>();
    private Collection<Path> cycleErrors = new HashSet<Path>();
    private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

    private ONCycleAlg onCycleAlg;
    private ASONAlg asonAlg;

    private Map<ONGroup, List<Marking>> reachableMarkings;

    private int errNumber = 0;
    private int warningNumber = 0;

    public ONStructureTask(SON net){
        super(net);
        this.net = net;

        onCycleAlg = new ONCycleAlg(net);
        asonAlg = new ASONAlg(net);
        reachableMarkings = new HashMap<ONGroup, List<Marking>>();
    }

    public void task(Collection<ONGroup> groups){

        infoMsg("-------------------------Occurrence Net Structure Verification-------------------------");

        ArrayList<Node> components = new ArrayList<Node>();

        for(ONGroup group : groups){
            components.addAll(group.getComponents());
        }

        infoMsg("Selected Groups = " +  groups.size());
        infoMsg("Group Components = " + components.size() + "\n");

        for(ONGroup group : groups){

            Collection<Node> task1, task2;
            Collection<Path> cycleResult;

            //group info
            infoMsg("Initialising selected groups and components...");

            Collection<Node> groupComponents = group.getComponents();
            infoMsg("Group name : ", group);
            infoMsg("Conditions = "+group.getConditions().size()+".\n" +"Events = "+group.getEvents().size()
                    +".\n" + "Collapsed Blocks = " + group.getCollapsedBlocks().size()+".");

            infoMsg("Running component relation tasks...");

            if(!getRelationAlg().hasInitial(groupComponents)){
                errMsg("ERROR : Invalid initial state (no initial state).");
                errNumber++;
                continue;
            }

            if(!getRelationAlg().hasFinal(groupComponents)){
                errMsg("ERROR : Invalid final state (no final state).");
                errNumber++;
                continue;
            }

            //initial state result
            task1 = iniStateTask(groupComponents);

            if (task1.isEmpty())
                infoMsg("Valid occurrence net input.");
            else{
                errNumber = errNumber + task1.size();
                for(Node node : task1){
                    relationErrors.add(node);
                    errMsg("ERROR : Invalid initial state (initial state is not a condition).", node);
                }
            }

            //final state result
            task2 = finalStateTask(groupComponents);
            if (task2.isEmpty())
                infoMsg("Valid occurrence net output.");
            else{
                errNumber = errNumber + task2.size();
                for(Node node : task2){
                    relationErrors.add(node);
                    errMsg("ERROR : Invalid final state (final state is not a condition).", node);
                }
            }

            infoMsg("Component relation tasks complete.");
            //safeness result
            infoMsg("Running safeness checking task...");
            Node node = safenessTask(group);
            if(node != null){
                relationErrors.add(node);
                errNumber++;
            }

            //cycle detection result
            infoMsg("Running cycle detection task...");

            cycleResult = getONCycleAlg().cycleTask(groupComponents);

            cycleErrors.addAll(cycleResult);

            if (cycleResult.isEmpty())
                infoMsg("Occurrence net is cycle free");
            else{
                errNumber++;
                errMsg("ERROR : Occurrence net involves cycle paths = "+ cycleResult.size() + ".");
                int i = 1;
                for(Path cycle : cycleResult){
                    errMsg("Cycle " + i + ": " + cycle.toString(net));
                    i++;
                }
            }
            infoMsg("Cycle detection task complete.\n");
        }
    }

    private Collection<Node> iniStateTask(Collection<Node> groupNodes){
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node node : groupNodes)
            if(node instanceof TransitionNode)
                if(getRelationAlg().isInitial(node))
                    result.add(node);
        return result;
    }

    private Collection<Node> finalStateTask(Collection<Node> groupNodes){
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node node : groupNodes)
            if(node instanceof TransitionNode)
                if(getRelationAlg().isFinal(node))
                    result.add(node);
        return result;
    }

    private Node safenessTask(ONGroup group){
        List<Marking> markings = null;

        try {
            markings =asonAlg.getReachableMarkings(group);
        } catch (UnboundedException e) {
            infoMsg("ERROR : "+e.getMessage());
            return e.getNode();
        }

        if(markings != null)
            reachableMarkings.put(group, markings);
        return null;
    }

    public ONCycleAlg getONCycleAlg(){
        return onCycleAlg;
    }

    @Override
    public Collection<String> getRelationErrors() {
        return getRelationErrorsSetRefs(relationErrors);
    }

    @Override
    public Collection<ArrayList<String>> getCycleErrors() {
        return getCycleErrorsSetRefs(cycleErrors);
    }

    @Override
    public Collection<String> getGroupErrors() {
        return getGroupErrorsSetRefs(groupErrors);
    }

    public Map<ONGroup, List<Marking>> getReachableMarkings() {
        return reachableMarkings;
    }

    @Override
    public int getErrNumber(){
        return this.errNumber;
    }
    @Override
    public int getWarningNumber(){
        return this.warningNumber;
    }

}
