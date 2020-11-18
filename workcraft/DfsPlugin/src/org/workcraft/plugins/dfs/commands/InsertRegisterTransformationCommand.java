package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.Register;

public class InsertRegisterTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "register";
    }

    @Override
    public Register createComponent() {
        return new Register();
    }

}
