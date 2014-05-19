package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.algorithm.TSONAlg;
import org.workcraft.plugins.son.elements.Condition;

public class TSONStructureTask implements SONStructureVerification{

	private SONModel net;
	private VisualSON vnet;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private TSONAlg tsonAlg;
	private PathAlgorithm onPathAlg;

	private Collection<Block> errBlocks = new HashSet<Block>();
	private Collection<Node> errNodes = new HashSet<Node>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public TSONStructureTask(SONModel net, VisualSON vnet){
		this.net = net;
		this.vnet = vnet;
		tsonAlg  = new TSONAlg(net);
		onPathAlg = new PathAlgorithm(net);
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

		for(Block block : blocks){
			logger.info("Initialising block " +net.getName(block)+ " ...");
			Collection<Node> inputs = tsonAlg.getBlockInputs(block);
			Collection<Node> outputs = tsonAlg.getBlockOutputs(block);

			Collection<String> inputNames = new ArrayList<String>();
			for(Node node : inputs)
				inputNames.add(" "+net.getName(node) + " ");
			logger.info(" inputs = "+ inputNames.toString() + "");

			Collection<String> outputNames = new ArrayList<String>();
			for(Node node : outputs)
				outputNames.add(" "+net.getName(node) + " ");
			logger.info(" outputs = "+ outputNames.toString() + " ");

/*		//interface task result
			Collection<Node> result = interfaceTask(block);
			Collection<Node> result2 = phaseTask(block);
			if(!result.isEmpty()){
				errNodes.addAll(result);
				errBlocks.add(block);
				errNumber = errNumber + result.size();
				for(Node node : result)
					logger.error("ERROR : Incorrect interface, the input/output must be condition: "
				+ net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
			}else if(!result2.isEmpty()){
				errNodes.addAll(result2);
				errBlocks.add(block);
				errNumber = errNumber + result2.size();
				for(Node node : result2)
					logger.error("ERROR : Incorrect interface, block cannot cross phases: "
				+ net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
			}
			else
				logger.info("Correct block interface relation");*/

		//Causally Precede task result
			logger.info("Running block structural checking tasks...");
			vnet.connectToBlocksInside();
			if(onPathAlg.cycleTask(block.getComponents()).isEmpty()){
				Collection<Node> result3 = CausallyPrecedeTask(block);
				if(!result3.isEmpty()){
					errNodes.addAll(result3);
					errBlocks.add(block);
					errNumber = errNumber + result3.size();
					for(Node node : result3)
						logger.error("ERROR : Incorrect causally relation, the input node "+
					net.getName(node) + "(" + net.getComponentLabel(node) + ")" +" must causally precede all outputs" );
				}else
					logger.info("Correct causal relation between inputs and outputs");
			}else{
				warningNumber++;
				logger.info("Warning : Block contians cyclic path, cannot run causally precede task: " + net.getName(block));
			}
			logger.info("block structural checking tasks complete");
			vnet.connectToBlocks();
		}
	}

//dynamic checking in VisualSON
/*	//Static check all inputs and outputs of a block
	private Collection<Node> interfaceTask(Block block){
		Collection<Node> result = new ArrayList<Node>();

		for(Node node : tsonAlg.getBlockPNInputs(block)){
			if(!(node instanceof Condition))
				result.add(node);
		}

		for(Node node : tsonAlg.getBlockPNOutputs(block)){
			if(!(node instanceof Condition))
				result.add(node);
		}
		return result;
	}

	private Collection<Node> phaseTask(Block block){
		Collection<Node> result = new ArrayList<Node>();
		for(Node node : block.getComponents()){
			if(node instanceof Condition && net.getSONConnectionTypes(node).contains("BHVLINE"))
				result.add(node);
		}
		return result;
	}*/

	//Check all inputs of a block causally precede all outputs of an un-collapsed block
	//Warning: run cycle check before
	private Collection<Node> CausallyPrecedeTask(Block block){
		Collection<Node> result = new ArrayList<Node>();
		if(!block.getIsCollapsed())
			for(Node input : tsonAlg.getBlockPNInputs(block)){
				if(!tsonAlg.isCausallyPrecede(input, tsonAlg.getBlockPNOutputs(block))){
					result.add(input);
				}
			}
		return result;
	}

	@Override
	public void errNodesHighlight() {
		for(Node node : this.errNodes){
			this.net.setFillColor(node, SONSettings.getRelationErrColor());
		}
		for(Block block : this.errBlocks){
			if(block.getIsCollapsed())
				this.net.setFillColor(block, SONSettings.getRelationErrColor());
		}
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
