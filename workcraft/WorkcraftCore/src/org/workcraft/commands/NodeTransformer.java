package org.workcraft.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.workspace.ModelEntry;

public interface NodeTransformer {
    String getPopupName();
    boolean isApplicableTo(VisualNode node);
    boolean isEnabled(ModelEntry me, VisualNode node);
}
