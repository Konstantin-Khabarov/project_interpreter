package ru.vsu.cs.course4.compiler.ast;

import ru.vsu.cs.course4.compiler.runtime.Value;

public class ValueNode implements ExprNode {
    private String str;
    private Value value;

    public ValueNode(String str) {
        this.str = str;
        if (str.equals("true") || str.equals("false")) {
            this.value = new Value(Boolean.valueOf(str));
        } else if (str.startsWith("\"") || str.startsWith("'")) {
            this.value = new Value(str.substring(1, str.length() - 1));
        } else if (str.contains(".")) {
            this.value = new Value(Double.valueOf(str));
        } else {
            this.value = new Value(Integer.valueOf(str));
        }
    }

    @Override
    public String toString() {
        return str;
    }

    public Value getValue() {
        return value;
    }
}