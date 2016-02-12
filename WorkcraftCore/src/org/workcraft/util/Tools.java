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
import org.workcraft.Tool;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.workspace.WorkspaceEntry;

public class Tools {

    public static void run(WorkspaceEntry we, Tool tool) {
        if (tool.isApplicableTo(we)) {
            tool.run(we);
        } else {
            String errorMessage = "Attempt to apply incompatible tool " +
                    tool.getClass().getName() + " to a workspace entry " + we.getWorkspacePath();

            throw new RuntimeException(errorMessage);
        }
    }


    public static List<Tool> getApplicableTools(WorkspaceEntry we) {
        ArrayList<Tool> tools = new ArrayList<>();

        final Framework framework = Framework.getInstance();
        Collection<PluginInfo<? extends Tool>> toolPlugins = framework.getPluginManager().getPlugins(Tool.class);
        for (PluginInfo<? extends Tool> info : toolPlugins) {
            Tool tool = info.getSingleton();
            if (tool.isApplicableTo(we)) {
                tools.add(tool);
            }
        }
        return tools;
    }

    public static List<String> getSections(List<Tool> tools) {
        HashSet<String> sections = new HashSet<>();
        for (Tool tool: tools) {
            sections.add(tool.getSection());
        }
        LinkedList<String> seortedSections = new LinkedList<>(sections);
        Collections.sort(seortedSections);
        return seortedSections;
    }

    public static List<Tool> getSectionTools(String section, List<Tool> tools) {
        List<Tool> sectionTools = new ArrayList<>();
        for (Tool tool: tools) {
            if (tool.getSection().equals(section)) {
                sectionTools.add(tool);
            }
        }
        Collections.sort(sectionTools, new Comparator<Tool>() {
            @Override
            public int compare(Tool o1, Tool o2) {
                Integer p1 = (o1 instanceof MenuOrdering) ? ((MenuOrdering)o1).getPriority() : 0;
                Integer p2 = (o2 instanceof MenuOrdering) ? ((MenuOrdering)o2).getPriority() : 0;
                int result = -p1.compareTo(p2); // Reverse the order, so low values correspond to lower priority
                if (result == 0) {
                    result = o1.getDisplayName().compareTo(o2.getDisplayName());
                }
                return result;
            }
        });
        return sectionTools;
    }

    public static List<Tool> getPositionedSectionTools(String section, List<Tool> tools, Position position) {
        return getSectionTools(section, getPositionedTools(tools, position));
    }

    public static List<Tool> getPositionedTools(List<Tool> tools, Position position) {
        List<Tool> result = new ArrayList<>();
        for (Tool tool: tools) {
            if ((tool instanceof MenuOrdering) && (((MenuOrdering)tool).getPosition() == position)) {
                result.add(tool);
            }
        }
        return result;
    }

    public static List<Tool> getUnpositionedSectionTools(String section, List<Tool> tools) {
        return getSectionTools(section, getUnpositionedTools(tools));
    }

    public static List<Tool> getUnpositionedTools(List<Tool> tools) {
        List<Tool> result = new ArrayList<>();
        for (Tool tool: tools) {
            if (!(tool instanceof MenuOrdering) || (((MenuOrdering)tool).getPosition() == null)) {
                result.add(tool);
            }
        }
        return result;
    }

}
