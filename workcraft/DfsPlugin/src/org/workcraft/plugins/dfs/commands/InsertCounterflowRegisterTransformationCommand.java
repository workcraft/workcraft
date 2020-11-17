package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.CounterflowRegister;

public class InsertCounterflowRegisterTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "counterflow register";
    }

    @Override
    public CounterflowRegister createComponent() {
        return new CounterflowRegister();
    }

}
