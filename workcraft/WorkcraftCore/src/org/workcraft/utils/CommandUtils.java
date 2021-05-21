package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.commands.MenuOrdering.Position;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.PluginManager;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;
import java.util.function.Function;

public class CommandUtils {

    public static String makePromotedSectionTitle(String title, int order) {
        return "!" + TextUtils.repeat(" ", 9 - order) + title;
    }

    public static List<Command> getCommands(Function<Command, Boolean> filter) {
        ArrayList<Command> result = new ArrayList<>();
        final PluginManager pm = Framework.getInstance().getPluginManager();
        for (Command command : pm.getCommands()) {
            if (filter.apply(command)) {
                result.add(command);
            }
        }
        return result;
    }

    public static <T extends Command> List<T> getCommands(Class<T> type) {
        ArrayList<T> result = new ArrayList<>();
        final PluginManager pm = Framework.getInstance().getPluginManager();
        for (Command command : pm.getCommands()) {
            try {
                result.add(type.cast(command));
            } catch (ClassCastException e) {
            }
        }
        return result;
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
        sectionCommands.sort((o1, o2) -> {
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

    public static void run(MainWindow mainWindow, Command command) {
        if (mainWindow != null) {
            GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
            if (currentEditor != null) {
                WorkspaceEntry we = currentEditor.getWorkspaceEntry();
                checkCommandApplicability(we, command);
                command.run(we);
            }
        }
    }

    public static void run(WorkspaceEntry we, String className) {
        Command command = findMatchingCommand(className, Command.class);
        checkCommandApplicability(we, command);
        command.run(we);
    }


    public static <R> R execute(WorkspaceEntry we, String className) {
        ScriptableCommand<R> command = findMatchingCommand(className, ScriptableCommand.class);
        checkCommandApplicability(we, command);
        return command.execute(we);
    }

    public static <R, D> R execute(WorkspaceEntry we, String className, String serialisedData) {
        ScriptableDataCommand<R, D> command = CommandUtils.findMatchingCommand(className, ScriptableDataCommand.class);
        checkCommandApplicability(we, command);
        D data = command.deserialiseData(serialisedData);
        return command.execute(we, data);
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

    public static <T extends Command> T findMatchingCommand(String className, Class<T> type) {
        if ((className == null) || className.isEmpty()) {
            throw new RuntimeException("Undefined command name.");
        }
        for (Command command : getCommands(type)) {
            Class<? extends Command> cls = command.getClass();
            if (className.equals(cls.getSimpleName()) || className.endsWith(cls.getName())) {
                return type.cast(command);
            }
        }
        throw new RuntimeException("Command '" + className + "' is not found.");
    }

}
