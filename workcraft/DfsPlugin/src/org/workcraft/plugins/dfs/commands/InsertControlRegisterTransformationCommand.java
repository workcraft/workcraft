package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.ControlRegister;

public class InsertControlRegisterTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "control register";
    }

    @Override
    public ControlRegister createComponent() {
        return new ControlRegister();
    }

}
