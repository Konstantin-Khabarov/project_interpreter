package ru.vsu.cs.course4.compiler.ast;

import java.util.Arrays;
import java.util.Collection;

public class ReturnNode implements StmtNode{
    private ExprNode expr;

    public ReturnNode(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public Collection<AstNode> childs() {
        return Arrays.asList(expr);
    }

    @Override
    public String toString() {
        return "return";
    }

    public ExprNode getExpr() {
        return expr;
    }
}
