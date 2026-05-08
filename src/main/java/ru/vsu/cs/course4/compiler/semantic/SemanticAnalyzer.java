package ru.vsu.cs.course4.compiler.semantic;

import ru.vsu.cs.course4.compiler.ast.*;
import ru.vsu.cs.course4.compiler.runtime.*;

import java.util.*;

/**
 * Semantic analysis pass over the AST.
 *
 * Checks performed:
 *   - Undefined variables
 *   - Undefined functions / wrong argument count
 *   - Type mismatches (C-like strict typing, no implicit coercions)
 *   - 'return' outside of function body
 *   - Duplicate function declarations
 *
 * AST modification:
 *   - Inserts CastNode for INT→DOUBLE arithmetic promotion (mirrors C behaviour)
 */
public class SemanticAnalyzer {

    private final List<SemanticError> errors = new ArrayList<>();

    // scope stack: each entry maps variable name → inferred type (NULL = unknown)
    private final Deque<Map<String, Type>> scopeStack = new ArrayDeque<>();

    // all declared functions visible so far (built-ins + user-defined)
    private final Map<String, FuncDeclNode> functions = new HashMap<>();

    private boolean inFunction = false;

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    public List<SemanticError> analyze(AstNode root, Context builtinContext) {
        // Register built-in functions
        for (Map.Entry<String, Value> entry : builtinContext.values.entrySet()) {
            if (entry.getValue().getType() == Type.FUNC && entry.getValue().getFunc() != null) {
                functions.put(entry.getKey(), entry.getValue().getFunc());
            }
        }

        // Pre-scan top-level function declarations so forward calls work
        if (root instanceof StmtListNode) {
            for (StmtNode stmt : ((StmtListNode) root).getStmts()) {
                if (stmt instanceof FuncDeclNode) {
                    registerFuncDecl((FuncDeclNode) stmt);
                }
            }
        }

        pushScope();
        analyzeNode(root);
        popScope();
        return errors;
    }

    // -----------------------------------------------------------------------
    // Scope helpers
    // -----------------------------------------------------------------------

    private void pushScope() { scopeStack.push(new LinkedHashMap<>()); }
    private void popScope()  { if (!scopeStack.isEmpty()) scopeStack.pop(); }

    private void setVarType(String name, Type type) {
        // Update in the nearest enclosing scope that already has the variable;
        // otherwise declare in the current scope.
        for (Map<String, Type> scope : scopeStack) {
            if (scope.containsKey(name)) {
                scope.put(name, type);
                return;
            }
        }
        if (!scopeStack.isEmpty()) {
            scopeStack.peek().put(name, type);
        }
    }

    private Type lookupVarType(String name) {
        for (Map<String, Type> scope : scopeStack) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null; // not found
    }

    private void registerFuncDecl(FuncDeclNode node) {
        String name = node.getName().toString();
        if (functions.containsKey(name) && !functions.get(name).isInternal()) {
            error("Function '" + name + "' is already declared");
        } else {
            functions.put(name, node);
        }
    }

    private void error(String msg) { errors.add(new SemanticError(msg)); }

    // -----------------------------------------------------------------------
    // Node dispatch — returns inferred expression type or null for statements
    // -----------------------------------------------------------------------

    private Type analyzeNode(AstNode node) {
        if (node == null) return null;

        if (node instanceof StmtListNode)    return analyzeStmtList((StmtListNode) node);
        if (node instanceof AssignNode)      return analyzeAssign((AssignNode) node);
        if (node instanceof FuncDeclNode)    return analyzeFuncDecl((FuncDeclNode) node);
        if (node instanceof IfNode)          return analyzeIf((IfNode) node);
        if (node instanceof WhileNode)       return analyzeWhile((WhileNode) node);
        if (node instanceof ForNode)         return analyzeFor((ForNode) node);
        if (node instanceof ReturnNode)      return analyzeReturn((ReturnNode) node);
        if (node instanceof FuncCallNode)    return analyzeFuncCall((FuncCallNode) node);
        if (node instanceof BinaryOpNode)    return analyzeBinaryOp((BinaryOpNode) node);
        if (node instanceof UnaryOpNode)     return analyzeUnaryOp((UnaryOpNode) node);
        if (node instanceof ValueNode)       return ((ValueNode) node).getValue().getType();
        if (node instanceof IdentNode)       return analyzeIdent((IdentNode) node);
        if (node instanceof ArrayNode)       return analyzeArray((ArrayNode) node);
        if (node instanceof ArrayAccessNode) return analyzeArrayAccess((ArrayAccessNode) node);
        if (node instanceof CastNode)        return ((CastNode) node).getTargetType();
        return null;
    }

