package org.workcraft.plugins.parity.commands;

import org.workcraft.commands.AbstractGameSolverCommand;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.parity.OinkInputNode;
import org.workcraft.plugins.parity.OinkOutputNode;
import org.workcraft.plugins.parity.Parity;
import org.workcraft.plugins.parity.VisualParity;
import org.workcraft.plugins.parity.Player0;
import org.workcraft.plugins.parity.Player1;
import org.workcraft.plugins.parity.VisualPlayer0;
import org.workcraft.plugins.parity.VisualPlayer1;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class involving the game solver. This will invoke functions mainly from
 * Parity.java, as well as dealing with the colouring of the visual model.
 */
public class ParitySolverCommand extends AbstractGameSolverCommand {

    /**
     * Colours all vertices passed in as arguments as red.
     * @param p0nodes   Vertices owned by Player 0
     * @param p1nodes   Vertices owned by Player 1
     */
    public void colorNodesRed(Collection<VisualPlayer0> p0nodes,
            Collection<VisualPlayer1> p1nodes) {

        if (p0nodes == null) {
            p0nodes = new ArrayList<>();
        }
        if (p1nodes == null) {
            p1nodes = new ArrayList<>();
        }
        Iterator<VisualPlayer0> p0iter = p0nodes.iterator();
        Iterator<VisualPlayer1> p1iter = p1nodes.iterator();
        while (p0iter.hasNext()) {
            VisualPlayer0 tempNode = p0iter.next();
            tempNode.setForegroundColor(Color.RED);
        }
        while (p1iter.hasNext()) {
            VisualPlayer1 tempNode = p1iter.next();
            tempNode.setForegroundColor(Color.RED);
        }
    }

    /**
     * Colours all vertices passed in as arguments as blue.
     * @param p0nodes   Vertices owned by Player 0
     * @param p1nodes   Vertices owned by Player 1
     */
    public void colorNodesBlue(Collection<VisualPlayer0> p0nodes,
            Collection<VisualPlayer1> p1nodes) {

        if (p0nodes == null) {
            p0nodes = new ArrayList<>();
        }
        if (p1nodes == null) {
            p1nodes = new ArrayList<>();
        }
        Iterator<VisualPlayer0> p0iter = p0nodes.iterator();
        Iterator<VisualPlayer1> p1iter = p1nodes.iterator();
        while (p0iter.hasNext()) {
            VisualPlayer0 tempNode = p0iter.next();
            tempNode.setForegroundColor(Color.BLUE);
        }
        while (p1iter.hasNext()) {
            VisualPlayer1 tempNode = p1iter.next();
            tempNode.setForegroundColor(Color.BLUE);
        }
    }

    /**
     * Colours edges passed in as red.
     * @param edges    A particular subset of edges in the visual model
     */
    public void colorEdgesRed(Collection<VisualConnection> edges) {

        if (edges == null) {
            edges = new ArrayList<>();
        }

        Iterator<VisualConnection> visualEdgeIter = edges.iterator();
        while (visualEdgeIter.hasNext()) {
            VisualConnection tempEdge = visualEdgeIter.next();
            tempEdge.setColor(Color.RED);
        }
    }

    /**
     * Colours edges passed in as blue.
     * @param edges    A particular subset of edges in the visual model
     */
    public void colorEdgesBlue(Collection<VisualConnection> edges) {

        if (edges == null) {
            edges = new ArrayList<>();
        }

        Iterator<VisualConnection> visualEdgeIter = edges.iterator();
        while (visualEdgeIter.hasNext()) {
            VisualConnection tempEdge = visualEdgeIter.next();
            tempEdge.setColor(Color.BLUE);
        }
    }

    /**
     * Colours edges passed in as black.
     * @param edges    A particular subset of edges in the visual model
     */
    public void colorEdgesBlack(Collection<VisualConnection> edges) {

        if (edges == null) {
            edges = new ArrayList<>();
        }

        Iterator<VisualConnection> visualEdgeIter = edges.iterator();
        while (visualEdgeIter.hasNext()) {
            VisualConnection tempEdge = visualEdgeIter.next();
            tempEdge.setColor(Color.BLACK);
        }
    }

