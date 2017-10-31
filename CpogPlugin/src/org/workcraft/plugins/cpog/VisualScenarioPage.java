package org.workcraft.plugins.cpog;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;

public class VisualScenarioPage extends VisualPage {

    public static final String PROPERTY_ENCODING = "Encoding";

    public VisualScenarioPage(MathNode refNode) {
        super(refNode);

        addPropertyDeclaration(new PropertyDeclaration<VisualScenarioPage, Encoding>(
                this, PROPERTY_ENCODING, Encoding.class, true, true, true) {
            public void setter(VisualScenarioPage object, Encoding value) {
                object.setEncoding(value);
            }
            public Encoding getter(VisualScenarioPage object) {
                return object.getEncoding();
            }
        });
    }

    private Encoding encoding = new Encoding();

    public void setEncoding(Encoding value) {
        if (encoding != value) {
            encoding = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ENCODING));
        }
    }

    public Encoding getEncoding() {
        return encoding;
    }

}
