package org.workcraft.commands;

public interface DataCommand<D> extends Command {
    D deserialiseData(String data);
}
