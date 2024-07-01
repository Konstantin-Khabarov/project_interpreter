package ru.vsu.cs.course4.compiler.runtime;

public enum Type {
    NULL("null"),
    INT("int"),
    DOUBLE("double"),
    BOOLEAN("bool"),
    STRING("str"),
    FUNC("def"),
    ARR("array");

    public final String type;

    Type(String type){
        this.type=type;
    }

    @Override
    public String toString() {
        return type;
    }
}
