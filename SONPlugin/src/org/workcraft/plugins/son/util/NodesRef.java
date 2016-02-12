package org.workcraft.plugins.son.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class NodesRef extends ArrayList<String>{

    private static final long serialVersionUID = 1L;

    public Collection<Node> getNodes(SON net){
        Collection<Node> result = new HashSet<Node>();

        for(String ref : this){
            Node node = net.getNodeByReference(ref);
            result.add(node);
        }

        return result;
    }

    public boolean containsNode(Node n, SON net){
        if(this.contains(net.getNodeReference(n)))
            return true;
        return false;
    }

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
            } else
                add(s.trim());
            first = false;
        }
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof NodesRef)) return false;

        NodesRef ref = (NodesRef) o;
        if(ref.size() != size()) return false;

        for(String str : ref){
            if(!this.contains(str)){
                return false;
            }
        }
        return true;
    }
}
