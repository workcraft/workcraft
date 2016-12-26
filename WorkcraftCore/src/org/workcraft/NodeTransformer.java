package org.workcraft;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.workspace.ModelEntry;

public interface NodeTransformer {
    String getPopupName();
    boolean isApplicableTo(Node node);
    boolean isEnabled(ModelEntry me, Node node);
    void transform(Model model, Node node);
}
