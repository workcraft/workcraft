package tools;

import org.w3c.dom.Element;
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.SynthesisSettings;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;

public class BalsaConfigSerialiser implements SettingsSerialiser<BalsaExportConfig> {

	@Override
	public BalsaExportConfig fromXML(Element e) {

		SynthesisSettings synthesisSettings =
			new SynthesisSettings(
					SynthesisSettings.SynthesisTool.valueOf(e.getAttribute("synthesis-tool")),
					SynthesisSettings.DummyContractionMode.valueOf(e.getAttribute("contraction-mode"))
					);

		BalsaExportConfig.CompositionMode compMode = BalsaExportConfig.CompositionMode.valueOf(e.getAttribute("composition-mode"));
		BalsaExportConfig.Protocol protocol = BalsaExportConfig.Protocol.valueOf(e.getAttribute("protocol"));

		return new BalsaExportConfig(synthesisSettings, compMode, protocol);
	}

	@Override
	public void toXML(BalsaExportConfig settings, Element parent) {
		Element e = parent.getOwnerDocument().createElement("settings");
		e.setAttribute("composition-mode", settings.getCompositionMode().name());
		e.setAttribute("contraction-mode", settings.getSynthesisSettings().getDummyContractionMode().name());
		e.setAttribute("synthesis-tool", settings.getSynthesisSettings().getSynthesisTool().name());
		e.setAttribute("protocol", settings.getProtocol().name());

		parent.appendChild(e);
	}
}
