package org.workcraft.plugins.son;

import java.util.ArrayList;

public class NodesRef extends ArrayList<String>{

	private static final long serialVersionUID = 1L;

	public String toString() {
		StringBuffer result = new StringBuffer("");
		// step
		for(int i = 0; i < this.size(); i++){
			if(i==0 || i==1){
				result.append(' ');
				result.append(this.get(i));
			}else{
				result.append(',');
				result.append(' ');
				result.append(this.get(i));
			}
		}
		return result.toString();
	}

	public void fromString(String str) {
		clear();
		for (String s : str.trim().split(" ")) {
			if(s == "<" || s == ">"){
				add(s);
				break;
			}
		}
		boolean first = true;
		for (String s : str.trim().split(",")) {
			if(first){
				for (String fir : s.trim().split(" ")){
					if(fir != "<" || fir != ">"){
						add(fir);
					}
				}
			}
			else
				add(s.trim());
			first = false;
		}
	}
}
