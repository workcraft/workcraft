package org.workcraft.plugins.cpog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CpogSettings implements SettingsPage {

	private static LinkedList<PropertyDescriptor> properties;

	public enum SatSolver {
		MINISAT("MiniSat"),
		CLASP("Clasp");

		public final String name;

		private SatSolver(String name) {
			this.name = name;
		}

		static public Map<String, SatSolver> getChoice() {
			LinkedHashMap<String, SatSolver> choice = new LinkedHashMap<String, SatSolver>();
			for (SatSolver item : SatSolver.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
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

		properties.add(new PropertyDeclaration<CpogSettings, SatSolver>(
				this, "SAT solver", SatSolver.class, SatSolver.getChoice()) {
			protected void setter(CpogSettings object, SatSolver value) {
				CpogSettings.setSatSolver(value);
			}
			protected SatSolver getter(CpogSettings object) {
				return CpogSettings.getSatSolver();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, Integer>(
				this, "Encoding bit-width", Integer.class) {
			protected void setter(CpogSettings object, Integer value) {
				CpogSettings.setEncodingWidth(value);
			}
			protected Integer getter(CpogSettings object) {
				return CpogSettings.getEncodingWidth();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, Integer>(
				this, "Circuit size in 2-input gates", Integer.class) {
			protected void setter(CpogSettings object, Integer value) {
				CpogSettings.setCircuitSize(value);
			}
			protected Integer getter(CpogSettings object) {
				return CpogSettings.getCircuitSize();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, String>(
				this, "Clasp solver command", String.class) {
			protected void setter(CpogSettings object, String value) {
				CpogSettings.setClaspCommand(value);
			}
			protected String getter(CpogSettings object) {
				return CpogSettings.getClaspCommand();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, String>(
				this, "MiniSat solver command", String.class) {
			protected void setter(CpogSettings object, String value) {
				CpogSettings.setMinisatCommand(value);
			}
			protected String getter(CpogSettings object) {
				return CpogSettings.getMinisatCommand();
			}
		});
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
