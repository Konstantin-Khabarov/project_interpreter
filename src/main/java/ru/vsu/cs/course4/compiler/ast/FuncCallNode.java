package ru.vsu.cs.course4.compiler.ast;

import java.util.*;

public class FuncCallNode implements ExprNode, StmtNode {
    private IdentNode func = null;
    private List<ExprNode> params = null;

    public FuncCallNode(IdentNode func, Collection<ExprNode> params) {
        this.func = func;
        if (params != null) {
            this.params = new ArrayList<>();
            this.params.addAll(params);
        }
    }

    @Override
    public Collection<? extends ExprNode> childs() {
        return params;
    }

    @Override
    public String toString() {
        return func.toString() + "()";
    }

    public IdentNode getFunc() {
        return func;
    }

    public List<ExprNode> getParams() {
        return Collections.unmodifiableList(params);
    }
}