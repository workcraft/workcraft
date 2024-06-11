package org.workcraft.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.workspace.ModelEntry;

public interface NodeTransformer {
    boolean isApplicableTo(VisualNode node);
    boolean isEnabled(ModelEntry me, VisualNode node);
    String getPopupName(ModelEntry me, VisualNode node);
}
