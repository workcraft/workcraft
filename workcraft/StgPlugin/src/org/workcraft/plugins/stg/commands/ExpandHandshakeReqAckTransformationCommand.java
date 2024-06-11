package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.types.Pair;
import org.workcraft.workspace.ModelEntry;

public class ExpandHandshakeReqAckTransformationCommand extends ExpandHandshakeTransformationCommand {

    private static final String SUFFIX_REQ = "_req";
    private static final String SUFFIX_ACK = "_ack";

    @Override
    public String getDisplayName() {
        return "Expand selected handshake transitions (" + SUFFIX_REQ + ' ' + SUFFIX_ACK + ')';
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Expand handshake transition (" + SUFFIX_REQ + ' ' + SUFFIX_ACK + ')';
    }

    @Override
    public Pair<String, String> getSufixes() {
        return Pair.of(SUFFIX_REQ, SUFFIX_ACK);
    }

}
