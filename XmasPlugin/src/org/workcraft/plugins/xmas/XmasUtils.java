package org.workcraft.plugins.xmas;

import org.workcraft.dom.Node;
import org.workcraft.plugins.xmas.components.VisualXmasComponent;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.plugins.xmas.components.XmasComponent;
import org.workcraft.plugins.xmas.components.XmasContact;

public class XmasUtils {

    public static XmasComponent getConnectedComponent(final Xmas xmas, XmasContact contact) {
        XmasComponent result = null;
        if (contact.isInput()) {
            for (Node prevNode: xmas.getPreset(contact)) {
                if (prevNode instanceof XmasContact) {
                    result = (XmasComponent) prevNode.getParent();
                    break;
                }
            }
        } else {
            for (Node succNode: xmas.getPostset(contact)) {
                if (succNode instanceof XmasContact) {
                    result = (XmasComponent) succNode.getParent();
                    break;
                }
            }
        }
        return result;
    }

    public static VisualXmasComponent getConnectedComponent(final VisualXmas visualXmas, VisualXmasContact visualContact) {
        Xmas xmas = (Xmas) visualXmas.getMathModel();
        XmasContact contact = visualContact.getReferencedContact();
        XmasComponent component = getConnectedComponent(xmas, contact);
        return visualXmas.getVisualComponent(component, VisualXmasComponent.class);
    }

    public static XmasContact getConnectedContact(final Xmas xmas, XmasContact contact) {
        XmasContact result = null;
        if (contact.isInput()) {
            for (Node prevNode: xmas.getPreset(contact)) {
                if (prevNode instanceof XmasContact) {
                    result = (XmasContact) prevNode;
                    break;
                }
            }
        } else {
            for (Node succNode: xmas.getPostset(contact)) {
                if (succNode instanceof XmasContact) {
                    result = (XmasContact) succNode;
                    break;
                }
            }
        }
        return result;
    }

    public static VisualXmasContact getConnectedContact(final VisualXmas visualXmas, VisualXmasContact visualContact) {
        Xmas xmas = (Xmas) visualXmas.getMathModel();
        XmasContact contact = visualContact.getReferencedContact();
        XmasContact connectedContact = getConnectedContact(xmas, contact);
        return visualXmas.getVisualComponent(connectedContact, VisualXmasContact.class);
    }

}
