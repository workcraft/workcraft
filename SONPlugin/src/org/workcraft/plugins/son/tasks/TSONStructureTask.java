package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.ONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.TSONAlg;
import org.workcraft.plugins.son.elements.Block;

public class TSONStructureTask extends AbstractStructuralVerification{

    private SON net;

    private Collection<Node> relationErrors = new HashSet<Node>();
    private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();
    private Collection<Path> cycleErrors = new ArrayList<Path>();

    private TSONAlg tsonAlg;
    private ONCycleAlg onCycleAlg;

    private int errNumber = 0;
    private int warningNumber = 0;

    public TSONStructureTask(SON net){
        super(net);
        this.net = net;

        onCycleAlg = new ONCycleAlg(net);
        tsonAlg = new TSONAlg(net);
    }

    @Override
    public void task(Collection<ONGroup> groups) {

        infoMsg("---------------------Temporal-SON Structure Verification---------------------");

        //group info
        infoMsg("Initialising selected groups and components...");

        Collection<Block> blocks = new ArrayList<Block>();

        for(ONGroup cGroup : groups)
            blocks.addAll(cGroup.getBlocks());

        infoMsg("Selected Groups : " +  net.toString(groups));
        infoMsg("Collapsed Blocks : " + net.toString(blocks));

        if(blocks.isEmpty()){
            infoMsg("Task terminated: no blocks in selected groups.");
            return;
        }

        for(Block block : blocks){
            infoMsg("Initialising block..." + net.getNodeReference(block));
            Collection<Node> inputs = getTSONAlg().getBlockInputs(block);
            Collection<Node> outputs = getTSONAlg().getBlockOutputs(block);

            infoMsg("Block inputs: "+ net.toString(inputs));
            infoMsg("Block outputs: "+ net.toString(outputs));

        //Causally Precede task result
            infoMsg("Running block structure tasks...");
            if(onCycleAlg.cycleTask(block.getComponents()).isEmpty()){
                Collection<Node> result3 = causallyPrecedeTask(block);
                if(!result3.isEmpty()){
                    relationErrors.addAll(result3);
                    relationErrors.add(block);
                    errNumber = errNumber + result3.size();
                    for(Node node : result3)
                        errMsg("ERROR : Invalid causally relation, the input does not causally precede all block outputs.",node);
                }else
                    infoMsg("Valid causal relation between block inputs and outputs.");
            }else{
                warningNumber++;
                infoMsg("Warning : Block contians cycle path, cannot run causal relation task.", block);
            }
        }

        //block connection task result


        infoMsg("Block structure checking tasks complete.");

    }

    //Check all inputs of a block causally precede all outputs of an un-collapsed block
    //Warning: run cycle check before
    private Collection<Node> causallyPrecedeTask(Block block){
        Collection<Node> result = new ArrayList<Node>();
        for(Node input : getTSONAlg().getBlockPNInputs(block)){
            if(!getTSONAlg().isCausallyPrecede(input, getTSONAlg().getBlockPNOutputs(block)))
                result.add(input);
        }
        return result;
    }

    public TSONAlg getTSONAlg(){
        return tsonAlg;
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

    @Override
    public int getErrNumber() {
        return errNumber;
    }

    @Override
    public int getWarningNumber() {
        return warningNumber;
    }
}