    // -----------------------------------------------------------------------
    // Statement nodes
    // -----------------------------------------------------------------------

    private Type analyzeStmtList(StmtListNode node) {
        for (StmtNode stmt : node.getStmts()) analyzeNode(stmt);
        return null;
    }

    private Type analyzeAssign(AssignNode node) {
        Type exprType = analyzeNode(node.getExpr());

        if (node.getIdent() instanceof IdentNode) {
            String name = ((IdentNode) node.getIdent()).getName();
            setVarType(name, exprType != null ? exprType : Type.NULL);
        } else if (node.getIdent() instanceof ArrayAccessNode) {
            analyzeNode(node.getIdent());
        } else {
            error("Invalid left-hand side of assignment");
        }
        return null;
    }

    private Type analyzeFuncDecl(FuncDeclNode node) {
        // Already registered in pre-scan; just analyse the body.
        boolean savedInFunc = inFunction;
        inFunction = true;

        pushScope();
        if (node.getParams() != null) {
            for (IdentNode param : node.getParams()) {
                scopeStack.peek().put(param.getName(), Type.NULL);
            }
        }
        analyzeNode(node.getBody());
        popScope();

        inFunction = savedInFunc;
        return null;
    }

    private Type analyzeIf(IfNode node) {
        Type condType = analyzeNode(node.getCond());
        checkBoolCondition(condType, "if");

        pushScope(); analyzeNode(node.getThenStmt()); popScope();
        if (node.getElseStmt() != null) {
            pushScope(); analyzeNode(node.getElseStmt()); popScope();
        }
        return null;
    }

    private Type analyzeWhile(WhileNode node) {
        Type condType = analyzeNode(node.getCond());
        checkBoolCondition(condType, "while");
        pushScope(); analyzeNode(node.getBodyStmt()); popScope();
        return null;
    }

    private Type analyzeFor(ForNode node) {
        pushScope();
        analyzeNode(node.getInit());
        Type condType = analyzeNode(node.getCond());
        checkBoolCondition(condType, "for");
        analyzeNode(node.getIterStmt());
        pushScope(); analyzeNode(node.getBody()); popScope();
        popScope();
        return null;
    }

    private void checkBoolCondition(Type t, String stmt) {
        if (t != null && t != Type.NULL && t != Type.BOOLEAN) {
            error("Condition in '" + stmt + "' must be boolean, got '" + t + "'");
        }
    }

    private Type analyzeReturn(ReturnNode node) {
        if (!inFunction) {
            error("'return' statement outside of function body");
        }
        analyzeNode(node.getExpr());
        return null;
    }

    // -----------------------------------------------------------------------
    // Expression nodes
    // -----------------------------------------------------------------------

    private Type analyzeFuncCall(FuncCallNode node) {
        String name = node.getFunc().getName();
        FuncDeclNode decl = functions.get(name);

        if (decl == null) {
            Type varType = lookupVarType(name);
            if (varType == null) {
                error("Undefined function '" + name + "'");
            } else if (varType != Type.FUNC && varType != Type.NULL) {
                error("'" + name + "' is not callable");
            }
        } else if (!decl.isInternal()) {
            int expected = decl.getParams() != null ? decl.getParams().size() : 0;
            if (expected != node.getParams().size()) {
                error("Function '" + name + "' expects " + expected +
                      " argument(s), but " + node.getParams().size() + " provided");
            }
        }

        for (ExprNode param : node.getParams()) analyzeNode(param);
        return null; // return type is unknown in a dynamically typed language
    }

    private Type analyzeBinaryOp(BinaryOpNode node) {
        Type lType = analyzeNode(node.getArg1());
        Type rType = analyzeNode(node.getArg2());
        BinaryOpNode.BinOp op = node.getOp();

        // When either type is unknown we can only infer, not validate
        if (isUnknown(lType) || isUnknown(rType)) {
            return inferBinOpResult(op, lType, rType);
        }

        return checkTypesAndPromote(node, op, lType, rType);
    }

    private boolean isUnknown(Type t) { return t == null || t == Type.NULL; }
    private boolean isNumeric(Type t)  { return t == Type.INT || t == Type.DOUBLE; }

