package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.PushRegister;

public class InsertPushRegisterTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "push register";
    }

    @Override
    public PushRegister createComponent() {
        return new PushRegister();
    }

}
