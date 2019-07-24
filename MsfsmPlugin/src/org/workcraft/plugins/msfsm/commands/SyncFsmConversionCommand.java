package org.workcraft.plugins.msfsm.commands;

public class SyncFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getFileName() {
        return "net.g";
    }

    @Override
    public String[] getConversionCommands() {
        return new String[] {
                "read_graph -format g " + getFileName(),
                "get_scover",
                "fsm_collapsing",
                "sync_fsm",
                "write_fsms_to_petrify_format -format sg -multiplefiles 1",
                "quit"};
    }

    @Override
    public String getDisplayName() {
        return "Synchronised FSMs [MSFSM]";
    }

}
