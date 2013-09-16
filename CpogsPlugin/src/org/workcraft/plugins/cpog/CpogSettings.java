package org.workcraft.plugins.cpog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CpogSettings implements SettingsPage {

	private static LinkedList<PropertyDescriptor> properties;

	public enum SatSolver {
		MINISAT, CLASP
	}

	private static SatSolver satSolver = SatSolver.CLASP;
	private static int encodingWidth = 2;
	private static int circuitSize = 4;
	private static String claspCommand = "clasp";
	private static String minisatCommand = "minisat";

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public CpogSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		LinkedHashMap<String, Object> solvers = new LinkedHashMap<String, Object>();

		solvers.put("Clasp", SatSolver.CLASP);
		solvers.put("MiniSat", SatSolver.MINISAT);

		properties.add(new PropertyDeclaration(this, "SAT solver", "getSatSolver", "setSatSolver", SatSolver.class, solvers));

		properties.add(new PropertyDeclaration(this, "Encoding bit-width", "getEncodingWidth", "setEncodingWidth", int.class));
		properties.add(new PropertyDeclaration(this, "Circuit size in 2-input gates", "getCircuitSize", "setCircuitSize", int.class));

		properties.add(new PropertyDeclaration(this, "Clasp solver command", "getClaspCommand", "setClaspCommand", String.class));
		properties.add(new PropertyDeclaration(this, "MiniSat solver command", "getMinisatCommand", "setMinisatCommand", String.class));
	}

	@Override
	public String getName() {
		return "Scenco"; // SCENario ENCOder!
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public void load(Config config) {
		satSolver = config.getEnum("CpogSettings.satSolver", SatSolver.class, SatSolver.CLASP);
		encodingWidth = config.getInt("CpogSettings.encodingWidth", 2);
		circuitSize = config.getInt("CpogSettings.circuitSize", 4);
		setClaspCommand(config.getString("CpogSettings.claspCommand", "clasp"));
		setMinisatCommand(config.getString("CpogSettings.minisatCommand", "minisat"));
	}

	@Override
	public void save(Config config) {
		config.setEnum("CpogSettings.satSolver", SatSolver.class, satSolver);
		config.setInt("CpogSettings.encodingWidth", encodingWidth);
		config.setInt("CpogSettings.circuitSize", circuitSize);
		config.set("CpogSettings.claspCommand", claspCommand);
		config.set("CpogSettings.minisatCommand", minisatCommand);
	}

	public static SatSolver getSatSolver() {
		return satSolver;
	}

	public static void setSatSolver(SatSolver satSolver) {
		CpogSettings.satSolver = satSolver;
	}

	public static int getEncodingWidth() {
		return encodingWidth;
	}

	public static void setEncodingWidth(int encodingWidth) {
		CpogSettings.encodingWidth = encodingWidth;
	}

	public static int getCircuitSize() {
		return circuitSize;
	}

	public static void setCircuitSize(int circuitSize) {
		CpogSettings.circuitSize = circuitSize;
	}

	public static String getClaspCommand() {
		return claspCommand;
	}

	public static void setClaspCommand(String claspCommand) {
		CpogSettings.claspCommand = claspCommand;
	}

	public static String getMinisatCommand() {
		return minisatCommand;
	}

	public static void setMinisatCommand(String minisatCommand) {
		CpogSettings.minisatCommand = minisatCommand;
	}
}
