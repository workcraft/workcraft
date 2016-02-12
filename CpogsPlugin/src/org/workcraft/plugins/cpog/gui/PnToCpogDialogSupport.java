package org.workcraft.plugins.cpog.gui;

import java.awt.Dimension;

public class PnToCpogDialogSupport {

    // labels
    public static String textReduceLabel = "Perform reductions of maximal significant runs";
    public static String textIsomorphismLabel = "Reduce isomorphic processes";
    public static String textSignificanceLabel = " Algorithm for checking significance property:   ";
    public static String textRemoveNodeLabel = "Remove condition nodes";

    // comboboxes
    public static String[] significanceItems = {"Exhaustive", "Hashmap-based", "Tree of runs"};

    // dimensions
    public static Dimension significanceSize = new Dimension(180, 26);

}