    private Type checkTypesAndPromote(BinaryOpNode node,
                                      BinaryOpNode.BinOp op,
                                      Type lType, Type rType) {
        switch (op) {
            case ADD:
                if (lType == Type.STRING && rType == Type.STRING) return Type.STRING;
                if (lType == Type.ARR    && rType == Type.ARR)    return Type.ARR;
                if (isNumeric(lType) && isNumeric(rType)) return numericPromotion(node, lType, rType);
                error("Operator '+' cannot be applied to '" + lType + "' and '" + rType + "'");
                return null;

            case SUB: case MUL: case DIV: case DIVREM:
                if (isNumeric(lType) && isNumeric(rType)) return numericPromotion(node, lType, rType);
                error("Operator '" + op + "' requires numeric operands, got '" + lType + "' and '" + rType + "'");
                return null;

            case AND: case OR:
                if (lType == Type.BOOLEAN && rType == Type.BOOLEAN) return Type.BOOLEAN;
                error("Operator '" + op + "' requires boolean operands, got '" + lType + "' and '" + rType + "'");
                return null;

            case EQ: case NEQ:
                if (lType == rType) return Type.BOOLEAN;
                if (isNumeric(lType) && isNumeric(rType)) {
                    numericPromotion(node, lType, rType);
                    return Type.BOOLEAN;
                }
                error("Operator '" + op + "' cannot compare '" + lType + "' and '" + rType + "'");
                return Type.BOOLEAN;

            case LESS: case MORE: case LESSEQ: case MOREEQ:
                if (lType == rType && (isNumeric(lType) || lType == Type.STRING)) return Type.BOOLEAN;
                if (isNumeric(lType) && isNumeric(rType)) {
                    numericPromotion(node, lType, rType);
                    return Type.BOOLEAN;
                }
                error("Operator '" + op + "' cannot be applied to '" + lType + "' and '" + rType + "'");
                return Type.BOOLEAN;

            default:
                return null;
        }
    }

    // Inserts CastNode for INT→DOUBLE promotion and returns the result type.
    private Type numericPromotion(BinaryOpNode node, Type lType, Type rType) {
        if (lType == rType) return lType;
        if (lType == Type.INT && rType == Type.DOUBLE) {
            node.setArg1(new CastNode((ExprNode) node.getArg1(), Type.DOUBLE));
            return Type.DOUBLE;
        }
        if (lType == Type.DOUBLE && rType == Type.INT) {
            node.setArg2(new CastNode((ExprNode) node.getArg2(), Type.DOUBLE));
            return Type.DOUBLE;
        }
        return Type.DOUBLE; // both numeric, default to double
    }

    private Type inferBinOpResult(BinaryOpNode.BinOp op, Type lType, Type rType) {
        switch (op) {
            case AND: case OR:
            case EQ:  case NEQ:
            case LESS: case MORE: case LESSEQ: case MOREEQ:
                return Type.BOOLEAN;
            case ADD:
                if (lType == Type.STRING || rType == Type.STRING) return Type.STRING;
                if (lType == Type.DOUBLE || rType == Type.DOUBLE) return Type.DOUBLE;
                if (lType == Type.INT    || rType == Type.INT)    return Type.INT;
                return null;
            case SUB: case MUL: case DIV: case DIVREM:
                if (lType == Type.DOUBLE || rType == Type.DOUBLE) return Type.DOUBLE;
                if (lType == Type.INT    || rType == Type.INT)    return Type.INT;
                return null;
            default: return null;
        }
    }

    private Type analyzeUnaryOp(UnaryOpNode node) {
        Type t = analyzeNode(node.getArg());
        if (isUnknown(t)) return null;

        switch (node.getOp()) {
            case OPP: case INC: case DEC:
                if (!isNumeric(t)) {
                    error("Operator '" + node.getOp() + "' requires numeric operand, got '" + t + "'");
                }
                return t;
            case NOT:
                if (t != Type.BOOLEAN) {
                    error("Operator '!' requires boolean operand, got '" + t + "'");
                }
                return Type.BOOLEAN;
            default: return null;
        }
    }

    private Type analyzeIdent(IdentNode node) {
        String name = node.getName();
        Type t = lookupVarType(name);
        if (t != null) return t == Type.NULL ? null : t;
        if (functions.containsKey(name)) return Type.FUNC;
        error("Undefined variable '" + name + "'");
        return null;
    }

    private Type analyzeArray(ArrayNode node) {
        for (ExprNode elem : node.getElements()) analyzeNode(elem);
        return Type.ARR;
    }

    private Type analyzeArrayAccess(ArrayAccessNode node) {
        Type arrType   = analyzeNode(node.getArray());
        Type indexType = analyzeNode(node.getIndex());

        if (!isUnknown(arrType) && arrType != Type.ARR) {
            error("Array access on non-array type '" + arrType + "'");
        }
        if (!isUnknown(indexType) && indexType != Type.INT) {
            error("Array index must be int, got '" + indexType + "'");
        }
        return null;
    }
}
