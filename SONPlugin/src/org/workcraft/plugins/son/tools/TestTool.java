package org.workcraft.plugins.son.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.ASONAlg;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.DFSEstimationAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.Marking;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool extends AbstractTool implements Tool {

    private String message = "";

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, SON.class);

    }

    public String getSection() {
        return "test";
    }

    public String getDisplayName() {
        return "Test";
    }

    GraphEditor editor1;

    public void run(WorkspaceEntry we) {
        System.out.println("================================================================================");
        SON net = (SON) we.getModelEntry().getMathModel();
        VisualSON vnet = (VisualSON) we.getModelEntry().getVisualModel();
        //reachableMarkingsTest(net);
        esitmationTest(net);
        //timeTest(net);
        //bhvTimeTest(net);
        //getScenario(net);

        //dfsTest(net);
        //outputBefore(net);
        //phaseTest(net);
        //csonCycleTest(net);
        //abtreactConditionTest(net);
        //GUI.drawEditorMessage(editor, g, Color.red, "sfasdfadsfa");
        //syncCycleTest(net);
        //blockMathLevelTest(net, vnet);
        //mathLevelTest(net, vnet);
        //connectionTypeTest(net, vnet);
        //this.convertBlockTest(net, vnet);
        //relation(net, vnet);
        //conditionOutputTest(vnet);
    }

    private void reachableMarkingsTest(SON net) {
        ASONAlg alg = new ASONAlg(net);
        for (ONGroup group : net.getGroups()) {
            try {
                Collection<Marking> markings = alg.getReachableMarkings(group);
                for (Marking marking : markings) {
                    System.out.println();
                    for (Node node : marking) {
                        System.out.print(net.getNodeReference(node) + ", ");
                    }
                }
            } catch (UnboundedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void esitmationTest(SON net) {
        //DFSEstimationAlg timeAlg = new DFSEstimationAlg(net, new Interval(0, 0), Granularity.YEAR_YEAR, null);
        BSONAlg bsonAlg = new BSONAlg(net);

//        try {
//            //timeAlg.entireEst();
//        } catch (AlternativeStructureException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        System.out.println(net.getConditions().size() + " " + net.getSONConnections().size());
//        try {
//            Interval result = timeAlg.EstimateEndTime(bsonAlg.getInitial(net.getComponents()).iterator().next(), null);
//              System.out.println("result" + result);
//        } catch (InconsistentTimeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (TimeOutOfBoundsException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

    }

/*    private void timeTest(SON net) {
        TimeAlg timeAlg = new TimeAlg(net);

        for (Node node : net.getComponents()) {
            System.out.println(net.getNodeReference(node));
            try {
                for (String str : timeAlg.onConsistecy(node)) {
                    System.out.println(str);
                }
            } catch (InvalidStructureException e) {
                System.out.println("Structure error");
            }
        }
    }*/

/*    private void bhvTimeTest(SON net) {
        BSONAlg bsonAlg = new BSONAlg(net);

        Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());
        Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());
        Map<Condition, Collection<Phase>> phases = bsonAlg.getAllPhases();

        TimeAlg timeAlg = new TimeAlg(net);

        for (ONGroup group : upperGroups) {
            for (TransitionNode t : group.getTransitionNodes()) {
                System.out.println(net.getNodeReference(t));
                for (String str : timeAlg.bsonConsistency(t, phases)) {
                    System.out.println(str);
                }
            }
        }

        for (ONGroup group : lowerGroups) {
            for (Condition c : group.getConditions()) {
                if (net.getInputPNConnections(c).isEmpty()) {
                    System.out.println("ini: " + net.getNodeReference(c));
                    for (String str : timeAlg.bsonConsistency2(c)) {
                        System.out.println(str);
                    }
                }

                if (net.getOutputPNConnections(c).isEmpty()) {
                    System.out.println("fine: " + net.getNodeReference(c));
                    for (String str : timeAlg.bsonConsistency3(c)) {
                        System.out.println(str);
                    }
                }

            }
        }
    }*/

    protected Collection<ChannelPlace> getSyncCPs(SON net) {
        Collection<ChannelPlace> result = new HashSet<>();
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        for (Path path : cycleAlg.syncCycleTask(nodes)) {
            for (Node node : path) {
                if (node instanceof ChannelPlace) {
                    result.add((ChannelPlace) node);
                }
            }
        }
        return result;
    }

    private void getScenario(SON net) {
        ScenarioGeneratorTool s = new ScenarioGeneratorTool();
    }

    private void dfsTest(SON net) {
        PathAlgorithm alg = new PathAlgorithm(net);
        RelationAlgorithm alg2 = new RelationAlgorithm(net);
        ONGroup g = net.getGroups().iterator().next();
        Collection<Path> result = alg.getPaths(alg2.getONInitial(g).iterator().next(),
                alg2.getONFinal(g).iterator().next(),
                net.getGroups().iterator().next().getComponents());

        for (Path path : result) {
            System.out.println(path.toString(net));
        }
    }

    private void outputBefore(SON net) {

        BSONAlg bsonAlg = new BSONAlg(net);
        System.out.println("\nOutput before(e):");
        Collection<TransitionNode[]> before = new ArrayList<>();

        Collection<ONGroup> groups = bsonAlg.getUpperGroups(net.getGroups());
        Collection<TransitionNode> set = new HashSet<>();
        for (ONGroup group : groups) {
            set.addAll(group.getTransitionNodes());
        }

        for (TransitionNode e : set) {
            //before =  bsonAlg.before(e);
            if (!before.isEmpty()) {
                Collection<String> subResult = new ArrayList<>();
                System.out.println("before(" + net.getComponentLabel(e) + "): ");
                for (TransitionNode[] t : before) {
                    subResult.add("(" + net.getComponentLabel(t[0]) + " " + net.getComponentLabel(t[1]) + ")");
                }
                System.out.println(subResult);
            }
        }

    }

    @Override
    public void drawInScreenSpace(final GraphEditor editor, Graphics2D g) {
        System.out.println("editor1111111");
        int a = 0;
        if (a == 0) {
            GUI.drawEditorMessage(editor, g, Color.BLACK, "afdasfasd");
        }
    }

    private void relation(SON net, VisualSON vnet) {
        for (Node node : net.getComponents()) {
            System.out.println("node name: " + net.getName(node) + "  node pre size:" + net.getPreset(node).size()
                    + "  node post size:" + net.getPostset(node).size());
        }
    }

    private void phaseTest(SON net) {
        BSONAlg alg = new BSONAlg(net);

        System.out.println("phase test");
        for (Condition c : alg.getAllPhases().keySet()) {
            System.out.println("condition = " + net.getNodeReference(c));

            for (Phase phase : alg.getAllPhases().get(c)) {
                System.out.println("phase = " + phase.toString(net));
            }

        }
    }

    private void syncCycleTest(SON net) {
        CSONCycleAlg csonPath = new CSONCycleAlg(net);
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getChannelPlaces());
        nodes.addAll(net.getTransitionNodes());

        for (Path path : csonPath.syncEventCycleTask(nodes)) {
            System.out.println(path.toString(net));
        }
    }

    private void csonCycleTest(SON net) {
        CSONCycleAlg csonPath = new CSONCycleAlg(net);

        for (Path path : csonPath.cycleTask(net.getComponents())) {
            System.out.println(path.toString(net));
        }
    }

    private void exceptionTest() throws InvalidConnectionException {
        boolean a = true;
        if (a) {
            message = "adfa";
            throw new InvalidConnectionException(message);
        }
    }

    private void abtreactConditionTest(SON net) {
        BSONAlg alg = new BSONAlg(net);
        for (Node node : net.getComponents()) {
            for (Condition c : alg.getUpperConditions(node)) {
                System.out.println("abstract condition of   " + net.getNodeReference(node) + "  is  "  + net.getNodeReference(c));
            }
        }
        System.out.println("********************");
    }

/*    private void convertBlockTest(SONModel net, VisualSON vnet) {
        for (Node node : net.getSONConnections()) {
            System.out.println("before " + net.getName(node) + " parent " + node.getParent().toString() + " type = " + ((SONConnection) node).getType());
    }
            vnet.connectToBlocks();
            System.out.println("node size =" + net.getComponents().size());
            for (Node node : net.getSONConnections()) {
                    System.out.println("after " + net.getName(node) + " parent " + node.getParent().toString() + " type = " + ((SONConnection) node).getType());
            }
    }
    */

    private void blockMathLevelTest(SON net, VisualSON vnet) {
        for (Block block : net.getBlocks()) {
            System.out.println("block name :" + net.getName(block));
            System.out.println("connection size : " + block.getSONConnections().size());
        }

/*        for (VisualBlock block : vnet.getVisualBlocks()) {
            System.out.println("visual block name :" + vnet.getName(block));
            System.out.
            println("visual connection size : " + block.getVisualSONConnections().size());
        }*/

    }

    private void mathLevelTest(SON net, VisualSON vnet) {
        for (ONGroup group: net.getGroups()) {
            System.out.println(group.toString());
            System.out.println("Page size = " + group.getPageNodes().size());
            System.out.println("block size = " + group.getBlocks().size());
            System.out.println("Condition size = " + group.getConditions().size());
            System.out.println("Event size = " + group.getEvents().size());
            System.out.println("Connection size = " + group.getSONConnections().size());
            System.out.println();
        }

/*        for (PageNode page : net.getPageNodes()) {
            System.out.println("page parent  " + page.getParent().toString());
        }
        */
/*        for (VisualONGroup vgroup: vnet.getVisualONGroups()) {
            System.out.println(vgroup.toString());
            System.out.println("Visual Page size = " + vgroup.getVisualPages().size());
            System.out.println("Visual Condition size = " + vgroup.getVisualConditions().size());
            System.out.println("Visual Connection size = " + vgroup.getVisualSONConnections().size());
            System.out.println("Visual block size = " + vgroup.getVisualBlocks().size());

        }*/

/*        for (VisualPage page : vnet.getVisualPages()) {
            System.out.println();
            System.out.println("visual page parent  " + page.getParent().toString());
        }*/
    }

    private void connectionTypeTest(SON net, VisualSON vnet) {
        for (SONConnection con : net.getSONConnections()) {
            System.out.println("con type " + con.getSemantics());
            System.out.println("con fisrt " + con.getFirst());
            System.out.println("con fisrt " + con.getSecond());
        }
        for (VisualSONConnection con : vnet.getVisualSONConnections()) {
            System.out.println("con type " + con.getSemantics());
            System.out.println("con fisrt " + con.getFirst());
            System.out.println("con fisrt " + con.getSecond());
        }
    }

    @Override
    public Decorator getDecorator(GraphEditor editor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }
}

