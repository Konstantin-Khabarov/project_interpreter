package ru.vsu.cs.course4.compiler.codegen;

public enum OpCode {
    PUSH_INT,
    PUSH_DOUBLE,
    PUSH_STRING,
    PUSH_BOOL,
    PUSH_NULL,

    LOAD,
    STORE,

    BINOP,
    UNOP,
    CAST,

    ARRAY_NEW,
    ARRAY_PUSH,
    ARRAY_GET,
    ARRAY_SET,

    POP,

    JMP,
    JMP_FALSE,

    CALL,
    RETURN,

    HALT,

    LABEL,
    DEF_FUNC,
    END_FUNC
}
