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

	private static LinkedList<PropertyDescriptor> properties;

	private static final String prefix = "CpogSettings";
	private static final String keySatSolver  = prefix + ".satSolver";
	private static final String keyEncodingWidth = prefix + ".encodingWidth";
	private static final String keyCircuitSize = prefix + ".circuitSize";
	private static final String keyClaspCommand = prefix + ".claspCommand";
	private static final String keyMinisatCommand = prefix + ".minisatCommand";

	private static final SatSolver defaultSatSolver = SatSolver.CLASP;
	private static final int defaultEncodingWidth = 2;
	private static final int defaultCircuitSize = 4;
	private static final String defaultClaspCommand = "clasp";
	private static final String defaultMinisatCommand = "minisat";

	private static SatSolver satSolver = defaultSatSolver;
	private static int encodingWidth = defaultEncodingWidth;
	private static int circuitSize = defaultCircuitSize;
	private static String claspCommand = defaultClaspCommand;
	private static String minisatCommand = defaultMinisatCommand;

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
		setSatSolver(config.getEnum(keySatSolver, SatSolver.class, defaultSatSolver));
		setEncodingWidth(config.getInt(keyEncodingWidth, defaultEncodingWidth));
		setCircuitSize(config.getInt(keyCircuitSize, defaultCircuitSize));
		setClaspCommand(config.getString(keyClaspCommand, defaultClaspCommand));
		setMinisatCommand(config.getString(keyMinisatCommand, defaultMinisatCommand));
	}

	@Override
	public void save(Config config) {
		config.setEnum(keySatSolver, SatSolver.class, satSolver);
		config.setInt(keyEncodingWidth, getEncodingWidth());
		config.setInt(keyCircuitSize, getCircuitSize());
		config.set(keyClaspCommand, getClaspCommand());
		config.set(keyMinisatCommand, getMinisatCommand());
	}

	public static SatSolver getSatSolver() {
		return satSolver;
	}

	public static void setSatSolver(SatSolver value) {
		satSolver = value;
	}

	public static int getEncodingWidth() {
		return encodingWidth;
	}

	public static void setEncodingWidth(int value) {
		encodingWidth = value;
	}

	public static int getCircuitSize() {
		return circuitSize;
	}

	public static void setCircuitSize(int value) {
		circuitSize = value;
	}

	public static String getClaspCommand() {
		return claspCommand;
	}

	public static void setClaspCommand(String value) {
		claspCommand = value;
	}

	public static String getMinisatCommand() {
		return minisatCommand;
	}

	public static void setMinisatCommand(String value) {
		minisatCommand = value;
	}
}