    /**
     * Function to colour winning regions. Will colour the full visual model;
     * all vertices and edges (where a winning strategy exists) shall be
     * coloured.
     * BLUE = Player 0 wins
     * RED = Player 1 wins
     * @param outputNodes    Collection of output nodes generated from Oink
     *                       solution file
     * @param pg             Mathematical model of the parity game
     * @param vpg            Visual model of the parity game
     */
    public void colorWinningRegions(ArrayList<OinkOutputNode> outputNodes,
            Parity pg, VisualParity vpg) {
        ArrayList<VisualPlayer0> p0win = new ArrayList<>();
        ArrayList<VisualPlayer0> p0lose = new ArrayList<>();
        ArrayList<VisualPlayer1> p1win = new ArrayList<>();
        ArrayList<VisualPlayer1> p1lose = new ArrayList<>();
        ArrayList<VisualConnection> p0strategy = new ArrayList<>();
        ArrayList<VisualConnection> p1strategy = new ArrayList<>();
        HashMap<String, Integer> nameToId = new HashMap<>();

        Iterator<Player0> p0iter = pg.getPlayer0().iterator();
        Iterator<Player1> p1iter = pg.getPlayer1().iterator();
        Iterator<VisualPlayer0> visualp0iter = vpg.getVisualPlayer0().iterator();
        Iterator<VisualPlayer1> visualp1iter = vpg.getVisualPlayer1().iterator();
        Iterator<OinkOutputNode> outputNodeiter = outputNodes.iterator();

        //Colour nodes owned by player 0
        while (p0iter.hasNext()) {
            Player0 tempP0 = p0iter.next();
            VisualPlayer0 tempvisualp0 = visualp0iter.next();
            OinkOutputNode tempOutput = outputNodeiter.next();
            nameToId.put(pg.getName(tempP0), tempOutput.getId());
            if (tempOutput.getWonByPlayer1()) {
                p0lose.add(tempvisualp0);
            } else {
                p0win.add(tempvisualp0);
            }
        }

        //Colour nodes owned by player 1
        while (p1iter.hasNext()) {
            Player1 tempP1 = p1iter.next();
            VisualPlayer1 tempvisualp1 = visualp1iter.next();
            OinkOutputNode tempOutput = outputNodeiter.next();
            nameToId.put(pg.getName(tempP1), tempOutput.getId());
            if (tempOutput.getWonByPlayer1()) {
                p1win.add(tempvisualp1);
            } else {
                p1lose.add(tempvisualp1);
            }
        }

        //2nd pass through OinkOutputNodes, Colour edges if strategy found
        outputNodeiter = outputNodes.iterator();
        while (outputNodeiter.hasNext()) {
            Iterator<MathConnection> edgeIter = pg.getConnections().iterator();
            Iterator<VisualConnection> visualEdgeIter = vpg.getEdges().iterator();
            OinkOutputNode tempOutput = outputNodeiter.next();

            while (edgeIter.hasNext()) {
                MathConnection tempEdge = edgeIter.next();
                VisualConnection tempVisualEdge = visualEdgeIter.next();
                MathNode tempFirst = tempEdge.getFirst();
                MathNode tempSecond = tempEdge.getSecond();
                Integer srcNode = nameToId.get(pg.getName(tempFirst));
                Integer destNode = nameToId.get(pg.getName(tempSecond));

                if (tempOutput.getId() == srcNode &&
                        tempOutput.getStrategy() == destNode) {
                    if (!tempOutput.getWonByPlayer1()) {
                        p0strategy.add(tempVisualEdge);
                    } else {
                        p1strategy.add(tempVisualEdge);
                    }
                }
            }
        }

        //Reset colour of all edges back to black
        colorEdgesBlack(vpg.getEdges());

        colorNodesBlue(p0win, p1lose);
        colorNodesRed(p0lose, p1win);
        colorEdgesBlue(p0strategy);
        colorEdgesRed(p1strategy);
    }

    /**
     * Get the display name of the text box to be clicked to run game solver.
     */
    @Override
    public String getDisplayName() {
        return "Solve Game";
    }

    /**
     * Ensure the WorkspaceEntry can be worked on.
     */
    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Parity.class);
    }

    /**
     * Generate the game solver here, calling every necessary function to
     * solve the game and colour the model accordingly.
     * @param we    Current WorkspaceEntry
     * @return      Text to be output to the reader
     */
    @Override
    public String getGameSolver(WorkspaceEntry we) {

        Parity pg = WorkspaceUtils.getAs(we, Parity.class);

        if (!pg.isMacLinux()) {
            return "Parity Game Plugin is only designed to be ran on\n"
                + "Linux distros or Mac OS.\n"
                + "If using Windows, please use WSL or Linux VM.\n";
        }

        if (!pg.isNonEmpty()) {
            return "Please place some vertices on the game graph.\n";
        }

        if (!pg.isNonNegative()) {
            return "Parity games do not allow negative priorities.\n"
                + "Please ensure all vertex priorities are non-negative.\n";
        }

        ArrayList<OinkInputNode> inputNodes = pg.buildOinkInput();
        if (!pg.isInfinite(inputNodes)) {
            return "Oink will only solve parity games that are infinitely looping\n"
                + "(every vertex has at least one outgoing edge).\n"
                + "Please ensure all vertices have at least one outgoing edge.\n";
        }

        String oinkInput = pg.printOinkInput(inputNodes);
        String textOutput = pg.runOink(oinkInput);
        ArrayList<OinkOutputNode> oinkOutput = pg.buildOutputNodes();
        VisualParity vpg = WorkspaceUtils.getAs(we, VisualParity.class);
        colorWinningRegions(oinkOutput, pg, vpg);
        pg.deleteTempFiles();
        return textOutput;
    }

}
