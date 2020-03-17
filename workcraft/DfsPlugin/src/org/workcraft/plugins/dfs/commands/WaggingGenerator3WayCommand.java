package org.workcraft.plugins.dfs.commands;

public class WaggingGenerator3WayCommand extends WaggingGeneratorCommand {

    @Override
    public String getDisplayName() {
        return "3-way wagging";
    }

    @Override
    public int getWayCount() {
        return 3;
    }

}
