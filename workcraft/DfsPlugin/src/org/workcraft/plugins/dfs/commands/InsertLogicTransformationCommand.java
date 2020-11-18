package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.Logic;

public class InsertLogicTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "logic";
    }

    @Override
    public Logic createComponent() {
        return new Logic();
    }

}
