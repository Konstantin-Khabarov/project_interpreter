package ru.vsu.cs.course4.compiler;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;

import ru.vsu.cs.course4.compiler.ast.AstNode;
import ru.vsu.cs.course4.compiler.runtime.Context;
import ru.vsu.cs.course4.compiler.runtime.Interpreter;
import ru.vsu.cs.course4.compiler.runtime.InterpreterException;
import ru.vsu.cs.course4.compiler.runtime.Value;


public class Program {
    public static Context prepareContext() throws Exception {
        Reader input = new StringReader("def println(a) { }\n" +
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

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);

        boolean isPrintTree = true;
        Reader input = null;
        if (args.length > 0) {
            if (args[0].equals("-nt")) {
                isPrintTree = false;
                input = new FileReader(args[1]);
            } else {
                input = new FileReader(args[0]);
            }
        } else {
            input = new InputStreamReader(System.in);
        }

        Parser parser = new Parser(input);
        AstNode ast = parser.start();
        if (isPrintTree) {
            ast.printTree(System.out);
        }
        Context context = prepareContext();

        try {
            Interpreter.execute(ast, context);
        } catch (InterpreterException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
