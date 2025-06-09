package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;

@SuppressWarnings("PMD.GuardLogStatement")
abstract class AbstractStructuralVerification implements StructuralVerification {

    private final SON net;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final RelationAlgorithm relationAlg;

    AbstractStructuralVerification(SON net) {
        this.net = net;

        relationAlg = new RelationAlgorithm(net);
    }

    @Override
    public abstract void task(Collection<ONGroup> groups);

    public Collection<String> getRelationErrorsSetRefs(Collection<Node> set) {
        Collection<String> result = new ArrayList<>();
        for (Node node : set) {
            result.add(net.getNodeReference(node));
        }
        return result;
    }

    public Collection<String> getGroupErrorsSetRefs(Collection<ONGroup> set) {
        Collection<String> result = new ArrayList<>();
        for (ONGroup node : set) {
            result.add(net.getNodeReference(node));
        }
        return result;
    }

    public Collection<ArrayList<String>> getCycleErrorsSetRefs(Collection<Path> set) {
        Collection<ArrayList<String>> result = new ArrayList<>();
        for (Path path : set) {
            ArrayList<String> sPath = new ArrayList<>();
            for (Node node : path) {
                sPath.add(net.getNodeReference(node));
                result.add(sPath);
            }
        }
        return result;
    }

    public void infoMsg(String msg) {
        logger.info(msg);
    }

    public void infoMsg(String msg, Node node) {
        logger.info(msg + " [" + net.getNodeReference(node) + "]");
    }

    public void errMsg(String msg) {
        logger.info("[ERROR] " + msg);
    }

    public void errMsg(String msg, Node node) {
        logger.info("[ERROR] " + msg + " [" + net.getNodeReference(node) + "]");
    }

    public RelationAlgorithm getRelationAlg() {
        return this.relationAlg;
    }
}
