package ru.vsu.cs.course4.compiler.runtime;

/**
 * Created by khabarov_k_a on 02.05.2024.
 */
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
