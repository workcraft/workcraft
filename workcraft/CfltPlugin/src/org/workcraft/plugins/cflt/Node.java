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

    public void setLeftChildName(String leftChildName) {
        this.leftChildName = leftChildName;
    }

    public String getRightChildName() {
        return this.rightChildName;
    }

    public void setRightChildName(String rightChildName) {
        this.rightChildName = rightChildName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

}
