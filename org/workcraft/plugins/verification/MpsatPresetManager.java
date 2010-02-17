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
import org.workcraft.plugins.verification.MpsatSettings.SolutionMode;
import org.workcraft.util.FileUtils;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class MpsatPresetManager {
	private ArrayList<MpsatSettings> presets = new ArrayList<MpsatSettings>();

	private void addBuiltInPresets() {
		presets.add(new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.FIRST, 0, "", "Deadlock", true));
		presets.add(new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.MINIMUM_COST, 0, "", "Deadlock (shortest trace)", true));
		presets.add(new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.ALL, 0, "", "Deadlock (all traces)", true));
	}

	private void savePresets() {
		try {
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("mpsat-presets");
			doc.appendChild(root);

			for (MpsatSettings p : presets)
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
					MpsatSettings e = new MpsatSettings(p);
					presets.add(e);
				}
			}

			Collections.sort(presets, new Comparator<MpsatSettings>() {
				@Override
				public int compare(MpsatSettings o1, MpsatSettings o2) {
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

	public void createPreset (MpsatSettings settings) {
		presets.add(settings);
		savePresets();
	}

	public void updatePreset (int preset, MpsatSettings settings) {
		if (!presets.get(preset).isBuiltIn()) {
			presets.set(preset, settings);
			savePresets();
		}
	}

	public MpsatSettings findPreset(String description) {
		for (MpsatSettings preset : presets)
			if (preset.getDescription().equals(description))
				return preset;
		return null;
	}

	public List<MpsatSettings> getPresets() {
		return Collections.unmodifiableList(presets);
	}

}