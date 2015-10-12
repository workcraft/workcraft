package org.workcraft.plugins.cpog;

public class PnToCpogSettings {

	private boolean reduce;
	private boolean isomorphism;
	private boolean removeNodes;
	private int significance;

	public enum SignificanceCheckType {
		EXHAUSTIVE,
		HASHMAP_BASED,
		TREE_OF_RUNS
	}

	public PnToCpogSettings(){
		this.reduce = false;
		this.isomorphism = false;
		this.removeNodes = false;
		this.significance = 0;
	}

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		this.reduce = reduce;
	}

	public boolean isIsomorphism() {
		return isomorphism;
	}

	public void setIsomorphism(boolean isomorphism) {
		this.isomorphism = isomorphism;
	}

	public int getSignificance() {
		return significance;
	}

	public void setSignificance(int significance) {
		this.significance = significance;
	}

	public void setSignificance(SignificanceCheckType type){
		switch(type){
			case EXHAUSTIVE:
				significance = 0;
				break;
			case HASHMAP_BASED:
				significance = 1;
				break;
			case TREE_OF_RUNS:
				significance = 2;
				break;
		}
	}

	public boolean isRemoveNodes() {
		return removeNodes;
	}

	public void setRemoveNodes(boolean removeNodes) {
		this.removeNodes = removeNodes;
	}

}
