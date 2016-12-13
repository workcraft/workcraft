package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class Commands {

    public static void run(WorkspaceEntry we, Command command) {
        if (command.isApplicableTo(we.getModelEntry())) {
            command.run(we);
        } else {
            String errorMessage = "Attempt to run incompatible command " +
                    command.getClass().getName() + " to a workspace entry " + we.getWorkspacePath();

            throw new RuntimeException(errorMessage);
        }
    }

    public static List<Command> getApplicableCommands(ModelEntry me) {
        ArrayList<Command> commands = new ArrayList<>();
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        Collection<PluginInfo<? extends Command>> commandPlugins = pm.getPlugins(Command.class);
        for (PluginInfo<? extends Command> info : commandPlugins) {
            Command command = info.getSingleton();
            if (command.isApplicableTo(me)) {
                commands.add(command);
            }
        }
        return commands;
    }

    public static List<String> getSections(List<Command> commands) {
        HashSet<String> sections = new HashSet<>();
        for (Command command: commands) {
            sections.add(command.getSection());
        }
        LinkedList<String> seortedSections = new LinkedList<>(sections);
        Collections.sort(seortedSections);
        return seortedSections;
    }

    public static List<Command> getSectionCommands(String section, List<Command> commands) {
        List<Command> sectionCommands = new ArrayList<>();
        for (Command command: commands) {
            if (command.getSection().equals(section)) {
                sectionCommands.add(command);
            }
        }
        Collections.sort(sectionCommands, new Comparator<Command>() {
            @Override
            public int compare(Command o1, Command o2) {
                Integer p1 = (o1 instanceof MenuOrdering) ? ((MenuOrdering) o1).getPriority() : 0;
                Integer p2 = (o2 instanceof MenuOrdering) ? ((MenuOrdering) o2).getPriority() : 0;
                int result = -p1.compareTo(p2); // Reverse the order, so low values correspond to lower priority
                if (result == 0) {
                    result = o1.getDisplayName().compareTo(o2.getDisplayName());
                }
                return result;
            }
        });
        return sectionCommands;
    }

    public static List<Command> getPositionedSectionCommands(String section, List<Command> commands, Position position) {
        return getSectionCommands(section, getPositionedCommands(commands, position));
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

    public static List<Command> getUnpositionedSectionCommands(String section, List<Command> commands) {
        return getSectionCommands(section, getUnpositionedCommands(commands));
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

}
