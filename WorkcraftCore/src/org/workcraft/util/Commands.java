package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.commands.Command;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.PluginManager;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.workspace.WorkspaceEntry;

public class Commands {

    public static List<Command> getCommands() {
        ArrayList<Command> result = new ArrayList<>();
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        Collection<PluginInfo<? extends Command>> commandPlugins = pm.getPlugins(Command.class);
        for (PluginInfo<? extends Command> info: commandPlugins) {
            Command command = info.getSingleton();
            result.add(command);
        }
        return result;
    }

    public static List<Command> getCommands(Function<Command, Boolean> filter) {
        ArrayList<Command> result = new ArrayList<>();
        for (Command command: getCommands()) {
            if (filter.apply(command)) {
                result.add(command);
            }
        }
        return result;
    }

    public static List<Command> getApplicableCommands(WorkspaceEntry we) {
        return getCommands(command -> command.isApplicableTo(we));
    }

    public static List<Command> getApplicableVisibleCommands(WorkspaceEntry we) {
        return getCommands(command -> command.isApplicableTo(we) && command.isVisibleInMenu());
    }

    public static List<String> getSections(List<Command> commands) {
        HashSet<String> sections = new HashSet<>();
        for (Command command: commands) {
            sections.add(command.getSection());
        }
        LinkedList<String> sortedSections = new LinkedList<>(sections);
        Collections.sort(sortedSections);
        return sortedSections;
    }

    public static List<Command> getSectionCommands(String section, List<Command> commands) {
        List<Command> sectionCommands = new ArrayList<>();
        for (Command command: commands) {
            if (command.getSection().equals(section)) {
                sectionCommands.add(command);
            }
        }
        Collections.sort(sectionCommands, (o1, o2) -> {
            Integer p1 = (o1 instanceof MenuOrdering) ? ((MenuOrdering) o1).getPriority() : 0;
            Integer p2 = (o2 instanceof MenuOrdering) ? ((MenuOrdering) o2).getPriority() : 0;
            int result = -p1.compareTo(p2); // Reverse the order, so low values correspond to lower priority
            if (result == 0) {
                result = o1.getDisplayName().compareTo(o2.getDisplayName());
            }
            return result;
        });
        return sectionCommands;
    }

    public static List<Command> getPositionedCommands(List<Command> commands, Position position) {
        List<Command> result = new ArrayList<>();
        for (Command command: commands) {
            if ((command instanceof MenuOrdering) && (((MenuOrdering) command).getPosition() == position)) {
                result.add(command);
            }
        }
        return result;
    }

    public static List<Command> getUnpositionedCommands(List<Command> commands) {
        List<Command> result = new ArrayList<>();
        for (Command command: commands) {
            if (!(command instanceof MenuOrdering) || (((MenuOrdering) command).getPosition() == null)) {
                result.add(command);
            }
        }
        return result;
    }

    public static void run(WorkspaceEntry we, Command command) {
        checkCommandApplicability(we, command);
        command.run(we);
    }

    public static <T> T execute(WorkspaceEntry we, ScriptableCommand<T> command) {
        checkCommandApplicability(we, command);
        return command.execute(we);
    }

    private static void checkCommandApplicability(WorkspaceEntry we, Command command) {
        if (!command.isApplicableTo(we)) {
            String commandName = command.getClass().getSimpleName();
            String displayName = we.getModelEntry().getDescriptor().getDisplayName();
            Path<String> workspacePath = we.getWorkspacePath();
            throw new RuntimeException("Command '" + commandName + "' is incompatible with "
                    + displayName + " (workspace entry '" + workspacePath + "').");
        }
    }

}
