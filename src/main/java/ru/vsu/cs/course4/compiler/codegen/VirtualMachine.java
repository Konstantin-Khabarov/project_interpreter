package ru.vsu.cs.course4.compiler.codegen;

import ru.vsu.cs.course4.compiler.ast.BinaryOpNode;
import ru.vsu.cs.course4.compiler.ast.UnaryOpNode;
import ru.vsu.cs.course4.compiler.runtime.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Stack-based virtual machine that executes bytecode produced by BytecodeGenerator.
 *
 * Memory model:
 *   valueStack  – operand/evaluation stack
 *   callStack   – return addresses and saved scope depths
 *   scopes      – list of variable scopes (global = index 0, function-local = top)
 *
 * Calling convention:
 *   CALL funcname:N – pops N arguments (arg_N on top), saves frame, pushes new
 *                     scope, jumps to function start. The function prologue then
 *                     STOREs the arguments into its local scope.
 *   RETURN          – pops return value, restores scope chain and IP, pushes
 *                     return value for the caller.
 */
public class VirtualMachine {

    private List<Instruction> code;
    private final Map<String, Integer> funcTable  = new HashMap<>();
    private final Map<String, Integer> labelTable = new HashMap<>();

    private final Deque<Value>    valueStack = new ArrayDeque<>();
    private final Deque<int[]>    callStack  = new ArrayDeque<>(); // [returnAddr, scopeDepth]
    private final List<Map<String, Value>> scopes = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Load & preprocess
    // -----------------------------------------------------------------------

    public void load(List<Instruction> instructions) {
        this.code = instructions;
        funcTable.clear();
        labelTable.clear();
        preprocess();
    }

    private void preprocess() {
        for (int i = 0; i < code.size(); i++) {
            Instruction instr = code.get(i);
            switch (instr.getOpCode()) {
                case LABEL:
                    // Map label → index of the instruction *after* the LABEL pseudo-op
                    labelTable.put((String) instr.getOperand(), i + 1);
                    break;
                case DEF_FUNC:
                    // Function body starts at the instruction *after* DEF_FUNC
                    funcTable.put((String) instr.getOperand(), i + 1);
                    break;
                default:
                    break;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Execute
    // -----------------------------------------------------------------------

    public void execute() throws InterpreterException {
        scopes.clear();
        scopes.add(new HashMap<>()); // global scope

        int ip = 0;
        while (ip < code.size()) {
            Instruction instr = code.get(ip);

            switch (instr.getOpCode()) {

                // ── Push literals ──────────────────────────────────────────
                case PUSH_INT:    push(new Value((int)    instr.getOperand())); break;
                case PUSH_DOUBLE: push(new Value((double) instr.getOperand())); break;
                case PUSH_STRING: push(new Value((String) instr.getOperand())); break;
                case PUSH_BOOL:   push(new Value((boolean)instr.getOperand())); break;
                case PUSH_NULL:   push(new Value());                            break;

                // ── Variable access ────────────────────────────────────────
                case LOAD: {
                    String name = (String) instr.getOperand();
                    Value v = loadVar(name);
                    if (v == null) {
                        throw new InterpreterException("Undefined variable: '" + name + "'");
                    }
                    push(v);
                    break;
                }
                case STORE: {
                    String name = (String) instr.getOperand();
                    Value v = pop();
                    storeVar(name, v);
                    break;
                }

                // ── Operations ─────────────────────────────────────────────
                case BINOP: {
                    Value v2 = pop();
                    Value v1 = pop();
                    BinaryOpNode.BinOp op = BinaryOpNode.BinOp.valueOf((String) instr.getOperand());
                    push(Value.binOp(op, v1, v2));
                    break;
                }
                case UNOP: {
                    Value v = pop();
                    UnaryOpNode.UnOp op = UnaryOpNode.UnOp.valueOf((String) instr.getOperand());
                    push(Value.unOp(op, v));
                    break;
                }
                case CAST: {
                    Type target = Type.valueOf((String) instr.getOperand());
                    Value v = pop();
                    Value converted = v.convert(target);
                    if (converted == null) {
                        throw new InterpreterException(
                            "Cannot cast '" + v.getType() + "' to '" + target + "'");
                    }
                    push(converted);
                    break;
                }

                // ── Array operations ───────────────────────────────────────
                case ARRAY_NEW:
                    push(new Value(new ArrayList<>()));
                    break;
                case ARRAY_PUSH: {
                    Value elem = pop();
                    peek().addItem(elem);   // array stays on stack
                    break;
                }
                case ARRAY_GET: {
                    Value index = pop();
                    Value arr   = pop();
                    push(arr.getItem(index));
                    break;
                }
                case ARRAY_SET: {
                    Value value = pop();
                    Value index = pop();
                    Value arr   = pop();
                    arr.setItem(index, value);
                    break;
                }

                // ── Stack ──────────────────────────────────────────────────
                case POP: pop(); break;

                // ── Control flow ───────────────────────────────────────────
                case JMP: {
                    int target = resolveLabel((String) instr.getOperand());
                    ip = target;
                    continue;
                }
                case JMP_FALSE: {
                    Value cond = pop();
                    if (!cond.getBool()) {
                        int target = resolveLabel((String) instr.getOperand());
                        ip = target;
                        continue;
                    }
                    break;
                }

                // ── Function call ──────────────────────────────────────────
                case CALL: {
                    ip = executeCall((String) instr.getOperand(), ip);
                    continue; // ip already updated
                }

                // ── Return ─────────────────────────────────────────────────
                case RETURN: {
                    Value returnVal = pop();

                    if (callStack.isEmpty()) {
                        throw new InterpreterException("RETURN outside of function");
                    }
                    int[] frame = callStack.pop();
                    int returnAddr  = frame[0];
                    int scopeTarget = frame[1];

                    while (scopes.size() > scopeTarget) {
                        scopes.remove(scopes.size() - 1);
                    }

                    push(returnVal);
                    ip = returnAddr;
                    continue;
                }

                // ── Pseudo-instructions (ignored at execution time) ────────
                case LABEL:
                case DEF_FUNC:
                case END_FUNC:
                    break;

                case HALT:
                    return;

                default:
                    throw new InterpreterException("Unknown opcode: " + instr.getOpCode());
            }

            ip++;
        }
    }

    // -----------------------------------------------------------------------
    // CALL dispatch
    // Returns the new ip to continue from (caller must `continue` the loop).
    // -----------------------------------------------------------------------

    private int executeCall(String callOperand, int currentIp) throws InterpreterException {
        int colon = callOperand.lastIndexOf(':');
        String funcName = callOperand.substring(0, colon);
        int nargs = Integer.parseInt(callOperand.substring(colon + 1));

        // Pop arguments in order (left-to-right after reversing)
        Value[] args = new Value[nargs];
        for (int i = nargs - 1; i >= 0; i--) {
            args[i] = pop();
        }

        // Try built-in first
        Method m = BuiltInFunctions.FUNCTIONS.get(funcName);
        if (m != null) {
            try {
                Object result = m.invoke(null, (Object[]) args);
                push(result instanceof Value ? (Value) result : new Value());
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterpreterException) throw (InterpreterException) cause;
                throw new InterpreterException(
                    "Error in built-in '" + funcName + "': " + e.getMessage());
            }
            return currentIp + 1; // normal advance
        }

        // User-defined function
        Integer funcAddr = funcTable.get(funcName);
        if (funcAddr == null) {
            throw new InterpreterException("Undefined function: '" + funcName + "'");
        }

        // Save frame (return address, current scope depth)
        callStack.push(new int[]{currentIp + 1, scopes.size()});

        // Push args back so the function prologue can STORE them
        for (int i = 0; i < nargs; i++) push(args[i]);

        // New scope for this function invocation
        scopes.add(new HashMap<>());

        return funcAddr; // jump to function
    }

    // -----------------------------------------------------------------------
    // Scope helpers
    // -----------------------------------------------------------------------

    private void storeVar(String name, Value value) {
        // Update existing binding (search outward); otherwise declare in current scope.
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                scopes.get(i).put(name, value);
                return;
            }
        }
        scopes.get(scopes.size() - 1).put(name, value);
    }

