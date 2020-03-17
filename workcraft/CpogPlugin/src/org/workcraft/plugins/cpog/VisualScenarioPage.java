package org.workcraft.plugins.cpog;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;

public class VisualScenarioPage extends VisualPage {

    public static final String PROPERTY_ENCODING = "Encoding";

    private Encoding encoding = new Encoding();

    public VisualScenarioPage(MathNode refNode) {
        super(refNode);

        addPropertyDeclaration(new PropertyDeclaration<>(Encoding.class, PROPERTY_ENCODING,
                this::setEncoding, this::getEncoding).setCombinable().setTemplatable());
    }

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
