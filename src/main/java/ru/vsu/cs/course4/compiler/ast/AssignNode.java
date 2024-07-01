package ru.vsu.cs.course4.compiler.ast;

import java.util.Arrays;
import java.util.Collection;

public class AssignNode implements StmtNode {
    private ExprNode ident;
    private ExprNode expr;

    public AssignNode(ExprNode ident, ExprNode expr) {
        this.ident = ident;
        this.expr = expr;
    }

    @Override
    public Collection<AstNode> childs() {
        return Arrays.asList(ident, expr);
    }

    @Override
    public String toString() {
        return "=";
    }

    public ExprNode getIdent() {
        return ident;
    }

    public ExprNode getExpr() {
        return expr;
    }
}
