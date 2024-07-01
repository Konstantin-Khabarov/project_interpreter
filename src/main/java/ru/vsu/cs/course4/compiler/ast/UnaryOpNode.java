package ru.vsu.cs.course4.compiler.ast;

import java.util.Arrays;
import java.util.Collection;

public class UnaryOpNode implements ExprNode {
    public enum UnOp {
        NOT("!"),
        OPP("-"),
        INC("++"),
        DEC("--");

        public final String op;

        UnOp(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    private UnOp op;
    private AstNode arg;

    public UnaryOpNode(String op, ExprNode arg) {
        this.op = Arrays.stream(UnOp.values()).filter(x -> x.op.equals(op)).findFirst().get();
        this.arg = arg;
    }

    @Override
    public Collection<AstNode> childs() {
        return Arrays.asList(arg);
    }

    @Override
    public String toString() {
        return op.toString();
    }

    public UnOp getOp() {
        return op;
    }

    public AstNode getArg() {
        return arg;
    }
}
