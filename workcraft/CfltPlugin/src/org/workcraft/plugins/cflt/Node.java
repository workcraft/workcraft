package org.workcraft.plugins.cflt;

public class Node {

    private String left;
    private String right;
    private Operator operator;

    public Node(String left, String right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public String getLeft() {
        return left;
    }
    public void setLEft(String left) {
        this.left = left;
    }
    public String getRight() {
        return right;
    }
    public void setB(String right) {
        this.right = right;
    }
    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}
