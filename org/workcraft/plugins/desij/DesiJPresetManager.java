package org.workcraft.plugins.desij;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.plugins.desij.DesiJSettings.DecompositionStrategy;
import org.workcraft.plugins.desij.DesiJSettings.PartitionMode;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class DesiJPresetManager {
	private ArrayList<DesiJPreset> presets = new ArrayList<DesiJPreset>();

	// **************** private helper methods *********************

	private void addBuiltInPresets() {
		presets.add(new DesiJPreset("Decomposition (basic)",
				new DesiJSettings(DesiJOperation.DECOMPOSITION,
						DecompositionStrategy.BASIC, -1, // decomposition strategy
						PartitionMode.FINEST, "", // partitioning
						true, true, false, // implicit place handling
						false, false, false, // contraction mode
						false, 0, false, false),  // component synthesis
					true) ); // built-in preset
		presets.add(new DesiJPreset("Dummy removal",
				new DesiJSettings(DesiJOperation.REMOVE_DUMMIES,
						DecompositionStrategy.BASIC, -1, // decomposition strategy
						null, "", // partitioning
						true, true, false, // implicit place handling
						false, false, false, // contraction mode
						false, 0, false, false),  // component synthesis
					true) ); // built-in preset
	}

	private void savePresets() {
		try {
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("desij-presets");
			doc.appendChild(root);

			for (DesiJPreset p : presets) {
				if (!p.isBuiltIn()) {
					Element pe = doc.createElement("preset");
					pe.setAttribute("description", p.getDescription());
					p.getSettings().toXML(pe);
					root.appendChild(pe);
				}
			}

			XmlUtil.saveDocument(doc, new File("config/desij_presets.xml"));
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkBuiltIn(DesiJPreset preset) {
		if (preset.isBuiltIn())
			throw new RuntimeException ("Invalid operation attempted on a built-in DesiJ preset.");
	}

	/**
	 * Constructor
	 */
	public DesiJPresetManager() {
		try {
			addBuiltInPresets();

			File f = new File("config/desij_presets.xml");
			if (f.exists()) {
				Document doc = XmlUtil.loadDocument("config/desij_presets.xml");

				for (Element p : XmlUtil.getChildElements("preset", doc.getDocumentElement()))
					presets.add(new DesiJPreset(p));
			}

			Collections.sort(presets, new Comparator<DesiJPreset>() {
				@Override
				public int compare(DesiJPreset o1, DesiJPreset o2) {
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

	// ******************** public methods for access by other classes ************

	public DesiJPreset save (DesiJSettings settings, String description) {
		DesiJPreset preset = new DesiJPreset(description, settings, false);
		presets.add(preset);
		savePresets();
		return preset;
	}

	public void update (DesiJPreset preset, DesiJSettings settings) {
		checkBuiltIn(preset);
		preset.setSettings(settings);
		savePresets();
	}

	public void delete (DesiJPreset preset) {
		checkBuiltIn(preset);
		presets.remove(preset);
		savePresets();
	}

	public DesiJPreset find(String description) {
		for (DesiJPreset p : presets)
			if (p.getDescription().equals(description))
				return p;
		return null;
	}

	public void rename(DesiJPreset preset, String description) {
		checkBuiltIn(preset);
		preset.setDescription(description);
		savePresets();
	}

	public List<DesiJPreset> list() {
		return Collections.unmodifiableList(presets);
	}

}
