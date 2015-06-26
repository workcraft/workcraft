package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.workspace.WorkspaceEntry;

public class Tools {

	public static ListMap<String, Pair<String, Tool>> getTools(WorkspaceEntry we) {
		ListMap<String, Pair<String, Tool>> toolSections = new ListMap<String, Pair<String, Tool>>();

		final Framework framework = Framework.getInstance();
		Collection<PluginInfo<? extends Tool>> toolPlugins = framework.getPluginManager().getPlugins(Tool.class);
		for (PluginInfo<? extends Tool> info : toolPlugins) {
			Tool tool = info.getSingleton();
			if (tool.isApplicableTo(we)) {
				String toolDisplayName = tool.getDisplayName();
				Pair<String, Tool> toolDescription = new Pair <String,Tool>(toolDisplayName, tool);
				toolSections.put(tool.getSection(), toolDescription);
			}
		}
		return toolSections;
	}

	public static void run(WorkspaceEntry we, Tool tool) {
		if (tool.isApplicableTo(we))
			tool.run(we);
		else {
			String errorMessage = "Attempt to apply incompatible tool " +
				tool.getClass().getName() + " to a workspace entry " + we.getWorkspacePath();

			throw new RuntimeException (errorMessage);
		}
	}

	public static List<String> getSections(ListMap<String, Pair<String, Tool>> tools) {
		LinkedList<String> list = new LinkedList<String>(tools.keySet());
		Collections.sort(list);
		return list;
	}

	public static List<Pair<String,Tool>> getSectionTools(String section, ListMap<String, Pair<String, Tool>> tools) {
		List<Pair<String,Tool>> sectionTools = new ArrayList<Pair<String, Tool>>(tools.get(section));

		Collections.sort(sectionTools, new Comparator<Pair<String,Tool>>() {
			@Override
			public int compare(Pair<String, Tool> o1,
					Pair<String, Tool> o2) {
				return (o1.getFirst().compareTo(o2.getFirst()));
			}
		});

		return sectionTools;
	}

}