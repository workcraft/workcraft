package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.ONPathAlg;
import org.workcraft.plugins.son.algorithm.TSONAlg;
import org.workcraft.plugins.son.elements.Condition;

public class TSONStructureTask implements SONStructureVerification{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private TSONAlg tsonAlg;
	private ONPathAlg onPathAlg;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public TSONStructureTask(SONModel net){
		this.net = net;
		tsonAlg  = new TSONAlg(net);
		onPathAlg = new ONPathAlg(net);
	}

	@Override
	public void task(Collection<ONGroup> selectedGroups) {

		logger.info("-----------------Temporal-SON Verification-----------------");

		Collection<Block> blocks = new ArrayList<Block>();
		for(ONGroup group : selectedGroups){
			blocks.addAll(group.getBlock());
			for(Node node : validBlockTask(blocks)){
				System.out.println("Node name:");
				System.out.println(net.getName(node));
			}

		}

	}

	private Collection<Node> validBlockTask(Collection<Block> blocks){
		Collection<Node> result = new ArrayList<Node>();

		for(Block block : blocks){
			if(onPathAlg.cycleTask(block.getComponents()).isEmpty())
				for(Condition input : tsonAlg.getBlockInputs(block)){
					System.out.println("task --- input+  "+ net.getName(input));
					if(!tsonAlg.isCausallyPrecede(input, tsonAlg.getBlockOutputs(block), block)){
						result.add(input);
					}
				}
		}
		System.out.println("task ---  result"+ result.size());
		return result;
	}

	@Override
	public void errNodesHighlight() {

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
