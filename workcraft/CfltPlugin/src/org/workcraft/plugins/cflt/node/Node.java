package org.workcraft.plugins.cflt.node;

public class Node {
    private final String leftChildName;
    private final String rightChildName;
    private Operator operator;

    public Node(String leftChildName, String rightChildName, Operator operator) {
        this.leftChildName = leftChildName;
        this.rightChildName = rightChildName;
        this.operator = operator;
    }

    public String getLeftChildName() {
        return this.leftChildName;
    }

    public String getRightChildName() {
        return this.rightChildName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}
