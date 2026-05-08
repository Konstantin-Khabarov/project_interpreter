package ru.vsu.cs.course3.compiler.runtime;


public class FuncValueReturn extends InterpreterException {
    private Value returnValue;

    public FuncValueReturn(Value returnValue) {
        super("return");
        this.returnValue = returnValue;
    }

    public Value getReturnValue() {
        return returnValue;
    }
}
