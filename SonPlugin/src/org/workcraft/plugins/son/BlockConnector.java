package org.workcraft.plugins.son;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.plugins.son.util.Interval;

public class BlockConnector {

    /**
     * reconnect block interface to its bounding.
     */
    public static void blockBoundingConnector(VisualSON visualNet) {
        for (VisualBlock block : visualNet.getVisualBlocks()) {
            if (block.getIsCollapsed()) {
                blockBoundingConnector(block, visualNet);
            }
        }
    }

    private static void blockBoundingConnector(VisualBlock block, VisualSON visualNet) {
        SON net = (SON) visualNet.getMathModel();

        Collection<VisualComponent> components = block.getComponents();

        for (VisualSONConnection con : visualNet.getVisualSONConnections()) {
            Node first = con.getFirst();
            Node second = con.getSecond();

            if (!components.contains(first) && components.contains(second)) {
                if (first instanceof VisualPlaceNode) {
                    //set input value
                    String name = net.getNodeReference(((VisualEvent) second).getReferencedComponent());
                    String type = "-" + con.getReferencedSONConnection().getSemantics();
                    String time = "-" + con.getTime();
                    String value = "";
                    if (((VisualPlaceNode) first).getInterface() == "") {
                        value = "to-" + name + type + time + ";";
                    } else {
                        value = ((VisualPlaceNode) first).getInterface() + "to-" + name + type + time + ";";
                    }
                    ((VisualPlaceNode) first).setInterface(value);
                    //remove connection
                    removeConnection(con);
                    //create connection from first to block
                    if (visualNet.getConnection(first, block) == null) {
                        try {
                            visualNet.forceConnectionSemantics(con.getReferencedSONConnection().getSemantics());
                            visualNet.connect(first, block);
                            VisualSONConnection newCon = visualNet.getVisualConnections((VisualComponent) first, (VisualComponent) block).iterator().next();
                            newCon.setTime(con.getTime());

                        } catch (InvalidConnectionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (components.contains(first) && !components.contains(second)) {
                if (second instanceof VisualPlaceNode) {
                    //set output value
                    String name = net.getNodeReference(((VisualEvent) first).getReferencedComponent());
                    String type = "-" + con.getReferencedSONConnection().getSemantics();
                    String time = "-" + con.getTime().toString();
                    String value = "";
                    if (((VisualPlaceNode) second).getInterface() == "") {
                        value = "from-" + name + type + time + ";";
                    } else {
                        value = ((VisualPlaceNode) second).getInterface() + "from-" + name + type + time + ";";
                    }
                    ((VisualPlaceNode) second).setInterface(value);

                    //remove connection
                    removeConnection(con);
                    //create connection from block to output
                    if (visualNet.getConnection(block, second) == null) {
                        try {
                            visualNet.forceConnectionSemantics(con.getReferencedSONConnection().getSemantics());
                            visualNet.connect(block, second);
                            VisualSONConnection newCon = visualNet.getVisualConnections((VisualComponent) block, (VisualComponent) second).iterator().next();
                            newCon.setTime(con.getTime());
                        } catch (InvalidConnectionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * reconnect from block bounding to its inside
     */
    public static void blockInternalConnector(VisualSON visualNet) {

        for (VisualBlock block : visualNet.getVisualBlocks()) {
            if (block.getIsCollapsed()) {
                blockInternalConnector(block, visualNet);
            }
        }
        for (VisualPlaceNode place : visualNet.getVisualPlaceNode()) {
            place.setInterface("");
        }
        connectionChecker(visualNet);
    }

    private static void connectionChecker(VisualSON visualNet) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        SON net = (SON) visualNet.getMathModel();

        for (VisualBlock block : visualNet.getVisualBlocks()) {
            if (!net.getPreset(block.getReferencedComponent()).isEmpty()
                    || !net.getPostset(block.getReferencedComponent()).isEmpty()) {

                JOptionPane.showMessageDialog(mainWindow, "Incorrect block connection"
                        + "Error may due to lost block information, " +
                        "reconnect block components again. " + net.getNodeReference(block),
                        "Block connection error", JOptionPane.WARNING_MESSAGE);
            }
        }

    }

    private static void blockInternalConnector(VisualBlock block, VisualSON visualNet) {
        SON net = (SON) visualNet.getMathModel();

        for (VisualPlaceNode p : visualNet.getVisualPlaceNode()) {
            String interfaceValue = p.getInterface();
            //    String newValue = interfaceValue;

            if (!interfaceValue.isEmpty()) {
                String[] infos = interfaceValue.trim().split(";");
                //interface compatiability checking
                ArrayList<VisualSONConnection> connections = new ArrayList<>();
                for (VisualSONConnection con : visualNet.getVisualSONConnections()) {
                    if (con.getFirst() == p && (con.getSecond() == block)) {
                        connections.add(con);
                    }
                    if (con.getSecond() == p && (con.getFirst() == block)) {
                        connections.add(con);
                    }
                }

                for (VisualSONConnection con :connections) {
                    //remove connection
                    removeConnection(con);
                }

                for (String info : infos) {
                    String[] piece = info.trim().split("-");
                    VisualEvent e = null;
                    //obtain event
                    for (VisualEvent event : block.getVisualEvents()) {
                        if (net.getNodeReference(event.getReferencedComponent()).equals(piece[1])) {
                            e = event;
                        }
                    }

                    if (e != null && visualNet.getConnection(e, p) == null) {
                        //create input connection
                        if (piece[0].equals("to")) {
                            VisualSONConnection con = null;
                            try {
                                if (piece[2].equals(Semantics.PNLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(p, e, Semantics.PNLINE);
                                } else if (piece[2].equals(Semantics.SYNCLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(p, e, Semantics.SYNCLINE);
                                } else if (piece[2].equals(Semantics.ASYNLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(p, e, Semantics.ASYNLINE);
                                } else if (piece[2].equals(Semantics.BHVLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(p, e, Semantics.BHVLINE);
                                }
                                //remove value
                                interfaceValue = interfaceValue.replace(info + ";", "");
                            } catch (InvalidConnectionException ex) {
                                ex.printStackTrace();
                            }
                            //set time value
                            if (con != null) {
                                int min = 0000;
                                int max = 9999;
                                try {
                                    min = Integer.parseInt(piece[3]);
                                    max = Integer.parseInt(piece[4]);
                                } catch (NumberFormatException e1) {
                                    e1.printStackTrace();
                                }
                                con.getReferencedSONConnection().setTime(new Interval(min, max));
                            }
                            //create output connection
                        } else if (piece[0].equals("from")) {
                            VisualSONConnection con = null;
                            try {
                                if (piece[2].equals(Semantics.PNLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(e, p, Semantics.PNLINE);
                                } else if (piece[2].equals(Semantics.SYNCLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(e, p, Semantics.SYNCLINE);
                                } else if (piece[2].equals(Semantics.ASYNLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(e, p, Semantics.ASYNLINE);
                                } else if (piece[2].equals(Semantics.BHVLINE.toString())) {
                                    con = (VisualSONConnection) visualNet.connect(e, p, Semantics.BHVLINE);
                                }
                                //remove value
                                interfaceValue = interfaceValue.replace(info + ";", "");
                            } catch (InvalidConnectionException ex) {
                                // TODO Auto-generated catch block
                                ex.printStackTrace();
                            }
                            if (con != null) {
                                int min = 4444;
                                int max = 9999;
                                try {
                                    min = Integer.parseInt(piece[3]);
                                    max = Integer.parseInt(piece[4]);
                                } catch (NumberFormatException e1) {
                                    e1.printStackTrace();
                                }
                                con.getReferencedSONConnection().setTime(new Interval(min, max));
                            }
                        }
                    }
                }
                p.setInterface(interfaceValue);
            }
        }
    }

    private static void removeConnection(VisualSONConnection con) {
        //remove visual connection
        Container parent = (Container) con.getParent();
        SONConnection mathCon = con.getReferencedSONConnection();
        parent.remove(con);

        //remove math connection
        Container mathParent = (Container) mathCon.getParent();
        if (mathParent != null) {
            mathParent.remove(mathCon);
        }
    }
}
