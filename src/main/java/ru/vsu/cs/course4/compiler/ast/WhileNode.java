package ru.vsu.cs.course4.compiler.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WhileNode implements ExprNode, StmtNode{
    private ExprNode cond = null;
    private StmtNode bodyStmt = null;

    public WhileNode(ExprNode cond, StmtNode bodyStmt) {
        this.cond = cond;
        this.bodyStmt = bodyStmt;
    }

    @Override
    public Collection<? extends AstNode> childs() {
        List<AstNode> childs = Arrays.asList(cond,bodyStmt);
        return childs;
    }

    @Override
    public String toString() {
        return "while";
    }

    public ExprNode getCond() {
        return cond;
    }

    public StmtNode getBodyStmt() {
        return bodyStmt;
    }
}
