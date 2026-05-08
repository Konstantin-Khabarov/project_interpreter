package ru.vsu.cs.course4.compiler.codegen;

public class Instruction {
    private final OpCode opCode;
    private final Object operand;

    public Instruction(OpCode opCode, Object operand) {
        this.opCode = opCode;
        this.operand = operand;
    }

    public Instruction(OpCode opCode) {
        this(opCode, null);
    }

    public OpCode getOpCode() { return opCode; }
    public Object getOperand() { return operand; }

    @Override
    public String toString() {
        if (operand == null) return opCode.name();
        return opCode.name() + " " + operand;
    }
}
