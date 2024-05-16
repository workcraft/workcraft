package org.workcraft.plugins.cflt;

public class Node {
    private String leftChildName;
    private String rightChildName;
    private Operator operator;

    public Node(String leftChildName, String rightChildName, Operator operator) {
        this.leftChildName = leftChildName;
        this.rightChildName = rightChildName;
        this.operator = operator;
    }

    public String getLeftChildName() {
        return this.leftChildName;
    }

    public void setLeftChildName(String leftChild) {
        this.leftChildName = leftChild;
    }

    public String getRightChildName() {
        return this.rightChildName;
    }

    public void setRightChildName(String rightChild) {
        this.rightChildName = rightChild;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

}
