package ru.vsu.cs.course4.compiler.ast;

import java.util.*;

public class StmtListNode implements StmtNode {
    private List<StmtNode> stmts = null;

    public StmtListNode(Collection<StmtNode> stmts) {
        this.stmts = new ArrayList<>();
        if (stmts != null) {
            this.stmts.addAll(stmts);
        }
    }

    public StmtListNode(StmtNode ...stmts) {
        this(Arrays.asList(stmts));
    }

    @Override
    public Collection<? extends AstNode> childs() {
        return stmts;
    }

    @Override
    public String toString() {
        return "...";
    }

    public List<StmtNode> getStmts() {
        return Collections.unmodifiableList(stmts);
    }
}
