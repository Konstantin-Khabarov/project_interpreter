package ru.vsu.cs.course4.compiler.codegen;

public enum OpCode {
    // Push literals
    PUSH_INT,
    PUSH_DOUBLE,
    PUSH_STRING,
    PUSH_BOOL,
    PUSH_NULL,

    // Variable access
    LOAD,
    STORE,

    // Operations
    BINOP,
    UNOP,
    CAST,

    // Array operations
    ARRAY_NEW,   // push new empty array
    ARRAY_PUSH,  // pop elem, peek array, add elem to array
    ARRAY_GET,   // pop index + array, push element
    ARRAY_SET,   // pop value + index + array, set array[index]=value

    // Stack
    POP,

    // Control flow
    JMP,
    JMP_FALSE,

    // Function call / return
    CALL,
    RETURN,

    // Program end
    HALT,

    // Pseudo-instructions (metadata only, never executed)
    LABEL,
    DEF_FUNC,
    END_FUNC
}
