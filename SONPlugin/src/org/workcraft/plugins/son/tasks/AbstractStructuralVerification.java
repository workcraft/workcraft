package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.BSONCycleAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.ONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.TSONAlg;

abstract class AbstractStructuralVerification implements StructuralVerification{

	private SON net;

	private RelationAlgorithm relationAlg;
	private CSONCycleAlg csonPathAlg;
	private BSONAlg bsonAlg;
	private BSONCycleAlg bsonPathAlg;
	private ONCycleAlg onPathAlg;
	private TSONAlg tsonAlg;

	public AbstractStructuralVerification(SON net){
		this.net = net;
		relationAlg = new RelationAlgorithm(net);
		csonPathAlg = new CSONCycleAlg(net);
		bsonAlg = new BSONAlg(net);
		bsonPathAlg = new BSONCycleAlg(net);
		onPathAlg = new ONCycleAlg(net);
		tsonAlg = new TSONAlg(net);

	}

	public abstract void task(Collection<ONGroup> groups);

	public Collection<String> getRelationErrorsSetReferences(Collection<Node> set){
		Collection<String> result = new ArrayList<String>();
		for(Node node : set)
			result.add(net.getNodeReference(node));
		return result;
	}

	public Collection<String> getGroupErrorsSetReferences(Collection<ONGroup> set){
		Collection<String> result = new ArrayList<String>();
		for(ONGroup node : set)
			result.add(net.getNodeReference(node));
		return result;
	}

	public Collection<ArrayList<String>> getcycleErrorsSetReferences(Collection<Path> set){
		Collection<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for(Path path : set){
			ArrayList<String> sPath = new ArrayList<String>();
			for(Node node : path){
				sPath.add(net.getNodeReference(node));
				result.add(sPath);
			}
		}
		return result;
	}

	public RelationAlgorithm getRelationAlg(){
		return this.relationAlg;
	}

	public BSONAlg getBSONAlg(){
		return this.bsonAlg;
	}

	public BSONCycleAlg getBSONPathAlg(){
		return bsonPathAlg;
	}

	public CSONCycleAlg getCSONPathAlg(){
		return csonPathAlg;
	}

	public ONCycleAlg getPathAlg(){
		return onPathAlg;
	}

	public TSONAlg getTSONAlg(){
		return tsonAlg;
	}
}
