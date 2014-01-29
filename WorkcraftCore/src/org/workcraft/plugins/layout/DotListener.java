package org.workcraft.plugins.layout;

import java.util.Map;

public interface DotListener {
	void node(String id, Map<String, String> properties);
	void arc(String from, String to, Map<String, String> properties);
}
