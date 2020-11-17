package org.workcraft.plugins.dfs.commands;

import org.workcraft.plugins.dfs.CounterflowLogic;

public class InsertCounterflowLogicTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getTypeName() {
        return "counterflow logic";
    }

    @Override
    public CounterflowLogic createComponent() {
        return new CounterflowLogic();
    }

}
