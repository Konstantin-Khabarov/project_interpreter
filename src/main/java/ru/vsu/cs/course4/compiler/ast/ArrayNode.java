package ru.vsu.cs.course4.compiler.ast;

import java.util.Collection;
import java.util.List;

public class ArrayNode implements ExprNode {
    private final List<ExprNode> elements;

    public ArrayNode(List<ExprNode> elements) {
        this.elements = elements;
    }

    public List<ExprNode> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "[...]";
    }

    @Override
    public Collection<? extends AstNode> childs() {
        return elements;
    }
}