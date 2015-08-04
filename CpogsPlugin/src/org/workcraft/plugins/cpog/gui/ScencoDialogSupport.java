package org.workcraft.plugins.cpog.gui;

import java.awt.Dimension;

public class ScencoDialogSupport {

	// Size of components present in SCENCO dialogs
	public static final Dimension dimensionOptimiseForBox = new Dimension(180, 26);
	public static final Dimension dimensionText = new Dimension(585, 22);
	public static final Dimension dimensionTable = new Dimension(400, 180);
	public static final Dimension dimensionWindow = new Dimension(100, 400);

	// Optimise for
	public static final String textOptimiseForLabel = "Target:                ";
	public static final String textOptimiseForFirstElement = "Microcontroller";
	public static final String textOptimiseForSecondElement = "CPOG";

	// Verbose mode
	public static final String textVerboseMode = "Verbose";

	// Abc tool
	public static final String textAbcLabel = "Use ABC for logic synthesis";

	// Algorithm tweaking panel
	public static final String textEncodingBitWidth = "Encoding bit-width:";
	public static final String textNumberSolutionLabel = " Number of solutions to generate";
	public static final String textCircuitSizeLabel = "Circuit size in 2-input gates:";
	public static final Dimension dimensionCircuitSizeText = new Dimension(35, 22);
	public static final Dimension dimensionBitEncodingWidthText = new  Dimension(35, 22);
	public static final Dimension dimensionNumberSolutionText = new Dimension(35, 22);

	// Table description
	public static final String textFirstColumnTable = "Name";
	public static final String textSecondColumnTable = "Opcode";

	// Example label for custom encoding
	public static final String dontCareBit = "?";
	public static final String reservedBit = "X";
	public static final String normalBitText = "0/1 - assign 0 or 1;  ";
	public static final String dontCareBitText = " - find best assignment;  ";
	public static final String reservedBitText = " - don't use (reserved bit).";
	public static final String textCustomiseLabel = "Customise                   ";
}
