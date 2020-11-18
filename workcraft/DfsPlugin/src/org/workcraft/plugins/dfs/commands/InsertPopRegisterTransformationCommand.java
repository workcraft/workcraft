package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.PopRegister;

public class InsertPopRegisterTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "pop register";
    }

    @Override
    public PopRegister createComponent() {
        return new PopRegister();
    }

}
