package ru.vsu.cs.course4.compiler.codegen;

import ru.vsu.cs.course4.compiler.ast.*;

import java.util.ArrayList;
import java.util.List;

public class BytecodeGenerator {

    private final List<Instruction> code = new ArrayList<>();
    private int labelCounter = 0;



    public List<Instruction> generate(AstNode root) {
        emit(OpCode.JMP, "__main__");

        collectFunctions(root);

        emit(OpCode.LABEL, "__main__");
        genStmt(root);
        emit(OpCode.HALT);

        return code;
    }


    private void emit(OpCode op)                { code.add(new Instruction(op)); }
    private void emit(OpCode op, Object operand){ code.add(new Instruction(op, operand)); }
    private String newLabel()                   { return "L" + (labelCounter++); }


    private void collectFunctions(AstNode node) {
        if (node instanceof StmtListNode) {
            for (StmtNode s : ((StmtListNode) node).getStmts()) collectFunctions(s);
        } else if (node instanceof FuncDeclNode) {
            genFuncDecl((FuncDeclNode) node);
        } else if (node instanceof IfNode) {
            IfNode n = (IfNode) node;
            collectFunctions(n.getThenStmt());
            if (n.getElseStmt() != null) collectFunctions(n.getElseStmt());
        } else if (node instanceof WhileNode) {
            collectFunctions(((WhileNode) node).getBodyStmt());
        } else if (node instanceof ForNode) {
            collectFunctions(((ForNode) node).getBody());
        }
    }

    private void genFuncDecl(FuncDeclNode node) {
        String name = node.getName().toString();
        emit(OpCode.DEF_FUNC, name);

        List<IdentNode> params = node.getParams();
        if (params != null) {
            for (int i = params.size() - 1; i >= 0; i--) {
                emit(OpCode.STORE, params.get(i).getName());
            }
        }

        genStmt(node.getBody());

        emit(OpCode.PUSH_NULL);
        emit(OpCode.RETURN);
        emit(OpCode.END_FUNC);
    }



    private void genStmt(AstNode node) {
        if (node == null) return;

        if (node instanceof StmtListNode) {
            for (StmtNode s : ((StmtListNode) node).getStmts()) genStmt(s);

        } else if (node instanceof AssignNode) {
            genAssign((AssignNode) node);

        } else if (node instanceof FuncDeclNode) {

        } else if (node instanceof IfNode) {
            genIf((IfNode) node);

        } else if (node instanceof WhileNode) {
            genWhile((WhileNode) node);

        } else if (node instanceof ForNode) {
            genFor((ForNode) node);

        } else if (node instanceof ReturnNode) {
            genExpr(((ReturnNode) node).getExpr());
            emit(OpCode.RETURN);

        } else if (node instanceof FuncCallNode) {
            genFuncCallExpr((FuncCallNode) node);
            emit(OpCode.POP); // discard result when used as statement

        } else {
            genExpr((ExprNode) node);
            emit(OpCode.POP);
        }
    }

    private void genAssign(AssignNode node) {
        if (node.getIdent() instanceof IdentNode) {
            genExpr(node.getExpr());
            emit(OpCode.STORE, ((IdentNode) node.getIdent()).getName());

        } else if (node.getIdent() instanceof ArrayAccessNode) {
            ArrayAccessNode aan = (ArrayAccessNode) node.getIdent();
            genExpr(aan.getArray());   // push array reference
            genExpr(aan.getIndex());   // push index
            genExpr(node.getExpr());   // push new value
            emit(OpCode.ARRAY_SET);    // arr[index] = value
        }
    }

    private void genIf(IfNode node) {
        String elseLabel = newLabel();
        String endLabel  = newLabel();

        genExpr(node.getCond());
        emit(OpCode.JMP_FALSE, elseLabel);
        genStmt(node.getThenStmt());

        if (node.getElseStmt() != null) {
            emit(OpCode.JMP, endLabel);
        }
        emit(OpCode.LABEL, elseLabel);

        if (node.getElseStmt() != null) {
            genStmt(node.getElseStmt());
            emit(OpCode.LABEL, endLabel);
        }
    }

    private void genWhile(WhileNode node) {
        String startLabel = newLabel();
        String endLabel   = newLabel();

        emit(OpCode.LABEL, startLabel);
        genExpr(node.getCond());
        emit(OpCode.JMP_FALSE, endLabel);
        genStmt(node.getBodyStmt());
        emit(OpCode.JMP, startLabel);
        emit(OpCode.LABEL, endLabel);
    }

    private void genFor(ForNode node) {
        String startLabel = newLabel();
        String endLabel   = newLabel();

        genStmt(node.getInit());
        emit(OpCode.LABEL, startLabel);
        genExpr(node.getCond());
        emit(OpCode.JMP_FALSE, endLabel);
        genStmt(node.getBody());
        genStmt(node.getIterStmt());
        emit(OpCode.JMP, startLabel);
        emit(OpCode.LABEL, endLabel);
    }


    private void genExpr(AstNode node) {
        if (node == null) { emit(OpCode.PUSH_NULL); return; }

        if (node instanceof ValueNode) {
            genValue((ValueNode) node);

        } else if (node instanceof IdentNode) {
            emit(OpCode.LOAD, ((IdentNode) node).getName());

        } else if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            genExpr(bin.getArg1());
            genExpr(bin.getArg2());
            emit(OpCode.BINOP, bin.getOp().name());

        } else if (node instanceof UnaryOpNode) {
            UnaryOpNode un = (UnaryOpNode) node;
            genExpr(un.getArg());
            emit(OpCode.UNOP, un.getOp().name());

        } else if (node instanceof CastNode) {
            CastNode cast = (CastNode) node;
            genExpr(cast.getExpr());
            emit(OpCode.CAST, cast.getTargetType().name());

        } else if (node instanceof FuncCallNode) {
            genFuncCallExpr((FuncCallNode) node);

        } else if (node instanceof ArrayNode) {
            emit(OpCode.ARRAY_NEW);
            for (ExprNode elem : ((ArrayNode) node).getElements()) {
                genExpr(elem);
                emit(OpCode.ARRAY_PUSH);
            }

        } else if (node instanceof ArrayAccessNode) {
            ArrayAccessNode aan = (ArrayAccessNode) node;
            genExpr(aan.getArray());
            genExpr(aan.getIndex());
            emit(OpCode.ARRAY_GET);

        } else if (node instanceof IfNode || node instanceof WhileNode || node instanceof ForNode) {
            genStmt((StmtNode) node);
            emit(OpCode.PUSH_NULL);

        } else {
            emit(OpCode.PUSH_NULL);
        }
    }

    private void genValue(ValueNode node) {
        ru.vsu.cs.course4.compiler.runtime.Value v = node.getValue();
        switch (v.getType()) {
            case INT:     emit(OpCode.PUSH_INT,    v.getInt());    break;
            case DOUBLE:  emit(OpCode.PUSH_DOUBLE, v.getDouble()); break;
            case STRING:  emit(OpCode.PUSH_STRING, v.getStr());    break;
            case BOOLEAN: emit(OpCode.PUSH_BOOL,   v.getBool());   break;
            default:      emit(OpCode.PUSH_NULL);                  break;
        }
    }

    private void genFuncCallExpr(FuncCallNode node) {
        for (ExprNode param : node.getParams()) {
            genExpr(param);
        }
        emit(OpCode.CALL, node.getFunc().getName() + ":" + node.getParams().size());
    }
}
