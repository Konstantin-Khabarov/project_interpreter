package ru.vsu.cs.course4.compiler;

import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.vsu.cs.course4.compiler.ast.AstNode;
import ru.vsu.cs.course4.compiler.codegen.BytecodeGenerator;
import ru.vsu.cs.course4.compiler.codegen.Instruction;
import ru.vsu.cs.course4.compiler.codegen.VirtualMachine;
import ru.vsu.cs.course4.compiler.runtime.Context;
import ru.vsu.cs.course4.compiler.runtime.Interpreter;
import ru.vsu.cs.course4.compiler.runtime.InterpreterException;
import ru.vsu.cs.course4.compiler.runtime.Value;
import ru.vsu.cs.course4.compiler.semantic.SemanticAnalyzer;
import ru.vsu.cs.course4.compiler.semantic.SemanticError;


public class Program {

    // -----------------------------------------------------------------------
    // Built-in context
    // -----------------------------------------------------------------------

    public static Context prepareContext() throws Exception {
        Reader input = new StringReader(
                "def println(a) { }\n" +
                "def print(a) { }\n" +
                "def read() { }\n" +
                "def readInt() { }\n" +
                "def readDouble() { }\n" +
                "def readString() { }\n" +
                "def readBool() { }\n" +
                "def cos(x) { }\n" +
                "def sin(x) { }\n" +
                "def pow(a,b) { }\n" +
                "def sqrt(a) { }\n" +
                "def abs(a) { }\n" +
                "def log(a) { }\n" +
                "def int(x) { }\n" +
                "def double(x) { }\n" +
                "def boolean(x) { }\n" +
                "def string(x) { }\n" +
                "def size(x) { }\n" +
                "def newArray(x) { }\n" +
                "def addItem(x, y) { }");

        Parser parser = new Parser(input);
        AstNode ast = parser.start();
        Context context = new Context(null);
        Interpreter.execute(ast, context);

        for (Map.Entry<String, Value> kv : context.values.entrySet()) {
            if (kv.getValue().getFunc() != null) {
                kv.getValue().getFunc().setInternal(true);
            }
        }
        return context;
    }

    // -----------------------------------------------------------------------
    // main
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);

        boolean printTree    = true;
        boolean runCodegen   = false;   // -codegen : generate bytecode + run via VM
        boolean runInterp    = true;    // default: run via tree-walking interpreter
        String  inputFile    = null;
        String  outputBcFile = null;    // -o <file> : save bytecode to file

        // Argument parsing
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-nt":       printTree  = false; break;
                case "-codegen":  runCodegen = true;  runInterp = false; break;
                case "-o":        if (i + 1 < args.length) outputBcFile = args[++i]; break;
                default:          if (inputFile == null) inputFile = args[i]; break;
            }
        }

        // Open input
        Reader input = (inputFile != null) ? new FileReader(inputFile)
                                           : new InputStreamReader(System.in);

        // Parse
        Parser parser = new Parser(input);
        AstNode ast;
        try {
            ast = parser.start();
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            System.exit(1);
            return;
        }

        if (printTree) {
            System.out.println("=== AST ===");
            ast.printTree(System.out);
            System.out.println();
        }

        // Prepare built-in function context
        Context builtinContext = prepareContext();

        // ── Semantic analysis ──────────────────────────────────────────────
        System.out.println("=== Semantic analysis ===");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        List<SemanticError> semanticErrors = analyzer.analyze(ast, builtinContext);

        if (!semanticErrors.isEmpty()) {
            for (SemanticError err : semanticErrors) {
                System.err.println(err);
            }
            System.err.println(semanticErrors.size() + " semantic error(s) found. Aborting.");
            System.exit(1);
            return;
        }
        System.out.println("No semantic errors found.");
        System.out.println();

        if (printTree) {
            System.out.println("=== AST after semantic analysis (with implicit casts) ===");
            ast.printTree(System.out);
            System.out.println();
        }

        // ── Execution ─────────────────────────────────────────────────────
        if (runCodegen) {
            // Code generation path
            System.out.println("=== Code generation ===");
            BytecodeGenerator gen = new BytecodeGenerator();
            List<Instruction> bytecode = gen.generate(ast);

            // Print/save bytecode
            if (outputBcFile != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(outputBcFile))) {
                    VirtualMachine.writeBytecode(bytecode, pw);
                }
                System.out.println("Bytecode written to: " + outputBcFile);
            } else {
                PrintWriter pw = new PrintWriter(System.out);
                VirtualMachine.writeBytecode(bytecode, pw);
                pw.flush();
            }

            System.out.println();
            System.out.println("=== VM execution ===");
            VirtualMachine vm = new VirtualMachine();
            vm.load(bytecode);
            try {
                vm.execute();
            } catch (InterpreterException e) {
                System.err.println("VM runtime error: " + e.getMessage());
                System.exit(1);
            }

        } else if (runInterp) {
            // Tree-walking interpreter path (default)
            System.out.println("=== Interpreter execution ===");
            try {
                Interpreter.execute(ast, builtinContext);
            } catch (InterpreterException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}
