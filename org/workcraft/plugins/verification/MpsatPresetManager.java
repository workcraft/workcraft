package org.workcraft.plugins.verification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.plugins.verification.MpsatPreset.SolutionMode;
import org.workcraft.util.FileUtils;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class MpsatPresetManager {
	private ArrayList<MpsatPreset> presets = new ArrayList<MpsatPreset>();

	private void addBuiltInPresets() {
		presets.add(new MpsatPreset(MpsatMode.DEADLOCK, 0, 0, SolutionMode.FIRST, 0, "", "Deadlock", true));
		presets.add(new MpsatPreset(MpsatMode.DEADLOCK, 0, 0, SolutionMode.MINIMUM_COST, 0, "", "Deadlock (shortest trace)", true));
		presets.add(new MpsatPreset(MpsatMode.DEADLOCK, 0, 0, SolutionMode.ALL, 0, "", "Deadlock (all traces)", true));
	}

	private void savePresets() {
		try {
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("mpsat-presets");
			doc.appendChild(root);

			for (MpsatPreset p : presets)
				if (!p.isBuiltIn())
					p.toXML(root);

			XmlUtil.saveDocument(doc, new File("config/mpsat_presets.xml"));
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MpsatPresetManager() {
		try {
			addBuiltInPresets();

			File f = new File("config/mpsat_presets.xml");
			if (f.exists()) {
				Document doc = XmlUtil.loadDocument("config/mpsat_presets.xml");

				for (Element p : XmlUtil.getChildElements("preset", doc.getDocumentElement())) {
					MpsatPreset e = new MpsatPreset(p);
					presets.add(e);
				}
			}

			Collections.sort(presets, new Comparator<MpsatPreset>() {
				@Override
				public int compare(MpsatPreset o1, MpsatPreset o2) {
					return o1.getDescription().compareTo(o2.getDescription());
				}
			});
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void createPreset (MpsatPreset settings) {
		presets.add(settings);
		savePresets();
	}

	public void updatePreset (int preset, MpsatPreset settings) {
		if (!presets.get(preset).isBuiltIn()) {
			presets.set(preset, settings);
			savePresets();
		}
	}

	public MpsatPreset findPreset(String description) {
		for (MpsatPreset preset : presets)
			if (preset.getDescription().equals(description))
				return preset;
		return null;
	}

	public List<MpsatPreset> getPresets() {
		return Collections.unmodifiableList(presets);
	}

	public String[] getMpsatArguments(MpsatPreset preset) {
		ArrayList<String> args = new ArrayList<String>();
		args.add(preset.getMode().getArgument());
		args.add(String.format("-v%d", preset.getVerbosity()));
		args.add(String.format("-$%d", preset.getSatSolver()));


		switch (preset.getSolutionMode()) {
		case FIRST:
			break;
		case MINIMUM_COST:
			args.add("-f");
			break;
		case ALL:
			int solutionNumberLimit = preset.getSolutionNumberLimit();
			if (solutionNumberLimit>0)
				args.add("-a" + Integer.toString(solutionNumberLimit));
			else
				args.add("-a");
		}


		if (preset.getMode().isReach())
			try {
				File reach = File.createTempFile("reach", null);
				reach.deleteOnExit();
				FileUtils.dumpString(reach, preset.getReach());
				args.add(String.format("-d@%s", reach.getCanonicalPath()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return args.toArray(new String[args.size()]);
	}
}