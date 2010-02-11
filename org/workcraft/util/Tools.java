package org.workcraft.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.PluginInfo;
import org.workcraft.Tool;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.PluginInstantiationException;

public class Tools {
	public static ListMap<String, Pair<String, Tool>> getTools(Model model, Framework framework) {
		PluginInfo[] toolsInfo = framework.getPluginManager().getPluginsImplementing(Tool.class.getName());
		ListMap<String, Pair<String, Tool>> toolSections = new ListMap<String, Pair<String, Tool>>();

		for (PluginInfo info : toolsInfo) {
			try {
				Tool tool = (Tool) framework.getPluginManager().getSingleton(info);

				if (!tool.isApplicableTo(model))
					continue;

				toolSections.put(tool.getSection(), new Pair <String,Tool> (info.getDisplayName(), tool));

			} catch (PluginInstantiationException e1) {
				throw new RuntimeException (e1);
			}
		}

		return toolSections;
	}

	public static List<String> getSections (ListMap<String, Pair<String, Tool>> tools) {
		LinkedList<String> list = new LinkedList<String>(tools.keySet());
		Collections.sort(list);
		return list;
	}

	public static List<Pair<String,Tool>> getSectionTools (String section, ListMap<String, Pair<String, Tool>> tools) {
		List<Pair<String,Tool>> sectionTools = tools.get(section);

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