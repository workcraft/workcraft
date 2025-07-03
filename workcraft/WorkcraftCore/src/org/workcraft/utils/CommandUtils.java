package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.commands.*;
import org.workcraft.commands.MenuOrdering.Position;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.PluginManager;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class CommandUtils {

    public static String makePromotedSectionTitle(String title, int order) {
        return '!' + TextUtils.repeat(" ", 9 - order) + title;
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
            } catch (ClassCastException ignored) {
            }
        }
        return result;
    }

    public static List<Command> getApplicableVisibleCommands(WorkspaceEntry we) {
        return getCommands(command -> {
            Command.MenuVisibility menuVisibility = command.getMenuVisibility();
            return (menuVisibility == Command.MenuVisibility.ALWAYS)
                    || ((menuVisibility == Command.MenuVisibility.APPLICABLE) && command.isApplicableTo(we));
        });
    }

    public static List<Command> getApplicableVisiblePopupCommands(WorkspaceEntry we, VisualNode node) {
        return getCommands(command -> {
            if ((command instanceof NodeTransformer nodeTransformer)
                    && command.isApplicableTo(we)
                    && (nodeTransformer.isApplicableTo(node))) {

                Command.MenuVisibility menuVisibility = command.getMenuVisibility();
                return (menuVisibility == Command.MenuVisibility.ALWAYS)
                        || (menuVisibility == Command.MenuVisibility.APPLICABLE)
                        || (menuVisibility == Command.MenuVisibility.APPLICABLE_POPUP_ONLY);
            }
            return false;
        });
    }

    public static List<String> getSections(List<Command> commands) {
        HashSet<String> sections = new HashSet<>();
        for (Command command: commands) {
            sections.add(command.getSection());
        }
        return SortUtils.getSortedNatural(sections);
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
            GraphEditor currentEditor = mainWindow.getCurrentEditor();
            WorkspaceEntry we = currentEditor == null ? null : currentEditor.getWorkspaceEntry();
            checkCommandApplicability(we, command);
            command.run(we);
        }
    }

    public static void run(WorkspaceEntry we, String className) {
        Command command = findMatchingCommand(className, Command.class);
        checkCommandApplicability(we, command);
        command.run(we);
    }


    @SuppressWarnings("unchecked")
    public static <R> R execute(WorkspaceEntry we, String className) {
        ScriptableCommand<R> command = findMatchingCommand(className, ScriptableCommand.class);
        checkCommandApplicability(we, command);
        return command.execute(we);
    }

    @SuppressWarnings("unchecked")
    public static <R, D> R execute(WorkspaceEntry we, String className, String serialisedData) {
        ScriptableDataCommand<R, D> command = CommandUtils.findMatchingCommand(className, ScriptableDataCommand.class);
        checkCommandApplicability(we, command);
        D data = command.deserialiseData(serialisedData);
        return command.execute(we, data);
    }

    private static void checkCommandApplicability(WorkspaceEntry we, Command command) {
        if (!command.isApplicableTo(we)) {
            String commandName = command.getClass().getSimpleName();
            if (we == null) {
                throw new RuntimeException("Command '" + commandName + "' needs active workspace entry.");
            } else {
                String displayName = we.getModelEntry().getDescriptor().getDisplayName();
                Path<String> workspacePath = we.getWorkspacePath();
                throw new RuntimeException("Command '" + commandName + "' is incompatible with "
                        + displayName + " (workspace entry '" + workspacePath + "').");
            }
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
