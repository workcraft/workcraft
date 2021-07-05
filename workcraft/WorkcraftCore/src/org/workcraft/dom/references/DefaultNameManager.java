package org.workcraft.dom.references;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.types.TwoWayMap;
import org.workcraft.utils.DialogUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultNameManager implements NameManager {

    private final Map<String, Integer> prefixCount = new HashMap<>();
    private final TwoWayMap<String, Node> nodes = new TwoWayMap<>();

    @Override
    public String getPrefix(Node node) {
        String result = Annotations.getIdentifierPrefix(node.getClass());
        if (result == null) {
            if (node instanceof Connection) {
                result = Identifier.makeInternal("c");
            } else if (node instanceof Container) {
                result = Identifier.makeInternal("group");
            } else {
                result = "node";
            }
        }
        if (node instanceof NamespaceProvider) {
            result = Identifier.appendNamespaceSeparator(result);
        }
        return result;
    }

    @Override
    public void setPrefixCount(String prefix, Integer count) {
        prefixCount.put(prefix, count);
    }

    @Override
    public Integer getPrefixCount(String prefix) {
        return prefixCount.getOrDefault(prefix, 0);
    }

    @Override
    public void setName(Node node, String name, boolean force) {
        if (node instanceof NamespaceProvider) {
            name = Identifier.appendNamespaceSeparator(name);
        }
        Node occupant = getNode(name);
        if (node == occupant) {
            return;
        }
        if (isUnusedName(name)) {
            nodes.removeValue(node);
            nodes.put(name, node);
        } else {
            String msg = "Name '" + name + "' is already taken by another node.";
            if (force) {
                DialogUtils.showError(msg);
            } else {
                String derivedName = getDerivedName(occupant, name);
                msg += "\nRename that node to '" + derivedName + "' and continue?";
                if (DialogUtils.showConfirmWarning(msg)) {
                    setName(occupant, derivedName, true);
                    setName(node, name, true);
                }
            }
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
            name = Identifier.compose(prefix, (count++).toString());
        } while (!isUnusedName(name));
        setPrefixCount(prefix, count);
        setName(node, name, true);
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if (!nodes.containsValue(node)) {
            setDefaultName(node);
        }
    }

    @Override
    public String getDerivedName(Node node, String candidate) {
        if (node instanceof NamespaceProvider) {
            candidate = Identifier.appendNamespaceSeparator(candidate);
        }
        String result = candidate;
        int code = 0;
        while (!isUnusedName(result)) {
            result = Identifier.compose(candidate, codeToString(code));
            code++;
        }
        return result;
    }

    private static String codeToString(int code) {
        StringBuilder result = new StringBuilder();
        do {
            result.append((char) ('a' + code % 26));
            code /= 26;
        } while (code > 0);
        return result.toString();
    }

}
