package org.workcraft.plugins.son.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.IncompatibleScenarioException;

@SuppressWarnings("serial")
public class ScenarioRef extends ArrayList<String>{

    public Collection<Node> getNodes(SON net){
        Collection<Node> result = new HashSet<Node>();

        for(String ref : this){
            Node node = net.getNodeByReference(ref);
            if((node instanceof PlaceNode) || (node instanceof TransitionNode))
                result.add(node);
        }
        return result;
    }

    public Collection<String> getNodeRefs(SON net){
        Collection<String> result = new HashSet<String>();
        for(String ref : this){
            Node node = net.getNodeByReference(ref);
            if((node instanceof PlaceNode) || (node instanceof TransitionNode))
                result.add(ref);
        }
        return result;
    }

    public boolean isNodeRef(String str, SON net){
        Node node = net.getNodeByReference(str);
        if((node instanceof PlaceNode) || (node instanceof TransitionNode))
            return true;
        return false;
    }

    public Collection<SONConnection> getConnections(SON net){
        Collection<SONConnection> result = new HashSet<SONConnection>();
        for(String ref : this){
            Node node = net.getNodeByReference(ref);
            if(node instanceof SONConnection)
                result.add((SONConnection) node);
        }
        return result;
    }

    public Collection<SONConnection> getRuntimeConnections(SON net){
        Collection<SONConnection> result = new HashSet<SONConnection>();
        Collection<Node> nodes = getNodes(net);
        for(Node node : nodes){
            Collection<SONConnection> connections = net.getSONConnections(node);
            for(SONConnection con : connections){
                if(con.getFirst() != node && nodes.contains(con.getFirst())){
                    result.add(con);
                }else if(con.getSecond() != node && nodes.contains(con.getSecond())){
                    result.add(con);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "";
        for (String s: this) {
            if (result != "") {
                result += ", ";
            }
            result += s;
        }
        return result;
    }

    public void fromString(String str, SON net) throws IncompatibleScenarioException{
        clear();
        for (String s: str.split("\\s*,\\s*")) {
            if(net.getNodeByReference(s) != null){
                add(s);
            }else{
                throw new IncompatibleScenarioException();
            }
        }
    }
}
