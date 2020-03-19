package org.workcraft.plugins.dfs.commands;

public class WaggingGenerator2WayCommand extends WaggingGeneratorCommand {

    @Override
    public String getDisplayName() {
        return "2-way wagging";
    }

    @Override
    public int getWayCount() {
        return 2;
    }

}
