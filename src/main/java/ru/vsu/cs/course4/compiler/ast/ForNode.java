package ru.vsu.cs.course4.compiler.ast;

import java.util.*;

public class ForNode implements ExprNode, StmtNode {
    private StmtNode init = null;
    private ExprNode cond = null;
    private StmtNode iterStmt = null;
    private StmtNode body = null;

    public ForNode(StmtNode init, ExprNode cond, StmtNode iterStmt, StmtNode body) {
        this.init = init;
        this.cond = cond;
        this.iterStmt = iterStmt;
        this.body = body;
    }

    @Override
    public Collection<? extends AstNode> childs() {
        List<AstNode> childs = Arrays.asList(init, cond, iterStmt, body);
        return childs;
    }

    @Override
    public String toString() {
        return "for";
    }

    public StmtNode getInit() {
        return init;
    }

    public ExprNode getCond() {
        return cond;
    }

    public StmtNode getIterStmt() {
        return iterStmt;
    }

    public StmtNode getBody() {
        return body;
    }
}
