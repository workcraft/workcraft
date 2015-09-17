package org.workcraft.plugins.son;

@SuppressWarnings("serial")
public class StepRef extends NodeRefs{

	public boolean isReverse(){
		if(this.iterator().next() == ">")
			return false;
		else
			return true;
	}
}
