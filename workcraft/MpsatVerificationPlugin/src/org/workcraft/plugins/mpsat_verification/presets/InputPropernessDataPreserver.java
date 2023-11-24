package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.presets.DataPreserver;
import org.workcraft.workspace.WorkspaceEntry;

public class InputPropernessDataPreserver extends DataPreserver<InputPropernessParameters> {

    public InputPropernessDataPreserver(WorkspaceEntry we) {
        super(we, "input-properness.xml", new InputPropernessDataSerialiser());
    }

}
