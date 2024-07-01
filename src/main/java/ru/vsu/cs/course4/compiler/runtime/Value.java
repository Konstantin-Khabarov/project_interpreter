package ru.vsu.cs.course4.compiler.runtime;

import ru.vsu.cs.course4.compiler.ast.BinaryOpNode;
import ru.vsu.cs.course4.compiler.ast.FuncDeclNode;
import ru.vsu.cs.course4.compiler.ast.UnaryOpNode;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Value {

    private interface BinOperation {
        Value operation(Value v1, Value v2);
    }
    private static final Map<BinaryOpNode.BinOp, Map<Type, BinOperation>> BIN_OP_TYPES = new HashMap<>();

    static {
        Map<Type, BinOperation> operations = new HashMap<>();

        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() + v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() + v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr() + v2.getStr()));
        operations.put(Type.ARR, (v1, v2) -> new Value(Stream.concat(v1.getArr().stream(), v2.getArr().stream()).collect(Collectors.toList())));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.ADD, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() * v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() * v2.getDouble()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.MUL, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() - v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() - v2.getDouble()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.SUB, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() / v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() / v2.getDouble()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.DIV, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() % v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() % v2.getDouble()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.DIVREM, operations);

        operations = new HashMap<>();
        operations.put(Type.BOOLEAN, (v1, v2) -> new Value(v1.getBool() && v2.getBool()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.AND, operations);

        operations = new HashMap<>();
        operations.put(Type.BOOLEAN, (v1, v2) -> new Value(v1.getBool() || v2.getBool()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.OR, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() == v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() == v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr().equals(v2.getStr())));
        operations.put(Type.BOOLEAN, (v1, v2) -> new Value(v1.getBool() == v2.getBool()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.EQ, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() != v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() != v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(!(v1.getStr().equals(v2.getStr()))));
        operations.put(Type.BOOLEAN, (v1, v2) -> new Value(v1.getBool() != v2.getBool()));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.NEQ, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() < v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() < v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr().compareTo(v2.getStr()) < 0));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.LESS, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() > v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() > v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr().compareTo(v2.getStr()) > 0));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.MORE, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() <= v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() <= v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr().compareTo(v2.getStr()) <= 0));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.LESSEQ, operations);

        operations = new HashMap<>();
        operations.put(Type.INT, (v1, v2) -> new Value(v1.getInt() >= v2.getInt()));
        operations.put(Type.DOUBLE, (v1, v2) -> new Value(v1.getDouble() >= v2.getDouble()));
        operations.put(Type.STRING, (v1, v2) -> new Value(v1.getStr().compareTo(v2.getStr()) >= 0));
        BIN_OP_TYPES.put(BinaryOpNode.BinOp.MOREEQ, operations);
    }

    private interface UnOperation {
        Value unOperation(Value v);
    }
    private static final Map<UnaryOpNode.UnOp, Map<Type, UnOperation>> UN_OP_TYPES = new HashMap<>();

    static {
        Map<Type, UnOperation> unOperations = new HashMap<>();

        unOperations.put(Type.INT, (v) -> new Value(-v.getInt()));
        unOperations.put(Type.DOUBLE, (v) -> new Value(-v.getDouble()));
        UN_OP_TYPES.put(UnaryOpNode.UnOp.OPP, unOperations);

        unOperations = new HashMap<>();
        unOperations.put(Type.BOOLEAN, (v) -> new Value(!(v.getBool())));
        UN_OP_TYPES.put(UnaryOpNode.UnOp.NOT, unOperations);

        unOperations = new HashMap<>();
        unOperations.put(Type.INT, (v) -> new Value(v.getInt() + 1));
        unOperations.put(Type.DOUBLE, (v) -> new Value(v.getDouble() + 1));
        UN_OP_TYPES.put(UnaryOpNode.UnOp.INC, unOperations);

        unOperations = new HashMap<>();
        unOperations.put(Type.INT, (v) -> new Value(v.getInt() - 1));
        unOperations.put(Type.DOUBLE, (v) -> new Value(v.getDouble() - 1));
        UN_OP_TYPES.put(UnaryOpNode.UnOp.DEC, unOperations);
    }

    Type type = Type.NULL;
    int intValue;
    double doubleValue;
    String strValue;
    boolean boolValue;
    FuncDeclNode funcValue;
    List<Value> array;

    public Value(int intValue) {
        this.type = Type.INT;
        this.intValue = intValue;
    }

    public Value(double doubleValue) {
        this.type = Type.DOUBLE;
        this.doubleValue = doubleValue;
    }

    public Value(String strValue) {
        this.type = Type.STRING;
        this.strValue = strValue;
    }

    public Value(boolean boolValue) {
        this.type = Type.BOOLEAN;
        this.boolValue = boolValue;
    }

    public Value(FuncDeclNode funcValue) {
        this.type = Type.FUNC;
        this.funcValue = funcValue;
    }

    public Value(List<Value> array) {
        this.type = Type.ARR;
        this.array = array;
    }

    public Value convert(Type type) {
        if (type == Type.INT) {
            return getIntValue();
        }
        if (type == Type.DOUBLE){
            return getDoubleValue();
        }
        if (type == Type.STRING){
            return getStrValue();
        }
        if (type == Type.BOOLEAN){
            return getBoolValue();
        }
        if (type == Type.FUNC){
            return getFuncValue();
        }
        if (type == Type.ARR){
            return getArrValue();
        }
        return null;
    }
    public Value getIntValue() {
        if (type == Type.INT) {
            return this;
        }
        return null;
    }
    public int getInt(){
        return intValue;
    }

    public Value getDoubleValue() {
        if (type == Type.DOUBLE) {
            return this;
        }
        if (type == Type.INT) {
            return new Value((double) intValue);
        }
        return null;
    }

    public double getDouble(){
        return doubleValue;
    }

    public Value getStrValue() {
        if (type == Type.STRING) {
            return this;
        }
        if (type == Type.INT) {
            return new Value(String.valueOf(intValue));
        }
        if (type == Type.DOUBLE){
            return new Value(String.valueOf(doubleValue));
        }
        if (type == Type.BOOLEAN){
            return new Value(String.valueOf(boolValue));
        }
        return null;
    }

    public String getStr(){
        return strValue;
    }

    public Value getBoolValue() {
        if (type == Type.BOOLEAN) {
            return this;
        }
        return null;
    }

    public boolean getBool(){
        return boolValue;
    }

    public Value getFuncValue() {
        if (type == Type.FUNC) {
            return this;
        }
        return null;
    }

    public FuncDeclNode getFunc(){
        return funcValue;
    }

    public Value getArrValue() {
        if (type == Type.ARR) {
            return this;
        }
        return null;
    }

    public List<Value> getArr(){
        return array;
    }

    private void checkArrayAccess(Value index, boolean checkIndex) throws InterpreterException {
        if (type != Type.ARR) {
            throw new InterpreterException("Not array");
        }
        if (checkIndex) {
            if (index == null) {
                throw new InterpreterException("Null array index");
            }
            index = index.getIntValue();
            if (index == null) {
                throw new InterpreterException("Not int array index");
            }
            int i = index.getInt();
            if (i < 0 || i >= array.size()) {
                throw new InterpreterException("Index not in array");
            }
        }
    }

    public Value getItem(Value index) throws InterpreterException {
        checkArrayAccess(index, true);
        return array.get(index.getInt());
    }

    public void setItem(Value index, Value value) throws InterpreterException {
        checkArrayAccess(index, true);
        array.set(index.getInt(), value);
    }

    public void addItem(Value value) throws InterpreterException {
        checkArrayAccess(null, false);
        array.add(value);
    }

    public static Value binOp(BinaryOpNode.BinOp op, Value v1, Value v2) throws InterpreterException {
        Map<Type, BinOperation> operations = BIN_OP_TYPES.getOrDefault(op, null);
        if (operations == null) {
            throw new InterpreterException(String.format("Unsupported operation %s", op));
        }
        boolean found = false;
        if (v1.type == v2.type) {
            found = operations.containsKey(v1.type);
        } else {
            boolean tmpBool = operations.containsKey(v1.type);
            if (tmpBool) {
                Value tmp = v2.convert(v1.type);
                if (tmp != null) {
                    v2 = tmp;
                    found = true;
                }
            }
            if (!found) {
                tmpBool = operations.containsKey(v2.type);
                if (tmpBool) {
                    Value tmp = v1.convert(v2.type);
                    if (tmp != null) {
                        v1 = tmp;
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            throw new InterpreterException(String.format("Unsupported operation %s for types (%s, %s)", op, v1.type, v2.type));
        }
        return operations.get(v1.type).operation(v1, v2);
    }

    public static Value unOp(UnaryOpNode.UnOp op, Value v) throws InterpreterException {
        Map<Type, UnOperation> operations = UN_OP_TYPES.getOrDefault(op, null);
        if (operations == null) {
            throw new InterpreterException(String.format("Unsupported operation %s", op));
        }
        boolean found = operations.containsKey(v.type);
        if (!found) {
            throw new InterpreterException(String.format("Unsupported operation %s for type %s", op, v.type));
        }
        return operations.get(v.type).unOperation(v);
    }

    @Override
    public String toString() {
        return getStrValue().strValue;
    }
}
