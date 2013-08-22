package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;

public class CSONCycleAlg extends GroupCycleAlg{

	private SONModel net;

	public CSONCycleAlg(SONModel net) {
		super(net);
		this.net = net;
	}

	@Override
	public List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n))
				if(nodes.contains(next)){
					Node[] adjoin = new Node[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);

					for (String conType :  net.getSONConnectionsTypes(n, next)){
						if(conType == "SYNCLINE"){
							Node[] reAdjoin = new Node[2];
							reAdjoin[0] = next;
							reAdjoin[1] = n;
							if(!result.contains(reAdjoin))
								result.add(reAdjoin);
					}
				}
			}
		}
		return result;
	}

}
