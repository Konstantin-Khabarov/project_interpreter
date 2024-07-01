package ru.vsu.cs.course4.compiler.ast;

public class ArrayAccessNode implements ExprNode {
    private final ExprNode array;
    private final ExprNode index;

    public ArrayAccessNode(ExprNode array, ExprNode index) {
        this.array = array;
        this.index = index;
    }

    public ExprNode getArray() {
        return array;
    }

    public ExprNode getIndex() {
        return index;
    }

    @Override
    public String toString() {return array.toString() + "[" + index.toString() + "]";}
}