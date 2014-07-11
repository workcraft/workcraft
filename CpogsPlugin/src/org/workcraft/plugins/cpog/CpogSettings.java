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
	//private static int encodingWidth = 2;
	private static int circuitSize = 4;
	private static String claspCommand = "clasp";
	private static String minisatCommand = "minisat";
	private static String espressoCommand = "espresso";
	private static String abcFolder = "abc/";
	private static String gatesLibrary = "90nm.genlib";

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

		properties.add(new PropertyDeclaration<CpogSettings, String>(
				this, "Espresso solver", String.class) {
			protected void setter(CpogSettings object, String value) {
				CpogSettings.setEspressoCommand(value);
			}
			protected String getter(CpogSettings object) {
				return CpogSettings.getEspressoCommand();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, String>(
				this, "Abc folder path", String.class) {
			protected void setter(CpogSettings object, String value) {
				CpogSettings.setAbcFolder(value);
			}
			protected String getter(CpogSettings object) {
				return CpogSettings.getAbcFolder();
			}
		});

		properties.add(new PropertyDeclaration<CpogSettings, String>(
				this, "Gate library (genlib format) inside abc folder", String.class) {
			protected void setter(CpogSettings object, String value) {
				CpogSettings.setGatesLibrary(value);
			}
			protected String getter(CpogSettings object) {
				return CpogSettings.getGatesLibrary();
			}
		});
	}

	@Override
	public String getName() {
		return "SCENCO";
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public void load(Config config) {
		satSolver = config.getEnum("CpogSettings.satSolver", SatSolver.class, SatSolver.CLASP);
		circuitSize = config.getInt("CpogSettings.circuitSize", 4);
		setClaspCommand(config.getString("CpogSettings.claspCommand", "clasp"));
		setMinisatCommand(config.getString("CpogSettings.minisatCommand", "minisat"));
		setEspressoCommand(config.getString("CpogSettings.espressoCommand", "espresso"));
		setAbcFolder(config.getString("CpogSettings.abcFolder", "abc/"));
		setGatesLibrary(config.getString("CpogSettings.gatesLibrary", "90nm.genlib"));
	}

	@Override
	public void save(Config config) {
		config.setEnum("CpogSettings.satSolver", SatSolver.class, satSolver);
		config.setInt("CpogSettings.circuitSize", circuitSize);
		config.set("CpogSettings.claspCommand", claspCommand);
		config.set("CpogSettings.minisatCommand", minisatCommand);
		config.set("CpogSettings.espressoCommand", espressoCommand);
		config.set("CpogSettings.abcFolder", abcFolder);
		config.set("CpogSettings.gatesLibrary", gatesLibrary);
	}

	public static SatSolver getSatSolver() {
		return satSolver;
	}

	public static void setSatSolver(SatSolver satSolver) {
		CpogSettings.satSolver = satSolver;
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

	public static String getEspressoCommand() {
		return espressoCommand;
	}

	public static void setEspressoCommand(String espressoCommand) {
		CpogSettings.espressoCommand = espressoCommand;
	}

	public static String getAbcFolder() {
		return abcFolder;
	}

	public static void setAbcFolder(String abcFolder) {
		CpogSettings.abcFolder = abcFolder;
	}

	public static String getGatesLibrary() {
		return gatesLibrary;
	}

	public static void setGatesLibrary(String gatesLibrary) {
		CpogSettings.gatesLibrary = gatesLibrary;
	}


}
