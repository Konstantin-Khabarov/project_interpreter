package ru.vsu.cs.course4.compiler.ast;

import ru.vsu.cs.course4.compiler.runtime.Type;

import java.util.Arrays;
import java.util.Collection;

public class CastNode implements ExprNode {
    private ExprNode expr;
    private final Type targetType;

    public CastNode(ExprNode expr, Type targetType) {
        this.expr = expr;
        this.targetType = targetType;
    }

    public ExprNode getExpr() { return expr; }
    public Type getTargetType() { return targetType; }

    @Override
    public Collection<? extends AstNode> childs() {
        return Arrays.asList(expr);
    }

    @Override
    public String toString() {
        return "(" + targetType + ")cast";
    }
}
