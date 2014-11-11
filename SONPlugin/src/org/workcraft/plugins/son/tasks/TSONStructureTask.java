package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.Block;

public class TSONStructureTask extends AbstractStructuralVerification{

	private SON net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> relationErrors = new HashSet<Node>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();
	private Collection<Path> cycleErrors = new ArrayList<Path>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public TSONStructureTask(SON net){
		super(net);
		this.net = net;
	}

	@Override
	public void task(Collection<ONGroup> selectedGroups) {

		logger.info("-----------------Temporal-SON Verification-----------------");

		//group info
		logger.info("Initialising selected group elements...");

		Collection<Block> blocks = new ArrayList<Block>();

		for(ONGroup cGroup : selectedGroups)
			blocks.addAll(cGroup.getBlocks());

		logger.info("Selected Groups = " +  selectedGroups.size());
		logger.info("Collapsed Block size = " + blocks.size());

		if(blocks.isEmpty()){
			logger.info("Task termination: no blocks in selected groups.");
			return;
		}

		for(Block block : blocks){
			logger.info("Initialising block " +net.getName(block)+ " ...");
			Collection<Node> inputs = getTSONAlg().getBlockInputs(block);
			Collection<Node> outputs = getTSONAlg().getBlockOutputs(block);

			Collection<String> inputNames = new ArrayList<String>();
			for(Node node : inputs)
				inputNames.add(" "+net.getName(node) + " ");
			logger.info("inputs = "+ inputNames.toString() + "");

			Collection<String> outputNames = new ArrayList<String>();
			for(Node node : outputs)
				outputNames.add(" "+net.getName(node) + " ");
			logger.info("outputs = "+ outputNames.toString() + " ");

		//Causally Precede task result
			logger.info("Running block structural checking tasks...");
			if(getPathAlg().cycleTask(block.getComponents()).isEmpty()){
				Collection<Node> result3 = CausallyPrecedeTask(block);
				if(!result3.isEmpty()){
					relationErrors.addAll(result3);
					relationErrors.add(block);
					errNumber = errNumber + result3.size();
					for(Node node : result3)
						logger.error("ERROR : Incorrect causally relation, the input node "+
					net.getName(node) + "(" + net.getComponentLabel(node) + ")" +" must causally precede all outputs." );
				}else
					logger.info("Correct causal relation between inputs and outputs.");
			}else{
				warningNumber++;
				logger.info("Warning : Block contians cyclic path, cannot run causally precede task: " + net.getName(block));
			}
		}

		//block connection task result


		logger.info("block structural checking tasks complete.");

	}

	//Check all inputs of a block causally precede all outputs of an un-collapsed block
	//Warning: run cycle check before
	private Collection<Node> CausallyPrecedeTask(Block block){
		Collection<Node> result = new ArrayList<Node>();
		for(Node input : getTSONAlg().getBlockPNInputs(block)){
			if(!getTSONAlg().isCausallyPrecede(input, getTSONAlg().getBlockPNOutputs(block)))
				result.add(input);
		}
		return result;
	}

	@Override
	public Collection<String> getRelationErrors() {
		return getRelationErrorsSetReferences(relationErrors);
	}

	@Override
	public Collection<ArrayList<String>> getCycleErrors() {
		return getcycleErrorsSetReferences(cycleErrors);
	}

	@Override
	public Collection<String> getGroupErrors() {
		return getGroupErrorsSetReferences(groupErrors);
	}

	@Override
	public boolean hasErr() {
		return hasErr;
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
