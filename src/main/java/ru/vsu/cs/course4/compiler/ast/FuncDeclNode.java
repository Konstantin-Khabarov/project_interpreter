package ru.vsu.cs.course4.compiler.ast;

import java.util.*;

public class FuncDeclNode implements StmtNode {
    private IdentNode name = null;
    private List<IdentNode> params = null;
    private StmtNode body = null;

    private boolean internal = false;

    public FuncDeclNode(IdentNode name, Collection<IdentNode> params, StmtNode body) {
        this.name = name;
        if (params != null) {
            this.params = new ArrayList<>();
            this.params.addAll(params);
        }
        this.body = body;
    }

    @Override
    public Collection<? extends AstNode> childs() {
        return Arrays.asList(AstNode.group("...", params), body);
    }

    @Override
    public String toString() {
        return "def " + name.toString();
    }

    public ExprNode getName() {
        return name;
    }

    public List<IdentNode> getParams() {
        return params;
    }

    public StmtNode getBody() {
        return body;
    }

    public boolean isInternal() {
        return internal;
    }
    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
