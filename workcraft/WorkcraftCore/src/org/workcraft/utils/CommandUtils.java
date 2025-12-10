package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.commands.*;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.PluginManager;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;

public class CommandUtils {

    public static List<Command> getCommands(Function<Command, Boolean> filter) {
        List<Command> result = new ArrayList<>();
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
            Command.Visibility visibility = command.getVisibility();
            return (visibility == Command.Visibility.ALWAYS)
                    || ((visibility == Command.Visibility.APPLICABLE) && command.isApplicableTo(we));
        });
    }

    public static List<Command> getApplicableVisiblePopupCommands(WorkspaceEntry we, VisualNode node) {
        return getCommands(command -> {
            if ((command instanceof NodeTransformer nodeTransformer)
                    && command.isApplicableTo(we)
                    && nodeTransformer.isApplicableTo(node)) {

                Command.Visibility visibility = command.getVisibility();
                return (visibility == Command.Visibility.ALWAYS)
                        || (visibility == Command.Visibility.APPLICABLE)
                        || (visibility == Command.Visibility.APPLICABLE_POPUP_ONLY);
            }
            return false;
        });
    }

    public static List<String> getOrderedCategoryNames(List<Command> commands) {
        Map<String, Integer> categoryNameToPriorityMap = new HashMap<>();
        for (Command command : commands) {
            Command.Category category = command.getCategory();
            if (category == null) {
                category = Command.DEFAULT_CATEGORY;
            }
            int priority = category.getPriority();
            String categoryName = category.getName();
            if ((categoryName == null) || categoryName.isEmpty()) {
                categoryName = Command.DEFAULT_CATEGORY.getName();
                priority = Command.DEFAULT_CATEGORY.getPriority();
            }
            categoryNameToPriorityMap.put(categoryName, priority);
        }

        List<String> result = new ArrayList<>(categoryNameToPriorityMap.keySet());
        result.sort((s1, s2) -> {
            Integer p1 = categoryNameToPriorityMap.getOrDefault(s1, 0);
            Integer p2 = categoryNameToPriorityMap.getOrDefault(s2, 0);
            int r = -p1.compareTo(p2); // Reverse the order, so low values correspond to lower priority
            return (r == 0) ? s1.compareTo(s2) : r;
        });
        return result;
    }

    private static List<Entry> getOrderedSectionsAndUnsectionedCommands(String categoryName, List<Command> commands) {
        if ((categoryName == null) || categoryName.isEmpty()) {
            categoryName = Command.DEFAULT_CATEGORY.getName();
        }
        Set<Entry> entries = new HashSet<>();
        Map<String, Command.Section> nameToSectionMap = new HashMap<>();
        for (Command command : commands) {
            Command.Category commandCategory = command.getCategory();
            if (commandCategory == null) {
                commandCategory = Command.DEFAULT_CATEGORY;
            }
            String commandCategoryName = commandCategory.getName();
            if ((commandCategoryName == null) || commandCategoryName.isEmpty()) {
                commandCategoryName = Command.DEFAULT_CATEGORY.getName();
            }
            if (categoryName.equals(commandCategoryName)) {
                Command.Section section = command.getSection();
                if (section != null) {
                    // Keep only one menu section with the same name
                    nameToSectionMap.put(section.getDisplayName(), section);
                } else {
                    entries.add(command);
                }
            }
        }
        List<Entry> result = new ArrayList<>(entries);
        result.addAll(nameToSectionMap.values());
        result.sort(CommandUtils::entryComparator);
        return result;
    }

    private static int entryComparator(Entry entry1, Entry entry2) {
        if ((entry1 == null) && (entry2 == null)) {
            return 0;
        }
        if (entry1 == null) {
            return 1;
        }
        if (entry2 == null) {
            return -1;
        }
        Integer priority1 = entry1.getPriority();
        Integer priority2 = entry2.getPriority();
        int result = -priority1.compareTo(priority2); // Reverse the order, so low values correspond to lower priority
        if (result == 0) {
            result = entry1.getDisplayName().compareTo(entry2.getDisplayName());
        }
        return result;
    }

    public static JMenu createCommandsMenu(String categoryName, List<Command> applicableVisibleCommands) {
        JMenu result = new JMenu(categoryName.trim());
        List<Entry> orderedEntries = getOrderedSectionsAndUnsectionedCommands(categoryName, applicableVisibleCommands);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        boolean isFirstMenuItem = true;
        for (Command.Position position : getOrderedPositionsWithNull()) {
            boolean isFirstPositionItem = true;
            for (Entry entry : orderedEntries) {
                if (position != entry.getPosition()) continue;
                if (!isFirstMenuItem && isFirstPositionItem) {
                    result.addSeparator();
                }
                if (entry instanceof Command.Section section) {
                    String sectionName = section.getDisplayName();
                    List<Command> orderedSectionCommands
                            = getOrderedSectionCommands(categoryName, sectionName, applicableVisibleCommands);

                    if (orderedSectionCommands.isEmpty()) continue;
                    // Dissolve the section if it contains a single command
                    if (orderedSectionCommands.size() == 1) {
                        Command command = orderedSectionCommands.get(0);
                        Action action = new Action(command.getDisplayName().trim(), () -> run(mainWindow, command));
                        result.add(new ActionMenuItem(action));
                    } else {
                        JMenu sectionSubmenu = new JMenu(sectionName.trim());
                        addCommandMenuSection(sectionSubmenu, orderedSectionCommands);
                        result.add(sectionSubmenu);
                    }
                    isFirstMenuItem = false;
                    isFirstPositionItem = false;
                }
                if (entry instanceof Command command) {
                    Action action = new Action(command.getDisplayName().trim(), () -> run(mainWindow, command));
                    result.add(new ActionMenuItem(action));
                    isFirstMenuItem = false;
                    isFirstPositionItem = false;
                }
            }
        }
        return result;
    }

    private static List<Command> getOrderedSectionCommands(String categoryName, String sectionName, List<Command> commands) {
        List<Command> result = new ArrayList<>();
        if ((categoryName == null) || categoryName.isEmpty()) {
            categoryName = Command.DEFAULT_CATEGORY.getName();
        }
        for (Command command : commands) {
            String commandCategoryName = command.getCategory().getName();
            if ((commandCategoryName == null) || commandCategoryName.isEmpty()) {
                commandCategoryName = Command.DEFAULT_CATEGORY.getName();
            }
            if (categoryName.equals(commandCategoryName)) {
                Command.Section commandSection = command.getSection();
                if (((sectionName != null) && (commandSection != null)
                        && sectionName.equals(commandSection.getDisplayName()))
                        || (sectionName == null) && (commandSection == null)) {

                    result.add(command);
                }
            }
        }
        result.sort((c1, c2) -> {
            Integer p1 = c1.getPriority();
            Integer p2 = c2.getPriority();
            int r = -p1.compareTo(p2); // Reverse the order, so low values correspond to lower priority
            return (r == 0) ? c1.getDisplayName().compareTo(c2.getDisplayName()) : r;
        });
        return result;
    }

    private static void addCommandMenuSection(JMenu menu, List<Command> commands) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        boolean isFirstMenuItem = true;
        for (Command.Position position : getOrderedPositionsWithNull()) {
            boolean isFirstPositionItem = true;
            for (Command command : commands) {
                if (position != command.getPosition()) continue;
                if (!isFirstMenuItem && isFirstPositionItem) {
                    menu.addSeparator();
                }
                Action action = new Action(command.getDisplayName().trim(), () -> run(mainWindow, command));
                menu.add(new ActionMenuItem(action));
                isFirstMenuItem = false;
                isFirstPositionItem = false;
            }

        }
    }

    private static List<Command.Position> getOrderedPositionsWithNull() {
        List<Command.Position> result = new ArrayList<>(List.of(Command.Position.values()));
        result.add(0, null);
        return result;
    }

    public static void run(MainWindow mainWindow, Command command) {
        if (mainWindow != null) {
            GraphEditor currentEditor = mainWindow.getCurrentEditor();
            WorkspaceEntry we = (currentEditor == null) ? null : currentEditor.getWorkspaceEntry();
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
