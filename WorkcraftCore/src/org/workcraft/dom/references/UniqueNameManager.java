package org.workcraft.dom.references;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.util.Identifier;
import org.workcraft.util.TwoWayMap;

public class UniqueNameManager implements NameManager {
	private Map<String, Integer> prefixCount = new HashMap<String, Integer>();
	private TwoWayMap<String, Node> nodes = new TwoWayMap<String, Node>();

	public Integer getPrefixCount(String prefix) {
		if (prefixCount.containsKey(prefix)) {
			return prefixCount.get(prefix);
		} else {
			return 0;
		}
	}

	public Integer setPrefixCount(String prefix, Integer count) {
		return prefixCount.put(prefix, count);
	}

	@Override
	public void setDefaultNameIfUnnamed(Node node) {
		if (nodes.containsValue(node)) {
			return;
		}

		String prefix = getPrefix(node);
		Integer count = getPrefixCount(prefix);
		String name;
		do	{
			name = prefix + count++;
		} while (nodes.containsKey(name));
		setPrefixCount(prefix, count);
		nodes.put(name, node);
	}

	public String getNameQuiet(Node node) {
		return nodes.getKey(node);
	}

	@Override
	public boolean isNamed(Node node) {
		String name = getNameQuiet(node);
		if (name == null) {
			return false;
		}
		return true;
	}

	@Override
	public String getName(Node node) {
		String name = getNameQuiet(node);
		if (name == null) {
			throw new NotFoundException("Node \"" + node.toString() + "\" was not issued a name");
		}
		return name;
	}

	@Override
	public void setName(Node node, String name) {
		final Node occupant = nodes.getValue(name);
		if(occupant == node) {
			return;
		}
		if(occupant != null) {
			throw new ArgumentException("The name \"" + name + "\" is already taken. Please choose another name.");
		}

		if (!Identifier.isValid(name)) {
			throw new ArgumentException("\"" + name + "\" is not a valid C-style identifier.\n"
					+ "The first character must be alphabetic or an underscore and the following characters must be alphanumeric or an underscore.");
		}
		nodes.removeValue(node);
		nodes.put(name, node);
	}

	@Override
	public Node get(String name) {
		return nodes.getValue(name);
	}

	@Override
	public void remove(Node node) {
		nodes.removeValue(node);
	}

	@Override
	public String getPrefix(Node node) {
		return "node";
	}

	@Override
	public String generateName(Node node, String candidate) {
		String result = null;
		if (get(candidate) == null) {
			// Name is not busy
			result = candidate;
		} else {
			// Find a non-conflicting suffix
			int code = 0;
			do {
				result = candidate + codeToString(code);
				code++;
			} while (get(result) != null);
		}
		return result;
	}

	private static String codeToString(int code) {
		String result = "";
		do {
			result += (char)('a' + code % 26);
			code /= 26;
		} while (code > 0);
		return result;
	}

}
