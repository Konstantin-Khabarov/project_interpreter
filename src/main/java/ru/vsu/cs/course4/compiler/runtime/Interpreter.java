package ru.vsu.cs.course4.compiler.runtime;

import ru.vsu.cs.course4.compiler.ast.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Interpreter {

    public static Value execute(AstNode node, Context context) throws InterpreterException {
        if (node instanceof ValueNode) {
            return execute((ValueNode) node, context);
        }
        if (node instanceof IdentNode) {
            return execute((IdentNode) node, context);
        }
        if (node instanceof BinaryOpNode) {
            return execute((BinaryOpNode) node, context);
        }
        if (node instanceof UnaryOpNode) {
            return execute((UnaryOpNode) node, context);
        }
        if (node instanceof AssignNode) {
            return execute((AssignNode) node, context);
        }
        if (node instanceof ForNode) {
            return execute((ForNode) node, context);
        }
        if (node instanceof FuncCallNode) {
            return execute((FuncCallNode) node, context);
        }
        if (node instanceof FuncDeclNode) {
            return execute((FuncDeclNode) node, context);
        }
        if (node instanceof IfNode) {
            return execute((IfNode) node, context);
        }
        if (node instanceof StmtListNode) {
            return execute((StmtListNode) node, context);
        }
        if (node instanceof WhileNode) {
            return execute((WhileNode) node, context);
        }
        if (node instanceof ReturnNode) {
            return execute((ReturnNode) node, context);
        }
        if (node instanceof ArrayNode) {
            return execute((ArrayNode) node, context);
        }
        if (node instanceof ArrayAccessNode) {
            return execute((ArrayAccessNode) node, context);
        }
        return null;
    }

    public static Value execute(ArrayAccessNode node, Context context) throws InterpreterException {
        Value arr = execute(node.getArray(), context);
        Value index = execute(node.getIndex(), context);
        return arr.getItem(index);
    }

    public static Value execute(ArrayNode node, Context context) throws InterpreterException {
        Value arr = new Value(new ArrayList<>());
        for (ExprNode elem: node.getElements()) {
            Value v = execute(elem, context);
            arr.addItem(v);
        }
        return arr;
    }

    public static Value execute(BinaryOpNode node, Context context) throws InterpreterException {
        Value v1 = execute(node.getArg1(), context);
        Value v2 = execute(node.getArg2(), context);
        return Value.binOp(node.getOp(), v1, v2);
    }

    public static Value execute(UnaryOpNode node, Context context) throws InterpreterException {
        Value v = execute(node.getArg(), context);
        return Value.unOp(node.getOp(), v);
    }

    public static Value execute(AssignNode node, Context context) throws InterpreterException {
        Value value = execute(node.getExpr(), context);
        if (node.getIdent() instanceof ArrayAccessNode) {
            ArrayAccessNode aan = (ArrayAccessNode) node.getIdent();
            Value arr = execute(aan.getArray(), context);
            Value index = execute(aan.getIndex(), context);
            arr.setItem(index, value);
        } else {
            if (!(node.getIdent() instanceof IdentNode)) {
                throw new InterpreterException("Left part of assign is not ident or array item");
            }
            context.put(((IdentNode) node.getIdent()).getName(), value);
        }
        return value;
    }

    public static Value execute(ForNode node, Context context) throws InterpreterException {
        execute(node.getInit(), context);
        while (true){
            Value condition = execute(node.getCond(), context);
            if (!condition.boolValue) break;
            execute(node.getBody(), context);
            execute(node.getIterStmt(), context);
        }
        return null;
    }

    public static Value execute(FuncCallNode node, Context context) throws InterpreterException {
        Value func = context.get(node.getFunc().getName());
        if (func == null) {
            throw new InterpreterException(String.format("Function %s not found", node.getFunc().getName()));
        }
        if (func.getFuncValue() == null) {
            throw new InterpreterException(String.format("%s not a function", node.getFunc().getName()));
        }
        if (func.getFunc().getParams().size() != node.getParams().size()) {
            throw new InterpreterException(String.format("Wrong params count for function %s", node.getFunc().getName()));
        }

        List<Value> params = new ArrayList<>();
        for (int i = 0; i < node.getParams().size(); i++) {
            params.add(execute(node.getParams().get(i), context));
        }

        if (func.getFunc().isInternal()) {
            Method m = BuiltInFunctions.FUNCTIONS.get(func.getFunc().getName().toString());
            Object[] objects = params.toArray(new Object[0]);
            try {
                Value r = (Value) m.invoke(null, objects);
                return r;
            } catch (Exception e) {
                if (e instanceof InterpreterException) {
                    throw (InterpreterException) e;
                }
                if (e.getCause() != null && e.getCause() instanceof InterpreterException) {
                    throw (InterpreterException) e.getCause();
                }
                throw new InterpreterException("Runtime exception in function " + func.getFunc().getName());
            }
        } else {
            Context newContext = new Context(context);
            for (int i = 0; i < node.getParams().size(); i++) {
                String paramName = func.getFunc().getParams().get(i).getName();
                newContext.put(paramName, params.get(i));
            }
            try {
                execute(func.getFunc().getBody(), newContext);
            } catch (FuncValueReturn r) {
                return r.getReturnValue();
            }
        }
        return null;
    }

    public static Value execute(FuncDeclNode node, Context context) {
        String functionName = node.getName().toString();
        Value functionValue = new Value(node);
        context.put(functionName, functionValue);
        return null;
    }

    public static Value execute(IdentNode node, Context context) throws InterpreterException {
        Value value = context.get(node.getName());
        if (value == null) {
            throw new InterpreterException(String.format("Not found %s identifier", node.getName()));
        }
        return value;
    }

    public static Value execute(IfNode node, Context context) throws InterpreterException {
        Value value = execute(node.getCond(), context);
        if (value.boolValue) {
            execute(node.getThenStmt(), context);
        } else {
            execute(node.getElseStmt(), context);
        }
        return null;
    }

    public static Value execute(ReturnNode node, Context context) throws InterpreterException {
        Value result = execute(node.getExpr(), context);
        throw new FuncValueReturn(result);
    }

    public static Value execute(StmtListNode node, Context context) throws InterpreterException {
        for (StmtNode stmt : node.getStmts()) {
            execute(stmt, context);
        }
        return null;
    }

    public static Value execute(ValueNode node, Context context) {
        return node.getValue();
    }

    public static Value execute(WhileNode node, Context context) throws InterpreterException {
        Value cond = execute(node.getCond(), context);
        while (cond.boolValue) {
            execute(node.getBodyStmt(), context);
            cond = execute(node.getCond(), context);
        }
        return null;
    }
}
