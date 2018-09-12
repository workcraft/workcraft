package org.workcraft.plugins.circuit

import org.workcraft.dom.Node
import org.workcraft.dom.math.MathConnection
import org.workcraft.dom.math.MathNode
import java.util.*

@JvmOverloads
fun getPresetComponents(circuit: Circuit, curNode: MathNode, skipZeroDelay: Boolean = false): HashSet<CircuitComponent> {
    val result = HashSet<CircuitComponent>()
    val visited = HashSet<Node>()
    val queue = LinkedList<Node>()
    if (curNode is CircuitComponent) {
        queue.addAll(curNode.inputs)
    } else {
        queue.add(curNode)
    }

    while (!queue.isEmpty()) {
        val node = queue.remove()
        if (node == null || visited.contains(node)) continue
        visited.add(node)
        when (node) {
            is CircuitComponent ->
                if (skipZeroDelay && node is FunctionComponent && node.isZeroDelay) {
                    queue.addAll(node.inputs)
                } else {
                    result.add(node)
                }
            is Contact ->
                if (node.isPort == node.isOutput) {
                    queue.addAll(circuit.getPreset(node))
                } else {
                    queue.add(node.parent)
                }
            is Joint ->
                queue.addAll(circuit.getPreset(node))
            is MathConnection ->
                queue.add(node.first)
        }
    }
    return result
}

@JvmOverloads
fun getPostsetComponents(circuit: Circuit, curNode: MathNode, skipZeroDelay: Boolean = false): HashSet<CircuitComponent> {
    val result = HashSet<CircuitComponent>()
    val visited = HashSet<Node>()
    val queue = LinkedList<Node>()
    if (curNode is CircuitComponent) {
        queue.addAll(curNode.outputs)
    } else {
        queue.add(curNode)
    }

    while (!queue.isEmpty()) {
        val node = queue.remove()
        if (node == null || visited.contains(node)) continue
        visited.add(node)
        when (node) {
            is CircuitComponent ->
                if (skipZeroDelay && node is FunctionComponent && node.isZeroDelay) {
                    queue.addAll(node.outputs)
                } else {
                    result.add(node)
                }
            is Contact ->
                if (node.isPort == node.isInput) {
                    queue.addAll(circuit.getPostset(node))
                } else {
                    queue.add(node.parent)
                }
            is Joint ->
                queue.addAll(circuit.getPostset(node))
            is MathConnection ->
                queue.add(node.second)
        }
    }
    return result
}

fun getPostsetPorts(circuit: Circuit, curNode: MathNode): HashSet<Contact> {
    val result = HashSet<Contact>()
    val visited = HashSet<Node>()
    val queue = LinkedList<Node>()
    if (curNode is CircuitComponent) {
        queue.addAll(curNode.outputs)
    } else {
        queue.add(curNode)
    }

    while (!queue.isEmpty()) {
        val node = queue.remove()
        if (node == null || visited.contains(node)) continue
        visited.add(node)
        when (node) {
            is Contact ->
                if (node.isOutput && node.isPort) {
                    result.add(node)
                } else {
                    queue.addAll(circuit.getPostset(node))
                }
            is Joint ->
                queue.addAll(circuit.getPostset(node))
            is MathConnection ->
                queue.add(node.second)
        }
    }
    return result
}
