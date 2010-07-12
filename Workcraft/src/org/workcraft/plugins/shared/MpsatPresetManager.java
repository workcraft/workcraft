package org.workcraft.plugins.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.plugins.shared.MpsatSettings.SolutionMode;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class MpsatPresetManager {
	private ArrayList<MpsatPreset> presets = new ArrayList<MpsatPreset>();

	private void addBuiltInPresets() {
		presets.add(new MpsatPreset("Deadlock", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.FIRST, 0, ""), true));
		presets.add(new MpsatPreset("Deadlock (shortest trace)", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.MINIMUM_COST, 0, ""), true));
		presets.add(new MpsatPreset("Deadlock (all traces)", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.ALL, 0, ""), true));
	}

	private void savePresets() {
		try {
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("mpsat-presets");
			doc.appendChild(root);

			for (MpsatPreset p : presets) {
				if (!p.isBuiltIn()) {
					Element pe = doc.createElement("preset");
					pe.setAttribute("description", p.getDescription());
					p.getSettings().toXML(pe);
					root.appendChild(pe);
				}
			}

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

				for (Element p : XmlUtil.getChildElements("preset", doc.getDocumentElement()))
					presets.add(new MpsatPreset(p));
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

	public MpsatPreset save (MpsatSettings settings, String description) {
		MpsatPreset preset = new MpsatPreset(description, settings, false);
		presets.add(preset);
		savePresets();
		return preset;
	}

	public void update (MpsatPreset preset, MpsatSettings settings) {
		checkBuiltIn(preset);
		preset.setSettings(settings);
		savePresets();
	}

	public void delete (MpsatPreset preset) {
		checkBuiltIn(preset);
		presets.remove(preset);
		savePresets();
	}

	private void checkBuiltIn(MpsatPreset preset) {
		if (preset.isBuiltIn())
			throw new RuntimeException ("Invalid operation attempted on a built-in MPSat preset.");
	}

	public MpsatPreset find(String description) {
		for (MpsatPreset p : presets)
			if (p.getDescription().equals(description))
				return p;
		return null;
	}

	public void rename(MpsatPreset preset, String description) {
		checkBuiltIn(preset);
		preset.setDescription(description);
		savePresets();
	}

	public List<MpsatPreset> list() {
		return Collections.unmodifiableList(presets);
	}
}