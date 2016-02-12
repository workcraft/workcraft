package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class ONCycleAlg{

    private SON net;
    protected RelationAlgorithm relationAlg;

    public ONCycleAlg(SON net) {
        this.net = net;
        relationAlg = new RelationAlgorithm(net);
    }

    /**
     * create Integer Graph for a nodes set
     * G<(a, b, c, d), (<a, b> <b, c> <c, d> <d, b> <b, d>)> would be
     * a = 0; b = 1, c = 2, d = 3
     * result[0] = <1>
     * result[1] = <2, 3>
     * result[2] = <3>
     * result[3] = <1>
     */
    @SuppressWarnings("unchecked")
    protected List<Integer>[] createGraph(List<Node> nodes){
        List<Integer>[] result = new List[nodes.size()];
        LinkedHashMap<Node, Integer> nodeIndex = new LinkedHashMap<Node, Integer>();
        for(int i = 0; i < nodes.size(); i++){
            nodeIndex.put(nodes.get(i), i);
        }
        if(nodes.size() == nodeIndex.size()){
            for(int i = 0; i < nodes.size(); i++){
                int index = nodeIndex.get(nodes.get(i));
                if(result[index] == null){
                    result[index] = new ArrayList<Integer>();
                }
                for(Node post: net.getPostset(nodes.get(index))){
                    if(nodes.contains(post)){
                        result[index].add(nodeIndex.get(post));
                    }
                }
            }
        }else{
            throw new RuntimeException("fail to create graph, input size is not equal to nodeIndex size");
        }
        return result;
    }

    /**
     * Using Tarjan algorithm to get all strongly connected components
     * then convert to cycle path
     */
    public Collection<Path> cycleTask(Collection<? extends Node> nodes){
        List<Path> result = new ArrayList<Path>();

        List<Node> list = new ArrayList<Node>();
        list.addAll(nodes);

        CycleAlgorithm cycleAlg = new CycleAlgorithm();

        for(List<Integer> cycleIndex : cycleAlg.getCycles(createGraph(list))){
            if(cycleIndex.size() > 1){
                Path cycle = new Path();
                for(Integer index : cycleIndex){
                    cycle.add(list.get(index));
                }
                result.add(cycle);
            }
        }

        return result;
    }

    protected Collection<Path> cycleFliter(Collection<Path> paths){
        return paths;
    }
}
