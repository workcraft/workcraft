package org.workcraft.plugins.stg.commands;

import org.workcraft.util.Pair;

public class ExpandHandshakeCtrlSanTransformationCommand extends ExpandHandshakeTransformationCommand {

    private static final String SUFFIX_CTRL = "_ctrl";
    private static final String SUFFIX_SAN = "_san";

    @Override
    public String getDisplayName() {
        return "Expand selected handshake transitions (" + SUFFIX_CTRL + " " + SUFFIX_SAN + ")";
    }

    @Override
    public String getPopupName() {
        return "Expand handshake transition (" + SUFFIX_CTRL + " " + SUFFIX_SAN + ")";
    }

    @Override
    public Pair<String, String> getSufixes() {
        return Pair.of(SUFFIX_CTRL, SUFFIX_SAN);
    }

}
