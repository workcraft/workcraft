package org.workcraft.dom.references;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.utils.DialogUtils;
import org.workcraft.types.TwoWayMap;

import java.util.HashMap;
import java.util.Map;

public class DefaultNameManager implements NameManager {

    private final Map<String, Integer> prefixCount = new HashMap<>();
    private final TwoWayMap<String, Node> nodes = new TwoWayMap<>();

    @Override
    public String getPrefix(Node node) {
        String result = Annotations.getIdentifierPrefix(node.getClass());
        if (result != null) {
            return result;
        }
        if (node instanceof Connection) return Identifier.createInternal("c");
        if (node instanceof Container) return Identifier.createInternal("group");
        return "node";
    }

    @Override
    public void setPrefixCount(String prefix, Integer count) {
        prefixCount.put(prefix, count);
    }

    @Override
    public Integer getPrefixCount(String prefix) {
        if (prefixCount.containsKey(prefix)) {
            return prefixCount.get(prefix);
        } else {
            return 0;
        }
    }

    @Override
    public void setName(Node node, String name) {
        Node occupant = getNode(name);
        if (node != occupant) {
            if (!isUnusedName(name)) {
                System.out.println("this = " + this);
                String derivedName = getDerivedName(occupant, name);
                String msg = "The name '" + name + "' is already taken by another node.\n" +
                        "Rename that node to '" + derivedName + "' and continue?";
                if (!DialogUtils.showConfirmWarning(msg)) {
                    return;
                }
                setName(occupant, derivedName);
            }
            if (!isUnusedName(name)) {
                throw new ArgumentException("The name '" + name + "' is unavailable.");
            }
            if (!Identifier.isName(name) && !Identifier.isInternal(name)) {
                throw new ArgumentException("The name '" + name + "' is invalid identifier.");
            }
            nodes.removeValue(node);
            nodes.put(name, node);
        }
    }

    @Override
    public String getName(Node node) {
        return nodes.getKey(node);
    }

    @Override
    public boolean isNamed(Node node) {
        return nodes.getKey(node) != null;
    }

    @Override
    public boolean isUnusedName(String name) {
        return getNode(name) == null;
    }

    @Override
    public Node getNode(String name) {
        return nodes.getValue(name);
    }

    @Override
    public void remove(Node node) {
        if (nodes.getKey(node) != null) {
            nodes.removeValue(node);
        }
    }

    @Override
    public void setDefaultName(Node node) {
        String prefix = getPrefix(node);
        Integer count = getPrefixCount(prefix);
        String name;
        do {
            name = prefix + count++;
        } while (!isUnusedName(name));
        setPrefixCount(prefix, count);
        setName(node, name);
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if (!nodes.containsValue(node)) {
            setDefaultName(node);
        }
    }

    @Override
    public String getDerivedName(Node node, String candidate) {
        String result = candidate;
        int code = 0;
        while (!isUnusedName(result)) {
            result = candidate + codeToString(code);
            code++;
        }
        return result;
    }

    private static String codeToString(int code) {
        String result = "";
        do {
            result += (char) ('a' + code % 26);
            code /= 26;
        } while (code > 0);
        return result;
    }

}