    private Value loadVar(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Value v = scopes.get(i).get(name);
            if (v != null) return v;
        }
        return null;
    }

    private int resolveLabel(String label) throws InterpreterException {
        Integer target = labelTable.get(label);
        if (target == null) throw new InterpreterException("Unknown label: '" + label + "'");
        return target;
    }

    // -----------------------------------------------------------------------
    // Stack helpers
    // -----------------------------------------------------------------------

    private void  push(Value v) { valueStack.push(v); }
    private Value pop()         { return valueStack.pop(); }
    private Value peek()        { return valueStack.peek(); }

    // -----------------------------------------------------------------------
    // Bytecode serialisation (human-readable text format)
    // -----------------------------------------------------------------------

    public static void writeBytecode(List<Instruction> instructions, PrintWriter writer) {
        for (Instruction instr : instructions) {
            writer.println(instr.toString());
        }
        writer.flush();
    }

    public static List<Instruction> readBytecode(BufferedReader reader) throws Exception {
        List<Instruction> list = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";")) continue;
            list.add(parseInstruction(line));
        }
        return list;
    }

    private static Instruction parseInstruction(String line) {
        int sp = line.indexOf(' ');
        if (sp == -1) {
            return new Instruction(OpCode.valueOf(line));
        }
        String opStr  = line.substring(0, sp);
        String oprand = line.substring(sp + 1);
        OpCode op = OpCode.valueOf(opStr);
        Object operand;
        switch (op) {
            case PUSH_INT:  operand = Integer.parseInt(oprand);   break;
            case PUSH_DOUBLE: operand = Double.parseDouble(oprand); break;
            case PUSH_BOOL: operand = Boolean.parseBoolean(oprand); break;
            default:        operand = oprand;                     break;
        }
        return new Instruction(op, operand);
    }
}
