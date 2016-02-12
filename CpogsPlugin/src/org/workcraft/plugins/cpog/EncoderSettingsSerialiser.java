package org.workcraft.plugins.cpog;

import org.w3c.dom.Element;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;
import org.workcraft.util.XmlUtil;

public class EncoderSettingsSerialiser implements SettingsSerialiser<EncoderSettings> {

    @Override
    public EncoderSettings fromXML(Element e) {
        String espressoPath = XmlUtil.readStringAttr(e, "Espresso");
        String abcPath = XmlUtil.readStringAttr(e, "Abc");
        String libraryPath = XmlUtil.readStringAttr(e, "Library");

        return new EncoderSettings(espressoPath, abcPath, libraryPath);
    }

    @Override
    public void toXML(EncoderSettings settings, Element parent) {
        Element e = parent.getOwnerDocument().createElement("settings");
        e.setAttribute("Espresso", settings.getEspressoPath());
        e.setAttribute("Abc", settings.getAbcPath());
        e.setAttribute("Library", settings.getLibPath());

        parent.appendChild(e);

    }

}
