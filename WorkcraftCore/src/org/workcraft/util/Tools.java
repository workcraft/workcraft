package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
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

			throw new RuntimeException (errorMessage);
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
		List<Tool> sectionTools = new ArrayList<Tool>();
		for (Tool tool: tools) {
			if (tool.getSection().equals(section)) {
				sectionTools.add(tool);
			}
		}
		Collections.sort(sectionTools, new Comparator<Tool>() {
			@Override
			public int compare(Tool o1, Tool o2) {
				return (o1.getDisplayName().compareTo(o2.getDisplayName()));
			}
		});
		return sectionTools;
	}

}